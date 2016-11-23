package models.project.global;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.blocko.TypeOfBlock;
import models.compiler.Board;
import models.notification.Notification;
import models.person.Invitation;
import models.person.Person;
import models.project.b_program.B_Program;
import models.project.b_program.instnace.Homer_Instance;
import models.project.b_program.servers.Cloud_Homer_Server;
import models.project.b_program.servers.Private_Homer_Server;
import models.project.c_program.C_Program;
import models.project.c_program.actualization.Actualization_procedure;
import models.project.m_program.M_Project;
import utilities.enums.Notification_action;
import utilities.enums.Notification_importance;
import utilities.enums.Notification_level;
import utilities.swagger.documentationClass.Swagger_Object_detail;
import utilities.swagger.outboundClass.Swagger_Notification_Button;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
public class Project extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
                                                             public String name;
                                                             public String description;

    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Private_Homer_Server>     privateHomerServerList = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<B_Program>                b_programs        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<C_Program>                c_programs        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<M_Project>                m_projects        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<TypeOfBlock>              type_of_blocks    = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Board>                    boards            = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Actualization_procedure>  procedures        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Invitation>               invitations       = new ArrayList<>();


    // reference na Fake Instanci - kam připojuji Yody - pokud nejsou připojení do vlastní instnace vytvořené v blocko programu
    @JsonIgnore @OneToOne(fetch = FetchType.EAGER)  public Homer_Instance private_instance;


    @JsonIgnore @ManyToOne public Product product;

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "owningProjects")  @JoinTable(name = "connected_projects") public List<Person> ownersOfProject = new ArrayList<>();

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String> homers_id()                     { List<String> l = new ArrayList<>();                   for( Private_Homer_Server m    : privateHomerServerList)   l.add(m.id); return l;  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String> boards_id()                     { List<String> l = new ArrayList<>();                   for( Board m                   : boards)                   l.add(m.id); return l;  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_Object_detail> b_programs()     { List<Swagger_Object_detail> l = new ArrayList<>();    for( B_Program m               : b_programs)               l.add(new Swagger_Object_detail(m.name, m.description, m.id)); return l;  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_Object_detail> c_programs()     { List<Swagger_Object_detail> l = new ArrayList<>();    for( C_Program m               : c_programs)               l.add(new Swagger_Object_detail(m.name, m.description, m.id)); return l;  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_Object_detail> m_projects()     { List<Swagger_Object_detail> l = new ArrayList<>();    for( M_Project m               : m_projects)               l.add(new Swagger_Object_detail(m.name, m.description, m.id)); return l;  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String> type_of_blocks_id()             { List<String> l = new ArrayList<>();                   for( TypeOfBlock m             : type_of_blocks)           l.add(m.id); return l;  }

    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String> actual_procedures_id()          { List<String> l = new ArrayList<>();                   for( Actualization_procedure m : procedures)               l.add(m.id); return l;  }



    @JsonProperty @Transient @ApiModelProperty(required = true) public String product_individual_name() { return product.product_individual_name;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public Long   product_id() { return product.id;}

    @JsonProperty @Transient @ApiModelProperty(required = true) public String tier_name()  { return product.product_type();}


    @JsonProperty @Transient @ApiModelProperty(required = true) public Integer errors() { return 0;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public Integer bugs() { return 0;}


    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Project_participant> participants() {

        List<Project_participant> project_participants = new ArrayList<>();

        for(Person person : ownersOfProject){
            Project_participant project_participant = new Project_participant();
            project_participant.id = person.id;
            project_participant.user_email = person.mail;
            project_participant.full_name = person.full_name;
            project_participant.state = "Project Member"; // TODO dá se tu vymyslet mnohem lepší a promakanější stavy
            project_participant.pending_invitation = false;
            project_participants.add(project_participant);
        }

        for(Invitation invitation : invitations){
            Project_participant project_participant = new Project_participant();
            project_participant.user_email = invitation.mail;
            project_participant.state      = "Waiting for decision"; // TODO dá se tu vymyslet mnohem lepší a promakanější stavy
            project_participant.pending_invitation = true;
            project_participants.add(project_participant);
        }

        return  project_participants;

    }


/* POMOCNÉ TŘÍDY ---------------------------------------------------------------------------------------------------------*/


    public class Project_participant {
        @JsonProperty @Transient @ApiModelProperty(required = false, value = "Only if the user is already part of the project") public String full_name;
        @JsonProperty @Transient @ApiModelProperty(required = true, value = "Its in object always") public String user_email;
        @JsonProperty @Transient @ApiModelProperty(required = false, value = "Only if the user is already part of the project (for click operations)")  public String id;
        @JsonProperty @Transient @ApiModelProperty(required = true, value = "Its in object always")  public String state;
        @JsonProperty @Transient @ApiModelProperty(required = true, value = "Its in object always")  public boolean pending_invitation;
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void notification_project_invitation(Person person, Invitation invitation){

        Person owner = SecurityController.getPerson();

        List<Person> receivers = new ArrayList<>();
        receivers.add(person);

        new Notification(Notification_importance.normal, Notification_level.info)
                .setText("User ", "black", false, false, false)
                .setObject(Person.class, owner.id, owner.full_name, null, "black", false, true, false, false)
                .setText(" invited you into the project ", "black", false, false, false)
                .setObject(Project.class, this.id, this.name, this.id, "black", false, true, false, false)
                .setText(". Do you accept the invitation?", "black", false, false, false)
                .setButtons(new Swagger_Notification_Button("Yes", Notification_action.accept_project_invitation, "green", invitation.id), new Swagger_Notification_Button("No", Notification_action.reject_project_invitation, "red", invitation.id))
                .send(receivers);
    }

    @JsonIgnore @Transient
    public void notification_project_invitation_rejected(Person owner){

        Person person = SecurityController.getPerson();
        List<Person> receivers = new ArrayList<>();
        receivers.add(owner);

        new Notification(Notification_importance.normal, Notification_level.info)
                .setText("User ", "black", false, false, false)
                .setObject(Person.class, person.id, person.full_name, null, "black", false, true, false, false)
                .setText(" did not accept your invitation to the project ", "black", false, false, false)
                .setObject(Project.class, this.id, this.name, this.id, "black", false, true, false, false)
                .setText(".", "black", false, false, false)
                .send(receivers);
    }

    @JsonIgnore @Transient
    public void notification_project_invitation_accepted(Person owner){

        Person person = SecurityController.getPerson();
        List<Person> receivers = new ArrayList<>();
        receivers.add(owner);

        new Notification(Notification_importance.normal, Notification_level.info)
                .setText("User ", "black", false, false, false)
                .setObject(Person.class, person.id, person.full_name, null, "black", false, true, false, false)
                .setText(" accepted your invitation to the project ", "black", false, false, false)
                .setObject(Project.class, this.id, this.name, this.id, "black", false, true, false, false)
                .setText(".", "black", false, false, false)
                .send(receivers);
    }


/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/


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

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: For all project: User can read project on API: {GET /project/project) - get Project by logged Person ";


    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean create_permission()    {  return true;  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission()    {  return ( Project.find.where().eq("ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_update");  }
    @JsonIgnore   @Transient @ApiModelProperty(required = true) public boolean read_permission()      {  return ( Project.find.where().eq("ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_read");}

    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean unshare_permission()   {  return ( Project.find.where().eq("ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_unshare"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean share_permission ()    {  return ( Project.find.where().eq("ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_share");   }

    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()      {  return ( Project.find.where().eq("ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_edit");    }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission()    {  return ( Project.find.where().eq("ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_delete");  }

    public enum permissions{Project_update, Project_read, Project_unshare , Project_share, Project_edit, Project_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
   public static Model.Finder<String,Project> find = new Finder<>(Project.class);

}

