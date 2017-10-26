package models;

import com.avaje.ebean.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.helps_objects.IdsList;
import utilities.cache.helps_objects.TyrionCachedList;
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
import java.util.stream.Collectors;

@Entity
@ApiModel(value = "Project", description = "Model of Project")
@Table(name="Project")
public class Model_Project extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_Project.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                                      @Id public String id;
                                                                          public String name;
                                                                          public String description;
                                                           @JsonIgnore    public boolean removed_by_user;

    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_BProgram>                b_programs        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_CProgram>                c_programs        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_Library>                 libraries         = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_MProject>                m_projects        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_TypeOfBlock>             type_of_blocks    = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_TypeOfWidget>            type_of_widgets   = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_Board>                   boards            = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_BoardGroup>              board_groups      = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) @OrderBy("date_of_creation desc")             public List<Model_Invitation>              invitations       = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) @OrderBy("id asc")                            public List<Model_ProjectParticipant>      participants      = new ArrayList<>();

    // reference na Fake Instanci - kam připojuji Yody q- pokud nejsou připojení do vlastní instnace vytvořené v blocko programu

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Product product;

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    // TODO - CHCI CACHE INGOR - NA VŠECHNY DB HODNOTY KTERÉ JSOU VÝŠE
    // COŽ ZNAMENÁ ŽE SI CACHE PÚAMATUJE JEN ID REFERENCE

    @JsonIgnore @Transient @TyrionCachedList public List<String> cache_list_board_ids = new ArrayList<>();
    @JsonIgnore @Transient @TyrionCachedList public List<String> cache_list_c_program_ids = new ArrayList<>();
    @JsonIgnore @Transient @TyrionCachedList public List<String> cache_list_library_ids = new ArrayList<>();
    @JsonIgnore @Transient @TyrionCachedList public List<String> cache_list_b_program_ids = new ArrayList<>();
    @JsonIgnore @Transient @TyrionCachedList public List<String> cache_list_m_project_ids = new ArrayList<>();
    @JsonIgnore @Transient @TyrionCachedList public List<String> cache_hardware_groups_ids = new ArrayList<>();
    @JsonIgnore @Transient @TyrionCachedList public List<String> cache_list_type_of_widgets_ids = new ArrayList<>();
    @JsonIgnore @Transient @TyrionCachedList public List<String> cache_list_type_of_blocks_ids = new ArrayList<>();
    @JsonIgnore @Transient @TyrionCachedList public List<String> cache_list_instance_ids = new ArrayList<>();
    @JsonIgnore @Transient @TyrionCachedList private String cache_value_product_id;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_Board_Short_Detail>         boards()             { List<Swagger_Board_Short_Detail>       l = new ArrayList<>();    if(!active()) return l; for( Model_Board m           : get_project_boards_not_deleted())         l.add(m.get_short_board());  return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_B_Program_Short_Detail>     b_programs()         { List<Swagger_B_Program_Short_Detail>   l = new ArrayList<>();    if(!active()) return l; for( Model_BProgram m        : get_b_programs_not_deleted()) l.add(m.get_b_program_short_detail()); return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_C_program_Short_Detail>     c_programs()         { List<Swagger_C_program_Short_Detail>   l = new ArrayList<>();    if(!active()) return l; for( Model_CProgram m        : get_c_programs_not_deleted()) l.add(m.get_c_program_short_detail()); return l;}
     @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_Library_Short_Detail>      libraries()          { List<Swagger_Library_Short_Detail>     l = new ArrayList<>();    if(!active()) return l; for( Model_Library m         : get_c_privates_project_libraries_not_deleted()) l.add(m.get_short_library()); return l;}

    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_M_Project_Short_Detail>     m_projects()         { List<Swagger_M_Project_Short_Detail>   l = new ArrayList<>();    if(!active()) return l; for( Model_MProject m        : get_m_projects_not_deleted()) l.add(m.get_short_m_project()); return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_TypeOfBlock_Short_Detail>   type_of_blocks()     { List<Swagger_TypeOfBlock_Short_Detail> l = new ArrayList<>();    if(!active()) return l; for( Model_TypeOfBlock m     : get_type_of_blocks_not_deleted()) l.add(m.get_type_of_block_short_detail()); return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_TypeOfWidget_Short_Detail>  type_of_widgets()    { List<Swagger_TypeOfWidget_Short_Detail>l = new ArrayList<>();    if(!active()) return l; for( Model_TypeOfWidget m    : get_type_of_widgets_not_deleted())l.add(m.get_typeOfWidget_short_detail());  return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_Instance_Short_Detail>      instancies()         { List<Swagger_Instance_Short_Detail>    l = new ArrayList<>();    if(!active()) return l; for( Model_HomerInstance m   : get_instances_not_deleted()) l.add(m.get_instance_short_detail());  return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean active() { return get_product().active;}

    @JsonProperty @Transient @ApiModelProperty(required = true) public String product_name() { return get_product().name;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public String product_id()   { return get_product().id;}

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


    @JsonIgnore @Transient public Swagger_Project_Short_Detail project_short_detail(){

        Swagger_Project_Short_Detail short_detail = new Swagger_Project_Short_Detail();

        short_detail.project_id = id;
        short_detail.project_name = name;
        short_detail.project_description = description;

        short_detail.product_name = product_name();
        short_detail.product_id = product_id();

        short_detail.edit_permission = edit_permission();
        short_detail.delete_permission = delete_permission();

        short_detail.active_status = active();

        return short_detail;

    }


/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public boolean isParticipant(Model_Person person){

        return participants.stream().anyMatch(participant -> participant.person.id.equals(person.id));
    }

    @JsonIgnore @Override public void save() {

        terminal_logger.debug("save :: Creating new Object");
        while(true){ // I need Unique Value
            this.id = UUID.randomUUID().toString();
            this.blob_project_link = product.get_path() + "/projects/" + this.id;
            if (Model_Project.find.byId(this.id) == null) break;
        }

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

    @JsonIgnore @TyrionCachedList
    public List<Model_Board> get_project_boards_not_deleted(){
        try {

            // Cache
            if(cache_list_board_ids.isEmpty()) {

                List<Model_Board> boards = Model_Board.find.where().eq("project.id", id).order().asc("date_of_user_registration").select("id").findList();

                // Získání seznamu
                for (Model_Board board : boards) {
                    cache_list_board_ids.add(board.id);
                }
            }

            List<Model_Board> board_list = new ArrayList<>();

            for(String board_id : cache_list_board_ids){
                board_list.add(Model_Board.get_byId(board_id));
            }

            return board_list;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new ArrayList<Model_Board>();
        }
    }

    @JsonIgnore @TyrionCachedList
    public List<Model_CProgram> get_c_programs_not_deleted(){
        try {

            // Cache
            if(cache_list_c_program_ids.isEmpty()){

                List<Model_CProgram> c_programs = Model_CProgram.find.where().eq("project.id", id).eq("removed_by_user", false).orderBy("UPPER(name) ASC").select("id").findList();

                // Získání seznamu
                for (Model_CProgram cProgram : c_programs) {
                    cache_list_c_program_ids.add(cProgram.id);
                }
            }

            List<Model_CProgram> c_programs = new ArrayList<>();

            for(String c_program_id : cache_list_c_program_ids){
                c_programs.add(Model_CProgram.get_byId(c_program_id));
            }

            return c_programs;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new ArrayList<Model_CProgram>();
        }
    }

    @JsonIgnore @TyrionCachedList
    public List<Model_Library> get_c_privates_project_libraries_not_deleted(){
        try {

            if(cache_list_library_ids.isEmpty()){

                List<Model_Library> libraries = Model_Library.find.where().eq("project.id", id).eq("removed_by_user", false).orderBy("UPPER(name) ASC").select("id").findList();

                // Získání seznamu
                for (Model_Library library : libraries) {
                    cache_list_library_ids.add(library.id);
                }

            }

            List<Model_Library> libraries = new ArrayList<>();

            for(String library_id : cache_list_library_ids){
                libraries.add(Model_Library.get_byId(library_id));
            }

            return libraries;


        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new ArrayList<Model_Library>();
        }
    }

    @JsonIgnore @TyrionCachedList
    public List<Model_BProgram> get_b_programs_not_deleted(){
        try{

            if(cache_list_b_program_ids.isEmpty()){

                List<Model_BProgram> b_programs = Model_BProgram.find.where().eq("project.id", id).eq("removed_by_user", false).orderBy("UPPER(name) ASC").select("id").findList();

                // Získání seznamu
                for (Model_BProgram b_program : b_programs) {
                    cache_list_b_program_ids.add(b_program.id);
                }

            }

            List<Model_BProgram> b_programs  = new ArrayList<>();

            for(String b_program_id : cache_list_b_program_ids){
                b_programs.add(Model_BProgram.get_byId(b_program_id));
            }

            return b_programs;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new ArrayList<Model_BProgram>();
        }
    }

    @JsonIgnore @TyrionCachedList
    public List<Model_MProject> get_m_projects_not_deleted(){
        try{

            if(cache_list_m_project_ids.isEmpty()){

                List<Model_MProject> m_projects = Model_MProject.find.where().eq("project.id", id).eq("removed_by_user", false).orderBy("UPPER(name) ASC").select("id").findList();

                // Získání seznamu
                for (Model_MProject m_project : m_projects) {
                    cache_list_m_project_ids.add(m_project.id);
                }

            }

            List<Model_MProject> m_projects  = new ArrayList<>();

            for(String m_project_id : cache_list_m_project_ids){
                m_projects.add(Model_MProject.get_byId(m_project_id));
            }

            return m_projects;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new ArrayList<Model_MProject>();
        }
    }

    @JsonIgnore @TyrionCachedList
    public List<Model_BoardGroup> get_hardware_groups_not_deleted(){
        try{

            if(cache_hardware_groups_ids.isEmpty()){

                List<Model_BoardGroup> board_groups = Model_BoardGroup.find.where().eq("project.id", id).eq("removed_by_user", false).orderBy("UPPER(name) ASC").select("id").findList();

                // Získání seznamu
                for (Model_BoardGroup board_group : board_groups) {
                    cache_hardware_groups_ids.add(board_group.id.toString());
                }

            }

            List<Model_BoardGroup> board_groups  = new ArrayList<>();

            for(String board_group_id : cache_hardware_groups_ids){
                board_groups.add(Model_BoardGroup.get_byId(board_group_id));
            }

            return board_groups;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new ArrayList<Model_BoardGroup>();
        }
    }


    @JsonIgnore @TyrionCachedList
    public List<Model_TypeOfWidget> get_type_of_widgets_not_deleted(){
        try{

            if(cache_list_type_of_widgets_ids.isEmpty()){

                List<Model_TypeOfWidget> typeOfWidgets = Model_TypeOfWidget.find.where().eq("project.id", id).eq("removed_by_user", false).orderBy("UPPER(name) ASC").select("id").findList();

                // Získání seznamu
                for (Model_TypeOfWidget typeOfWidget : typeOfWidgets) {
                    cache_list_type_of_widgets_ids.add(typeOfWidget.id);
                }

            }

            List<Model_TypeOfWidget> typeOfWidgets  = new ArrayList<>();

            for(String typeOfWidget_id : cache_list_type_of_widgets_ids){
                typeOfWidgets.add(Model_TypeOfWidget.get_byId(typeOfWidget_id));
            }

            return typeOfWidgets;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new ArrayList<Model_TypeOfWidget>();
        }
    }

    @JsonIgnore @TyrionCachedList
    public List<Model_TypeOfBlock> get_type_of_blocks_not_deleted(){
        try{


            if(cache_list_type_of_blocks_ids.isEmpty()){

                List<Model_TypeOfBlock> typeOfBlocks = Model_TypeOfBlock.find.where().eq("project.id", id).eq("removed_by_user", false).orderBy("UPPER(name) ASC").select("id").findList();

                // Získání seznamu
                for (Model_TypeOfBlock typeOfBlock : typeOfBlocks) {
                    cache_list_type_of_blocks_ids.add(typeOfBlock.id);
                }

            }

            List<Model_TypeOfBlock> typeOfBlocks  = new ArrayList<>();

            for(String type_of_blocks_id : cache_list_type_of_blocks_ids){
                typeOfBlocks.add(Model_TypeOfBlock.get_byId(type_of_blocks_id));
            }

            return typeOfBlocks;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new ArrayList<Model_TypeOfBlock>();
        }
    }

    @JsonIgnore @TyrionCachedList
    public List<Model_HomerInstance> get_instances_not_deleted(){
        try{


            if(cache_list_instance_ids.isEmpty()){

                List<Model_HomerInstance> instances = Model_HomerInstance.find.where().ne("removed_by_user", true).isNotNull("actual_instance").eq("b_program.project.id", id).select("id").findList();

                // Získání seznamu
                for (Model_HomerInstance instance : instances) {
                    cache_list_instance_ids.add(instance.id);
                }

            }

            List<Model_HomerInstance> instances  = new ArrayList<>();

            for(String type_of_blocks_id : cache_list_instance_ids){
                instances.add(Model_HomerInstance.get_byId(type_of_blocks_id));
            }

            return instances;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new ArrayList<Model_HomerInstance>();
        }
    }

    @JsonIgnore @TyrionCachedList
    public Model_Product get_product(){

        if(cache_value_product_id == null){
            Model_Product product = Model_Product.find.where().eq("projects.id", id).select("id").findUnique();
            if (product == null) return null;

            cache_value_product_id = product.id;
        }

        return Model_Product.get_byId(cache_value_product_id);
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

    @JsonIgnore public boolean create_permission() {
        return product.active && product.create_permission();
    }

    @JsonProperty public boolean update_permission()    {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().has_permission("project_update_" + id)) return Controller_Security.get_person().has_permission("project_update_"+ id);
        if(Controller_Security.get_person().has_permission("Project_update")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if( Model_Project.find.where().eq("participants.person.id", Controller_Security.get_person_id()).where().eq("id", id).findRowCount() > 0){
            Controller_Security.get_person().cache_permission("project_update_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().cache_permission("project_update_" + id, false);
        return false;
    }
    @JsonIgnore public boolean read_permission()      {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().has_permission("project_read_" + id)) return Controller_Security.get_person().has_permission("project_read_"+ id);
        if(Controller_Security.get_person().has_permission("Project_read")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
        if( Model_Project.find.where().eq("participants.person.id", Controller_Security.get_person_id()).where().eq("id", id).findRowCount() > 0){
            Controller_Security.get_person().cache_permission("project_read_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().cache_permission("project_read_" + id, false);
        return false;
    }


    @JsonProperty public boolean edit_permission()      {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().has_permission("project_edit_" + id)) return Controller_Security.get_person().has_permission("project_edit_"+ id);
        if(Controller_Security.get_person().has_permission("Project_edit")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if( Model_Project.find.where().eq("participants.person.id", Controller_Security.get_person_id()).where().eq("id", id).findRowCount() > 0){
            Controller_Security.get_person().cache_permission("project_edit_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().cache_permission("projecte_edit_" + id, false);
        return false;

    }

    @JsonProperty public boolean delete_permission()    {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().has_permission("project_delete_" + id)) return Controller_Security.get_person().has_permission("project_delete_"+ id);
        if(Controller_Security.get_person().has_permission("Project_delete")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if( Model_ProjectParticipant.find.where().eq("project.id", id).where().eq("person.id", Controller_Security.get_person_id()).where().eq("state", Enum_Participant_status.owner).findRowCount() > 0){
            Controller_Security.get_person().cache_permission("project_delete_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().cache_permission("project_delete_" + id, false);
        return false;

    }

    @JsonProperty public boolean unshare_permission()   {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().has_permission("project_share_" + id)) return Controller_Security.get_person().has_permission("project_share_"+ id);
        if(Controller_Security.get_person().has_permission("Project_unshare")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if( Model_ProjectParticipant.find.where().eq("project.id", id).where().eq("person.id", Controller_Security.get_person_id()).where().disjunction().add(Expr.eq("state", Enum_Participant_status.owner)).add(Expr.eq("state", Enum_Participant_status.admin)).findRowCount()> 0){
            Controller_Security.get_person().cache_permission("project_share_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().cache_permission("project_share_" + id, false);
        return false;
    }

    @JsonProperty public boolean share_permission ()    {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().has_permission("project_unshare_" + id)) return Controller_Security.get_person().has_permission("project_unshare_"+ id);
        if(Controller_Security.get_person().has_permission("Project_share")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if( Model_ProjectParticipant.find.where().eq("project.id", id).where().eq("person.id", Controller_Security.get_person_id()).where().disjunction().add(Expr.eq("state", Enum_Participant_status.owner)).add(Expr.eq("state", Enum_Participant_status.admin)).findRowCount()> 0){
            Controller_Security.get_person().cache_permission("project_unshare_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().cache_permission("project_unshare_" + id, false);
        return false;

    }


    @JsonProperty public boolean admin_permission ()    {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().has_permission("project_admin_permission_" + id)) return Controller_Security.get_person().has_permission("project_admin_permission_"+ id);
        if(Controller_Security.get_person().has_permission("Project_admin")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if( Model_ProjectParticipant.find.where().eq("project.id", id).where().eq("person.id", Controller_Security.get_person_id()).where().disjunction().add(Expr.eq("state", Enum_Participant_status.owner)).add(Expr.eq("state", Enum_Participant_status.admin)).findRowCount()> 0){
            Controller_Security.get_person().cache_permission("project_admin_permission_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().cache_permission("project_admin_permission_" + id, false);
        return false;

    }

    @JsonIgnore public boolean financial_permission() {return this.product.financial_permission("project");}

    public enum permissions{Project_update, Project_read, Project_unshare , Project_share, Project_edit, Project_delete, Project_admin}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE                          = Model_Project.class.getSimpleName();
    public static final String CACHE_BECKI_CONNECTED_PERSONS  = Model_Project.class.getSimpleName() + "_BECKI_CONNECTED_PERSONS_ID";

    public static Cache<String, Model_Project> cache = null;
    public static Cache<String, IdsList> token_cache = null;  // < Project_Id, List<Person_id>> // Only connected on Websocket with Becki

    public static Model_Project get_byId(String id){

        Model_Project project = cache.get(id);
        if (project == null){

            terminal_logger.debug("Project {} is not in cache", id);

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
                if(idlist.list.contains(person_id)) {
                    idlist.list.remove(person_id);
                }
            }
        }
    }

    public static List<String> get_project_becki_person_ids_list(String project_id){

        if(token_cache == null){
            terminal_logger.error("get_project_becki_person_ids_list:: token_cache is null");
            return new ArrayList<>();
        }

        if(project_id == null){
            terminal_logger.error("get_project_becki_person_ids_list:: project_id is null");
            return new ArrayList<>();
        }

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