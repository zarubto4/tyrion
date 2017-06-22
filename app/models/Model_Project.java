package models;

import com.avaje.ebean.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.helps_objects.IdsList;
import utilities.enums.*;
import utilities.logger.Class_Logger;
import utilities.notifications.helps_objects.Becki_color;
import utilities.notifications.helps_objects.Notification_Button;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.swagger.outboundClass.*;
import javax.persistence.*;
import javax.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "Project", description = "Model of Project")
public class Model_Project extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_Project.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                                      @Id public String id;
                                                                          public String name;
                                                                          public String description;
                                                                          public boolean removed_by_user;

    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_BProgram>                b_programs        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_CProgram>                c_programs        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_MProject>                m_projects        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) @OrderBy("UPPER(name) ASC")                   public List<Model_TypeOfBlock>             type_of_blocks    = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) @OrderBy("UPPER(name) ASC")                   public List<Model_TypeOfWidget>            type_of_widgets   = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) @OrderBy("date_of_user_registration desc")    public List<Model_Board>                   boards            = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) @OrderBy("date_of_creation desc")             public List<Model_Invitation>              invitations       = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) @OrderBy("id asc")                            public List<Model_ProjectParticipant>      participants      = new ArrayList<>();

    // reference na Fake Instanci - kam připojuji Yody q- pokud nejsou připojení do vlastní instnace vytvořené v blocko programu
    @JsonIgnore @OneToOne(fetch = FetchType.EAGER)  public Model_HomerInstance private_instance;
    @JsonIgnore @ManyToOne(fetch = FetchType.EAGER) public Model_Product product;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_Board_Short_Detail>         boards()             { List<Swagger_Board_Short_Detail>       l = new ArrayList<>();    for( Model_Board m           : boards)         l.add(m.get_short_board());                return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_B_Program_Short_Detail>     b_programs()         { List<Swagger_B_Program_Short_Detail>   l = new ArrayList<>();    for( Model_BProgram m        : get_b_programs_not_deleted()) l.add(m.get_b_program_short_detail()); return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_C_program_Short_Detail>     c_programs()         { List<Swagger_C_program_Short_Detail>   l = new ArrayList<>();    for( Model_CProgram m        : get_c_programs_not_deleted()) l.add(m.get_c_program_short_detail()); return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_Library_Short_Detail>       c_private_libraries(){ List<Swagger_Library_Short_Detail>     l = new ArrayList<>();    for( Model_Library m         : get_c_privates_project_libraries_not_deleted()) l.add(m.get_short_library()); return l;}

    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_M_Project_Short_Detail>     m_projects()         { List<Swagger_M_Project_Short_Detail>   l = new ArrayList<>();    for( Model_MProject m        : get_m_projects_not_deleted()) l.add(m.get_short_m_project()); return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_TypeOfBlock_Short_Detail>   type_of_blocks()     { List<Swagger_TypeOfBlock_Short_Detail> l = new ArrayList<>();    for( Model_TypeOfBlock m     : get_type_of_blocks_not_deleted()) l.add(m.get_type_of_block_short_detail()); return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_TypeOfWidget_Short_Detail>  type_of_widgets()    { List<Swagger_TypeOfWidget_Short_Detail>l = new ArrayList<>();    for( Model_TypeOfWidget m    : get_type_of_widgets_not_deleted())l.add(m.get_typeOfWidget_short_detail());  return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_Instance_Short_Detail>      instancies()         { List<Swagger_Instance_Short_Detail>    l = new ArrayList<>();    for( Model_HomerInstance m   : get_instances_not_deleted()) l.add(m.get_instance_short_detail());  return l;}

    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean active() { return product.active;}

    @JsonProperty @Transient @ApiModelProperty(required = true) public String product_name() { return product.name;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public String product_id() { return product.id;}

    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Model_ProjectParticipant> participants() {

        List<Model_ProjectParticipant> project_participants = this.participants;

        for(Model_Invitation invitation : invitations){

            Model_Person person = Model_Person.find.where().eq("mail", invitation.mail).findUnique();

            Model_ProjectParticipant project_participant = new Model_ProjectParticipant();

            if(person != null){

                project_participant.person = person;
            }else project_participant.user_email = invitation.mail;

            project_participant.state = Enum_Participant_status.invited;

            project_participants.add(project_participant);
        }

        return  project_participants;
    }

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        terminal_logger.debug("save :: Creating new Object");
        while(true){ // I need Unique Value
            this.id = UUID.randomUUID().toString();
            this.blob_project_link = product.get_path() + "/projects/" + this.id;
            if (Model_Project.find.byId(this.id) == null) break;
        }

        Model_HomerInstance instance = new Model_HomerInstance();
        instance.instance_type = Enum_Homer_instance_type.VIRTUAL;
        instance.cloud_homer_server = Model_HomerServer.get_destination_server();
        instance.save();

        this.private_instance = instance;
        super.save();
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update: Update object value: {}",  this.id);

        try {

            if (cache.containsKey(this.id)) cache.replace(this.id, this);


        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }

        super.update();
    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("delete: Delete object Id: {} ", this.id);
        removed_by_user = true;

        try {

            if (cache.containsKey(this.id)) cache.remove(this.id);

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }

        update();
    }


/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* GET SQL PARAMETER - CACHE OBJECTS ------------------------------------------------------------------------------------*/

    @JsonIgnore
    public List<Model_CProgram> get_c_programs_not_deleted(){
        try {
            return Model_CProgram.find.where().eq("project.id", id).eq("removed_by_user", false).orderBy("UPPER(name) ASC").findList();
        }catch (Exception e){
            terminal_logger.internalServerError("get_c_programs_not_deleted", e);
            return new ArrayList<Model_CProgram>();
        }
    }

    @JsonIgnore
    public List<Model_Library> get_c_privates_project_libraries_not_deleted(){
        try {
            return Model_Library.find.where().eq("project_id", id).eq("removed_by_user", false).orderBy("UPPER(name) ASC").findList();
        }catch (Exception e){
            terminal_logger.internalServerError("get_c_privates_project_libraries_not_deleted", e);
            return new ArrayList<Model_Library>();
        }
    }

    @JsonIgnore
    public List<Model_BProgram> get_b_programs_not_deleted(){
        try{
            return Model_BProgram.find.where().eq("project.id", id).eq("removed_by_user", false).orderBy("UPPER(name) ASC").findList();
        }catch (Exception e){
            terminal_logger.internalServerError("get_b_programs_not_deleted", e);
            return new ArrayList<Model_BProgram>();
        }
    }

    @JsonIgnore
    public List<Model_MProject> get_m_projects_not_deleted(){
        try{
            return Model_MProject.find.where().eq("project.id", id).eq("removed_by_user", false).orderBy("UPPER(name) ASC").findList();
        }catch (Exception e){
            terminal_logger.internalServerError("get_m_projects_not_deleted", e);
            return new ArrayList<Model_MProject>();
        }
    }

    @JsonIgnore
    public List<Model_TypeOfWidget> get_type_of_widgets_not_deleted(){
        try{
            return Model_TypeOfWidget.find.where().eq("project.id", id).eq("removed_by_user", false).orderBy("UPPER(name) ASC").findList();
        }catch (Exception e){
            terminal_logger.internalServerError("get_type_of_widgets_not_deleted", e);
            return new ArrayList<Model_TypeOfWidget>();
        }
    }

    @JsonIgnore
    public List<Model_TypeOfBlock> get_type_of_blocks_not_deleted(){
        try{
            return Model_TypeOfBlock.find.where().eq("project.id", id).eq("removed_by_user", false).orderBy("UPPER(name) ASC").findList();
        }catch (Exception e){
            terminal_logger.internalServerError("get_type_of_blocks_not_deleted", e);
            return new ArrayList<Model_TypeOfBlock>();
        }
    }

    @JsonIgnore
    public List<Model_HomerInstance> get_instances_not_deleted(){
        try{
            return Model_HomerInstance.find.where().ne("removed_by_user", true).isNotNull("actual_instance").eq("b_program.project.id", id).findList();
        }catch (Exception e){
            terminal_logger.internalServerError("get_instances_not_deleted", e);
            return new ArrayList<Model_HomerInstance>();
        }
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void notification_project_invitation(Model_Person person, Model_Invitation invitation){

        Model_Person owner = Controller_Security.get_person();

        new Model_Notification()
                .setImportance(Enum_Notification_importance.normal)
                .setLevel(Enum_Notification_level.info)
                .setText(new Notification_Text().setText("User "))
                .setObject(owner)
                .setText(new Notification_Text().setText(" invited you into the project "))
                .setObject(this)
                .setText(new Notification_Text().setText(". Do you accept the invitation?"))
                .setButton( new Notification_Button().setAction(Enum_Notification_action.accept_project_invitation).setPayload(invitation.id).setColor(Becki_color.byzance_green).setText("Yes")  )
                .setButton( new Notification_Button().setAction(Enum_Notification_action.reject_project_invitation).setPayload(invitation.id).setColor(Becki_color.byzance_red).setText("No")  )
                .send(person);
    }

    @JsonIgnore @Transient
    public void notification_project_invitation_rejected(Model_Person owner){

        Model_Person person = Controller_Security.get_person();

        new Model_Notification()
                .setImportance(Enum_Notification_importance.normal)
                .setLevel(Enum_Notification_level.info)
                .setText(new Notification_Text().setText("User "))
                .setObject(person)
                .setText(new Notification_Text().setText(" did not accept your invitation to the project "))
                .setObject(this)
                .setText(new Notification_Text().setText("."))
                .send(owner);
    }

    @JsonIgnore @Transient
    public void notification_project_invitation_accepted(Model_Person invitee, Model_Person owner){

        new Model_Notification()
                .setImportance(Enum_Notification_importance.normal)
                .setLevel(Enum_Notification_level.info)
                .setText(new Notification_Text().setText("User "))
                .setObject(invitee)
                .setText(new Notification_Text().setText(" accepted your invitation to the project "))
                .setObject(this)
                .setText(new Notification_Text().setText("."))
                .send(owner);
    }

    @JsonIgnore @Transient
    public void notification_project_participant_change_status(Model_ProjectParticipant participant){

        Model_Person person = Controller_Security.get_person();

        new Model_Notification()
                .setImportance(Enum_Notification_importance.normal)
                .setLevel(Enum_Notification_level.info)
                .setText(new Notification_Text().setText("User "))
                .setObject(person)
                .setText(new Notification_Text().setText(" changed your status in project "))
                .setObject(this)
                .setText(new Notification_Text().setText(" to " + participant.state.name() + ". You have different permissions now."))
                .send(participant.person);
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore private String blob_project_link;

    @JsonIgnore @Transient
    public String get_path(){
        return  blob_project_link;
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: For all project: User can read project on API: {GET /project) - get Project by logged Person ";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore                                      public boolean create_permission()    {return true;}
    @JsonProperty @ApiModelProperty(required = true) public boolean update_permission()    {return (Model_Project.find.where().eq("participants.person.id", Controller_Security.get_person_id()).where().eq("id", id).findRowCount() > 0) || Controller_Security.get_person().has_permission("Project_update");}
    @JsonIgnore                                      public boolean read_permission()      {return (Model_Project.find.where().eq("participants.person.id", Controller_Security.get_person_id()).where().eq("id", id).findRowCount() > 0) || Controller_Security.get_person().has_permission("Project_read");}

    @JsonProperty @ApiModelProperty(required = true) public boolean unshare_permission()   {return (Model_ProjectParticipant.find.where().eq("project.id", id).where().eq("person.id", Controller_Security.get_person_id()).where().disjunction().add(Expr.eq("state", Enum_Participant_status.owner)).add(Expr.eq("state", Enum_Participant_status.admin)).findRowCount() > 0) || Controller_Security.get_person().has_permission("Project_unshare");}
    @JsonProperty @ApiModelProperty(required = true) public boolean share_permission ()    {return (Model_ProjectParticipant.find.where().eq("project.id", id).where().eq("person.id", Controller_Security.get_person_id()).where().disjunction().add(Expr.eq("state", Enum_Participant_status.owner)).add(Expr.eq("state", Enum_Participant_status.admin)).findRowCount() > 0) || Controller_Security.get_person().has_permission("Project_share");}
    @JsonProperty @ApiModelProperty(required = true) public boolean admin_permission ()    {return (Model_ProjectParticipant.find.where().eq("project.id", id).where().eq("person.id", Controller_Security.get_person_id()).where().disjunction().add(Expr.eq("state", Enum_Participant_status.owner)).add(Expr.eq("state", Enum_Participant_status.admin)).findRowCount() > 0) || Controller_Security.get_person().has_permission("Project_admin");}

    @JsonProperty @ApiModelProperty(required = true) public boolean edit_permission()      {return (Model_Project.find.where().eq("participants.person.id", Controller_Security.get_person_id()).where().eq("id", id).findRowCount() > 0) || Controller_Security.get_person().has_permission("Project_edit");}
    @JsonProperty @ApiModelProperty(required = true) public boolean delete_permission()    {return (Model_ProjectParticipant.find.where().eq("project.id", id).where().eq("person.id", Controller_Security.get_person_id()).where().eq("state", Enum_Participant_status.owner).findRowCount() > 0) || Controller_Security.get_person().has_permission("Project_delete");}
    @JsonIgnore                                      public boolean financial_permission() {return this.product.financial_permission("project");}

    public enum permissions{Project_update, Project_read, Project_unshare , Project_share, Project_edit, Project_delete, Project_admin}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE                          = Model_Project.class.getSimpleName();
    public static final String CACHE_BECKI_CONNECTED_PERSONS  = Model_Project.class.getSimpleName() + "_BECKI_CONNECTED_PERSONS_ID";

    public static Cache<String, Model_Project> cache = null;
    public static Cache<String, IdsList> token_cache = null;  // < Project_Id, List<Person_id>> // Only connected on Websocket with Becki

    public static Model_Project get_byId(String id){

        Model_Project project = cache.get(id);
        if (project == null){

            project = find.where().eq("id", id).eq("removed_by_user", false).findUnique();
            if (project == null) return null;

            cache.put(id, project);
        }

        return project;
    }

    public static void becki_person_id_subscribe(String person_id){

        List<Model_Project> list_of_projects = Model_Project.find.where().eq("participants.person.id", person_id).disjunction()
                    .eq("state", Enum_Participant_status.admin)
                    .eq("state", Enum_Participant_status.member)
                    .eq("state", Enum_Participant_status.owner)
                .endJunction()
                .findList();

        for(Model_Project project : list_of_projects ){

            IdsList idlist = token_cache.get(project.id);

            if(idlist == null) idlist = new IdsList();

            if(!idlist.list.contains(person_id)) idlist.list.add(person_id);

            token_cache.put(project.id, idlist);
        }
    }

    public static void becki_person_id_unsubscribe(String person_id){

        List<Model_Project> list_of_projects = Model_Project.find.where().eq("participants.person.id", person_id).disjunction()
                .eq("state", Enum_Participant_status.admin)
                .eq("state", Enum_Participant_status.member)
                .eq("state", Enum_Participant_status.owner)
                .endJunction()
                .findList();

        for(Model_Project project : list_of_projects ){

            IdsList idlist = token_cache.get(project.id);

            if(idlist != null){
                idlist.list.remove(person_id);
            }
        }
    }

    public static List<String> get_project_becki_person_ids_list(String project_id){

        IdsList idlist = token_cache.get(project_id);

        if(idlist == null){

            idlist = new IdsList();

            Model_Project project = get_byId(project_id);
            if (project == null) return idlist.list;

            for(Model_ProjectParticipant participant : project.participants){

                if(participant.state == Enum_Participant_status.invited ) continue;

                if(!idlist.list.contains(participant.person.id))  idlist.list.add(participant.person.id);
            }

            token_cache.put(project_id, idlist);
        }

        return idlist.list;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_Project> find = new Finder<>(Model_Project.class);
}