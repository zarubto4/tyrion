package models.project.global;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.blocko.Model_TypeOfBlock;
import models.compiler.Model_Board;
import models.grid.Model_TypeOfWidget;
import models.notification.Model_Notification;
import models.person.Model_Invitation;
import models.person.Model_Person;
import models.project.b_program.Model_BProgram;
import models.project.b_program.instnace.Model_HomerInstance;
import models.project.b_program.servers.Model_HomerServer;
import models.project.c_program.Model_CProgram;
import models.project.m_program.Model_MProject;
import utilities.enums.Notification_action;
import utilities.enums.Notification_importance;
import utilities.enums.Notification_level;
import utilities.enums.Participant_status;
import utilities.swagger.outboundClass.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@ApiModel(description = "Model of Project",
        value = "Project")
public class Model_Project extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                                      @Id public String id;
                                                                          public String name;
                                                                          public String description;

    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Model_BProgram>                b_programs        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Model_CProgram>                c_programs        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Model_MProject>                m_projects        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Model_TypeOfBlock>             type_of_blocks    = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Model_TypeOfWidget>            type_of_widgets   = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Model_Board>                   boards            = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Model_Invitation>              invitations       = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Model_ProjectParticipant>      participants      = new ArrayList<>();

    // reference na Fake Instanci - kam připojuji Yody q- pokud nejsou připojení do vlastní instnace vytvořené v blocko programu
    @JsonIgnore @OneToOne(fetch = FetchType.EAGER)                        public Model_HomerInstance private_instance;

    @JsonIgnore @ManyToOne(fetch = FetchType.EAGER)                       public Model_Product product;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_Board_Short_Detail>         boards()           { List<Swagger_Board_Short_Detail>       l = new ArrayList<>();    for( Model_Board m           : boards)         l.add(m.get_short_board());                return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_B_Program_Short_Detail>     b_programs()       { List<Swagger_B_Program_Short_Detail>   l = new ArrayList<>();    for( Model_BProgram m       : b_programs)     l.add(m.get_b_program_short_detail());     return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_C_program_Short_Detail>     c_programs()       { List<Swagger_C_program_Short_Detail>   l = new ArrayList<>();    for( Model_CProgram m       : c_programs)     l.add(m.get_c_program_short_detail());     return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_M_Project_Short_Detail>     m_projects()       { List<Swagger_M_Project_Short_Detail>   l = new ArrayList<>();    for( Model_MProject m       : m_projects)     l.add(m.get_short_m_project());            return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_TypeOfBlock_Short_Detail>   type_of_blocks()   { List<Swagger_TypeOfBlock_Short_Detail> l = new ArrayList<>();    for( Model_TypeOfBlock m     : type_of_blocks) l.add(m.get_type_of_block_short_detail()); return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_TypeOfWidget_Short_Detail>  type_of_widgets()  { List<Swagger_TypeOfWidget_Short_Detail>l = new ArrayList<>();    for( Model_TypeOfWidget m    : type_of_widgets)l.add(m.get_typeOfWidget_short_detail());  return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_Instance_Short_Detail>      instancies()       { List<Swagger_Instance_Short_Detail>    l = new ArrayList<>();    for( Model_HomerInstance m  : Model_HomerInstance.find.where().isNotNull("actual_instance").eq("b_program.project.id", id).findList()) l.add(m.get_instance_short_detail());  return l;}


    @JsonProperty @Transient @ApiModelProperty(required = true) public String product_individual_name() { return product.product_individual_name;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public Long   product_id() { return product.id;}

    @JsonProperty @Transient @ApiModelProperty(required = true) public String tier_name()  { return product.product_type();}


    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Model_ProjectParticipant> participants() {

        List<Model_ProjectParticipant> project_participants = this.participants;

        for(Model_Invitation invitation : invitations){

            Model_Person person = Model_Person.find.where().eq("mail", invitation.mail).findUnique();

            Model_ProjectParticipant project_participant = new Model_ProjectParticipant();

            if(person != null){

                project_participant.person = person;
            }else project_participant.user_email = invitation.mail;

            project_participant.state = Participant_status.invited;

            project_participants.add(project_participant);
        }

        return  project_participants;

    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void notification_project_invitation(Model_Person person, Model_Invitation invitation){

        Model_Person owner = Controller_Security.getPerson();

        new Model_Notification(Notification_importance.normal, Notification_level.info)
                .setText("User ")
                .setObject(Model_Person.class, owner.id, owner.full_name, null, "black", false, true, false, false)
                .setText(" invited you into the project ")
                .setObject(Model_Project.class, this.id, this.name, this.id, "black", false, true, false, false)
                .setText(". Do you accept the invitation?")
                .setButton(Notification_action.accept_project_invitation, invitation.id, "green", "Yes", false, false, false)
                .setButton(Notification_action.reject_project_invitation, invitation.id, "red", "No", false, false, false)
                .send(person);
    }

    @JsonIgnore @Transient
    public void notification_project_invitation_rejected(Model_Person owner){

        Model_Person person = Controller_Security.getPerson();

        new Model_Notification(Notification_importance.normal, Notification_level.info)
                .setText("User ")
                .setObject(Model_Person.class, person.id, person.full_name, null, "black", false, true, false, false)
                .setText(" did not accept your invitation to the project ")
                .setObject(Model_Project.class, this.id, this.name, this.id, "black", false, true, false, false)
                .setText(".")
                .send(owner);
    }

    @JsonIgnore @Transient
    public void notification_project_invitation_accepted(Model_Person owner){

        Model_Person person = Controller_Security.getPerson();

        new Model_Notification(Notification_importance.normal, Notification_level.info)
                .setText("User ")
                .setObject(Model_Person.class, person.id, person.full_name, null, "black", false, true, false, false)
                .setText(" accepted your invitation to the project ")
                .setObject(Model_Project.class, this.id, this.name, this.id, "black", false, true, false, false)
                .setText(".")
                .send(owner);
    }

    @JsonIgnore @Transient
    public void notification_project_participant_change_status(Model_ProjectParticipant participant){

        Model_Person person = Controller_Security.getPerson();

        new Model_Notification(Notification_importance.normal, Notification_level.info)
                .setText("User ")
                .setObject(Model_Person.class, person.id, person.full_name, null, "black", false, true, false, false)
                .setText(" changed your status in project ")
                .setObject(Model_Project.class, this.id, this.name, this.id, "black", false, true, false, false)
                .setText(" to " + participant.state.name() + ". You have different person_permissions now.")
                .send(participant.person);
    }


/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/


    @JsonIgnore private String blob_project_link;

    @JsonIgnore @Override public void save() {

        while(true){ // I need Unique Value
            this.id = UUID.randomUUID().toString();
            this.blob_project_link = product.get_path() + "/projects/" + this.id;
            if (Model_Project.find.byId(this.id) == null) break;
        }

        Model_HomerInstance instance = new Model_HomerInstance();
        instance.cloud_homer_server = Model_HomerServer.find.where().eq("personal_server_name", "Alfa").findUnique();
        instance.virtual_instance = true;
        instance.save();
        this.private_instance = instance;

        super.save();
    }

    @JsonIgnore @Transient
    public String get_path(){
        return  blob_project_link;
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: For all project: User can read project on API: {GET /project) - get Project by logged Person ";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean create_permission()    {  return true;  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission()    {  return ( Model_Project.find.where().eq("participants.person.id", Controller_Security.getPerson().id).where().eq("id", id).findRowCount() > 0) || Controller_Security.getPerson().has_permission("Project_update");  }
    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean read_permission()      {  return ( Model_Project.find.where().eq("participants.person.id", Controller_Security.getPerson().id).where().eq("id", id).findRowCount() > 0) || Controller_Security.getPerson().has_permission("Project_read");}

    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean unshare_permission()   {  return ( Model_ProjectParticipant.find.where().eq("project.id", id).where().eq("person.id", Controller_Security.getPerson().id).where().disjunction().add(Expr.eq("state", Participant_status.owner)).add(Expr.eq("state", Participant_status.admin)).findRowCount() > 0) || Controller_Security.getPerson().has_permission("Project_unshare"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean share_permission ()    {  return ( Model_ProjectParticipant.find.where().eq("project.id", id).where().eq("person.id", Controller_Security.getPerson().id).where().disjunction().add(Expr.eq("state", Participant_status.owner)).add(Expr.eq("state", Participant_status.admin)).findRowCount() > 0) || Controller_Security.getPerson().has_permission("Project_share");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean admin_permission ()    {  return ( Model_ProjectParticipant.find.where().eq("project.id", id).where().eq("person.id", Controller_Security.getPerson().id).where().disjunction().add(Expr.eq("state", Participant_status.owner)).add(Expr.eq("state", Participant_status.admin)).findRowCount() > 0) || Controller_Security.getPerson().has_permission("Project_admin");   }

    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()      {  return ( Model_Project.find.where().eq("participants.person.id", Controller_Security.getPerson().id).where().eq("id", id).findRowCount() > 0) || Controller_Security.getPerson().has_permission("Project_edit");    }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission()    {  return ( Model_ProjectParticipant.find.where().eq("project.id", id).where().eq("person.id", Controller_Security.getPerson().id).where().eq("state", Participant_status.owner).findRowCount() > 0) || Controller_Security.getPerson().has_permission("Project_delete");  }

    public enum permissions{Project_update, Project_read, Project_unshare , Project_share, Project_edit, Project_delete, Project_admin}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
   public static Model.Finder<String,Model_Project> find = new Finder<>(Model_Project.class);

}

