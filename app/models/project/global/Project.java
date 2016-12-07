package models.project.global;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.blocko.TypeOfBlock;
import models.compiler.Board;
import models.grid.TypeOfWidget;
import models.notification.Notification;
import models.person.Invitation;
import models.person.Person;
import models.project.b_program.B_Program;
import models.project.b_program.instnace.Homer_Instance;
import models.project.b_program.servers.Cloud_Homer_Server;
import models.project.c_program.C_Program;
import models.project.m_program.M_Project;
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
public class Project extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
                                                             public String name;
                                                             public String description;

    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<B_Program>                b_programs        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<C_Program>                c_programs        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<M_Project>                m_projects        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<TypeOfBlock>              type_of_blocks    = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<TypeOfWidget>             type_of_widgets   = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Board>                    boards            = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Invitation>               invitations       = new ArrayList<>();


    // reference na Fake Instanci - kam připojuji Yody q- pokud nejsou připojení do vlastní instnace vytvořené v blocko programu
    @JsonIgnore @OneToOne(fetch = FetchType.EAGER)  public Homer_Instance private_instance;


    @JsonIgnore @ManyToOne(fetch = FetchType.EAGER) public Product product;

    @JsonIgnore @OneToMany(cascade = CascadeType.ALL, mappedBy = "project") public List<Project_participant> participants = new ArrayList<>();

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_Board_Short_Detail>         boards()           { List<Swagger_Board_Short_Detail>       l = new ArrayList<>();    for( Board m         : boards)         l.add(m.get_short_board());             return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_B_Program_Short_Detail>     b_programs()       { List<Swagger_B_Program_Short_Detail>   l = new ArrayList<>();    for( B_Program m     : b_programs)     l.add(m.get_b_program_short_detail());  return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_C_program_Short_Detail>     c_programs()       { List<Swagger_C_program_Short_Detail>   l = new ArrayList<>();    for( C_Program m     : c_programs)     l.add(m.get_c_program_short_detail());     return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_M_Project_Short_Detail>     m_projects()       { List<Swagger_M_Project_Short_Detail>   l = new ArrayList<>();    for( M_Project m     : m_projects)     l.add(m.get_short_m_project());         return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_TypeOfBlock_Short_Detail>   type_of_blocks()   { List<Swagger_TypeOfBlock_Short_Detail> l = new ArrayList<>();    for( TypeOfBlock m   : type_of_blocks) l.add(m.get_b_program_short_detail());  return l;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_TypeOfWidget_Short_Detail>  type_of_widgets()  { List<Swagger_TypeOfWidget_Short_Detail>l = new ArrayList<>();    for( TypeOfWidget m  : type_of_widgets)l.add(m.get_typeOfWidget_short_detail());return l;}


    @JsonProperty @Transient @ApiModelProperty(required = true) public String product_individual_name() { return product.product_individual_name;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public Long   product_id() { return product.id;}

    @JsonProperty @Transient @ApiModelProperty(required = true) public String tier_name()  { return product.product_type();}


    @JsonProperty @Transient @ApiModelProperty(required = true) public Integer errors() { return 0;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public Integer bugs() { return 0;}


    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Project_participant> participants() {

        List<Project_participant> project_participants = this.participants;

        for(Invitation invitation : invitations){

            Person person = Person.find.where().eq("mail", invitation.mail).findUnique();

            Project_participant project_participant = new Project_participant();

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
    public void notification_project_invitation(Person person, Invitation invitation){

        Person owner = SecurityController.getPerson();

        new Notification(Notification_importance.normal, Notification_level.info)
                .setText("User ")
                .setObject(Person.class, owner.id, owner.full_name, null, "black", false, true, false, false)
                .setText(" invited you into the project ")
                .setObject(Project.class, this.id, this.name, this.id, "black", false, true, false, false)
                .setText(". Do you accept the invitation?")
                .setButton(Notification_action.accept_project_invitation, invitation.id, "green", "Yes", false, false, false)
                .setButton(Notification_action.reject_project_invitation, invitation.id, "red", "No", false, false, false)
                .send(person);
    }

    @JsonIgnore @Transient
    public void notification_project_invitation_rejected(Person owner){

        Person person = SecurityController.getPerson();

        new Notification(Notification_importance.normal, Notification_level.info)
                .setText("User ")
                .setObject(Person.class, person.id, person.full_name, null, "black", false, true, false, false)
                .setText(" did not accept your invitation to the project ")
                .setObject(Project.class, this.id, this.name, this.id, "black", false, true, false, false)
                .setText(".")
                .send(owner);
    }

    @JsonIgnore @Transient
    public void notification_project_invitation_accepted(Person owner){

        Person person = SecurityController.getPerson();

        new Notification(Notification_importance.normal, Notification_level.info)
                .setText("User ")
                .setObject(Person.class, person.id, person.full_name, null, "black", false, true, false, false)
                .setText(" accepted your invitation to the project ")
                .setObject(Project.class, this.id, this.name, this.id, "black", false, true, false, false)
                .setText(".")
                .send(owner);
    }


/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/


    @JsonIgnore private String blob_project_link;

    @JsonIgnore @Override public void save() {

        while(true){ // I need Unique Value
            this.blob_project_link = product.get_path() + "/projects/" + UUID.randomUUID().toString();
            if (Project.find.where().eq("blob_project_link", blob_project_link ).findUnique() == null) break;
        }

        Homer_Instance instance = new Homer_Instance();
        instance.cloud_homer_server = Cloud_Homer_Server.find.where().eq("server_name", "Alfa").findUnique();
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
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission()    {  return ( Project.find.where().eq("participants.person.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_update");  }
    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean read_permission()      {  return ( Project.find.where().eq("participants.person.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_read");}

    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean unshare_permission()   {  return ( Project.find.where().eq("participants.person.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_unshare"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean share_permission ()    {  return ( Project.find.where().eq("participants.person.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_share");   }

    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()      {  return ( Project.find.where().eq("participants.person.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_edit");    }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission()    {  return ( Project.find.where().eq("participants.person.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_delete");  }

    public enum permissions{Project_update, Project_read, Project_unshare , Project_share, Project_edit, Project_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
   public static Model.Finder<String,Project> find = new Finder<>(Project.class);

}

