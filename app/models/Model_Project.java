package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import controllers._BaseController;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.*;
import exceptions.NotFoundException;
import utilities.logger.Logger;
import utilities.model.Echo;
import utilities.model.TaggedModel;
import utilities.model.UnderCustomer;
import utilities.notifications.helps_objects.Becki_color;
import utilities.notifications.helps_objects.Notification_Button;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.permission.Action;
import utilities.permission.Permissions;
import utilities.permission.Permissible;
import utilities.project.ProjectStatsSerializer;
import utilities.swagger.output.Swagger_ProjectParticipant;
import utilities.swagger.output.Swagger_ProjectStats;
import utilities.swagger.output.Swagger_Short_Reference;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@ApiModel(value = "Project", description = "Model of Project")
@Table(name="Project")
@Permissions({ Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE, Action.INVITE, Action.ACTIVATE })
public class Model_Project extends TaggedModel implements Permissible, UnderCustomer, Echo {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Project.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Product product;

    @JsonIgnore @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)  public List<Model_Person> persons = new ArrayList<>();

    @JsonIgnore @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_Role>                  roles           = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_BProgram>              b_programs      = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_CProgram>              c_programs      = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_Library>               libraries       = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_GridProject>           grid_projects   = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_Block>                 blocks          = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_Widget>                widgets         = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_Hardware>              hardware        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_HardwareGroup>         hardware_groups = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_Invitation>            invitations     = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_Instance>              instances       = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_HomerServer>           servers         = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_GSM>                   gsm             = new ArrayList<>();

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonSerialize(using = ProjectStatsSerializer.class, nullsUsing = ProjectStatsSerializer.class) @Transient
    @ApiModelProperty(required = false, value = "Its Asynchronous Cached Value and it visible only, when system has cached everything. " +
            "If not, the system automatically searches for all data in a special thread, and when it gets it, it sends them to the client via Websocket. ")
    public Swagger_ProjectStats project_stats;

    @JsonProperty @ApiModelProperty(required = true)
    public Swagger_Short_Reference product(){
        try {
            Model_Product product = getProduct();
            return new Swagger_Short_Reference(product.id, product.name, product.description);
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true)
    public boolean active(){
        try {
            return getProduct().active;
        } catch (Exception e) {
            logger.internalServerError(e);
            return false;
        }
    }


/* GET SQL PARAMETER - CACHE OBJECTS ------------------------------------------------------------------------------------*/

    /**
     * Making List of Model_ProjectParticipant from Model_ProjectParticipant and also from all invitations!
     * @return Model_ProjectParticipant[]
     */
    @JsonIgnore  @ApiModelProperty(required = true)
    public List<Swagger_ProjectParticipant> participants() {
        try {

            return getPersons().stream().map(person -> {
                Swagger_ProjectParticipant participant = new Swagger_ProjectParticipant();
                participant.id = person.id;
                participant.email = person.email;
                participant.full_name = person.full_name();
                return participant;
            }).collect(Collectors.toList());


        } catch (Exception e){
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public List<UUID> getPersonsIds() {
        if (idCache().gets(Model_Person.class) == null) {
            idCache().add(Model_Person.class, Model_Person.find.query().where().eq("projects.id", id).ne("deleted", true).select("id").findSingleAttributeList());
        }
        return idCache().gets(Model_Person.class) != null ?  idCache().gets(Model_Person.class) : new ArrayList<>();
    }

    @JsonIgnore
    public List<Model_Person> getPersons() {
        return getPersonsIds().stream().map(Model_Person.find::byId).collect(Collectors.toList());
    }

    @JsonIgnore
    public List<UUID> getInvitationIds() {
        if (idCache().gets(Model_Invitation.class) == null) {
            idCache().add(Model_Invitation.class, Model_Invitation.find.query().where().eq("project.id", id).ne("deleted", true).select("id").findSingleAttributeList());
        }
        return idCache().gets(Model_Invitation.class) != null ?  idCache().gets(Model_Invitation.class) : new ArrayList<>();
    }

    @JsonIgnore
    public List<Model_Invitation> getInvitations() {
        return getInvitationIds().stream().map(Model_Invitation.find::byId).collect(Collectors.toList());
    }

    @JsonIgnore
    public List<UUID> getHardwareIds() {
        if (idCache().gets(Model_Hardware.class) == null) {
            idCache().add(Model_Hardware.class, Model_Hardware.find.query().where().eq("project.id", id).ne("deleted", true).select("id").findSingleAttributeList());
        }
        return idCache().gets(Model_Hardware.class) != null ?  idCache().gets(Model_Hardware.class) : new ArrayList<>();
    }

    @JsonIgnore
    public List<Model_Hardware> getHardware() {
        return getHardwareIds().stream().map(Model_Hardware.find::byId).collect(Collectors.toList());
    }

    @JsonIgnore
    public List<UUID> getCProgramsIds() {
        if (idCache().gets(Model_CProgram.class) == null) {
            idCache().add(Model_CProgram.class, Model_CProgram.find.query().where().eq("project.id", id).ne("deleted", true).orderBy("UPPER(name) ASC").select("id").findSingleAttributeList());
        }
        return idCache().gets(Model_CProgram.class) != null ?  idCache().gets(Model_CProgram.class) : new ArrayList<>();
    }

    @JsonIgnore
    public List<Model_CProgram> getCPrograms() {
        return getCProgramsIds().stream().map(Model_CProgram.find::byId).collect(Collectors.toList());
    }

    @JsonIgnore
    public List<UUID> getLibrariesIds() {
        if (idCache().gets(Model_Library.class) == null) {
            idCache().add(Model_Library.class, Model_Library.find.query().where().eq("project.id", id).ne("deleted", true).orderBy("UPPER(name) ASC").select("id").findSingleAttributeList());
        }
        return idCache().gets(Model_Library.class) != null ?  idCache().gets(Model_Library.class) : new ArrayList<>();
    }

    @JsonIgnore
    public List<Model_Library> getLibraries() {
        return getLibrariesIds().stream().map(Model_Library.find::byId).collect(Collectors.toList());
    }

    @JsonIgnore
    public List<UUID> getBProgramsIds() {
        if (idCache().gets(Model_BProgram.class) == null) {
            idCache().add(Model_BProgram.class, Model_BProgram.find.query().where().eq("project.id", id).ne("deleted", true).orderBy("UPPER(name) ASC").select("id").findSingleAttributeList());
        }
        return idCache().gets(Model_BProgram.class) != null ?  idCache().gets(Model_BProgram.class) : new ArrayList<>();
    }

    @JsonIgnore
    public List<Model_BProgram> getBPrograms() {
        return getBProgramsIds().stream().map(Model_BProgram.find::byId).collect(Collectors.toList());
    }

    @JsonIgnore
    public List<UUID> getGridProjectsIds() {
        if (idCache().gets(Model_GridProject.class) == null) {
            idCache().add(Model_GridProject.class, Model_GridProject.find.query().where().eq("project.id", id).ne("deleted", true).orderBy("UPPER(name) ASC").select("id").findSingleAttributeList());
        }
        return idCache().gets(Model_GridProject.class) != null ?  idCache().gets(Model_GridProject.class) : new ArrayList<>();
    }

    @JsonIgnore
    public List<Model_GridProject> getGridProjects() {
        return getGridProjectsIds().stream().map(Model_GridProject.find::byId).collect(Collectors.toList());
    }

    @JsonIgnore
    public List<UUID> getHardwareGroupsIds() {
        if (idCache().gets(Model_HardwareGroup.class) == null) {
            idCache().add(Model_HardwareGroup.class, Model_HardwareGroup.find.query().where().eq("project.id", id).ne("deleted", true).orderBy("UPPER(name) ASC").select("id").findSingleAttributeList());
        }
        return idCache().gets(Model_HardwareGroup.class) != null ?  idCache().gets(Model_HardwareGroup.class) : new ArrayList<>();
    }

    @JsonIgnore
    public List<Model_HardwareGroup> getHardwareGroups() {
        return getHardwareGroupsIds().stream().map(Model_HardwareGroup.find::byId).collect(Collectors.toList());
    }

    @JsonIgnore
    public List<UUID> getWidgetsIds() {
        if (idCache().gets(Model_Widget.class) == null) {
            idCache().add(Model_Widget.class, Model_Widget.find.query().where().eq("project.id", id).ne("deleted", true).orderBy("UPPER(name) ASC").select("id").findSingleAttributeList());
        }
        return idCache().gets(Model_Widget.class) != null ?  idCache().gets(Model_Widget.class) : new ArrayList<>();
    }

    @JsonIgnore
    public List<Model_Widget> getWidgets() {
        return getWidgetsIds().stream().map(Model_Widget.find::byId).collect(Collectors.toList());
    }

    @JsonIgnore
    public List<UUID> getBlocksIds() {
        if (idCache().gets(Model_Block.class) == null) {
            idCache().add(Model_Block.class, Model_Block.find.query().where().eq("project.id", id).ne("deleted", true).orderBy("UPPER(name) ASC").select("id").findSingleAttributeList());
        }
        return idCache().gets(Model_Block.class) != null ?  idCache().gets(Model_Block.class) : new ArrayList<>();
    }

    @JsonIgnore
    public List<Model_Block> getBlocks() {
        return getBlocksIds().stream().map(Model_Block.find::byId).collect(Collectors.toList());
    }

    @JsonIgnore
    public List<UUID> getHomerServerIds() {
        if (idCache().gets(Model_HomerServer.class) == null) {
            idCache().add(Model_HomerServer.class, Model_HomerServer.find.query().where().eq("project.id", id).ne("deleted", true).orderBy("UPPER(name) ASC").select("id").findSingleAttributeList());
        }
        return idCache().gets(Model_HomerServer.class) != null ?  idCache().gets(Model_HomerServer.class) : new ArrayList<>();
    }

    @JsonIgnore
    public List<Model_HomerServer> getHomerServers() {
        return getHomerServerIds().stream().map(Model_HomerServer.find::byId).collect(Collectors.toList());
    }

    @JsonIgnore
    public List<UUID> getInstancesIds() {
        if (idCache().gets(Model_Instance.class) == null) {
            idCache().add(Model_Instance.class, Model_Instance.find.query().where().eq("project.id", id).ne("deleted", true).orderBy("UPPER(name) ASC").select("id").findSingleAttributeList());
        }
        return idCache().gets(Model_Instance.class) != null ?  idCache().gets(Model_Instance.class) : new ArrayList<>();
    }

    @JsonIgnore
    public List<Model_Instance> getInstances() {
        return getInstancesIds().stream().map(Model_Instance.find::byId).collect(Collectors.toList());
    }

    @JsonIgnore
    public Model_Product getProduct() {
        return isLoaded("product") ? product : Model_Product.find.query().where().eq("projects.id", id).findOne();
    }

    @JsonIgnore @Override
    public Model_Customer getCustomer() {
        return getProduct().getCustomer();
    }



/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public boolean isParticipant(Model_Person person) {
        return getPersons().stream().anyMatch(p -> p.id.equals(person.id));
    }

    @JsonIgnore @Override
    public Echo getParent() {
        return null;
    }

    @JsonIgnore @Override
    public Model_Project getProject() {
        return this;
    }

    @JsonIgnore @Override
    public boolean isPublic() {
        return false;
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_Notification notificationInvitation(Model_Person person, Model_Invitation invitation) {
        return new Model_Notification()
                .setImportance(NotificationImportance.NORMAL)
                .setLevel(NotificationLevel.INFO)
                .setText(new Notification_Text().setText("User "))
                .setObject(person)
                .setText(new Notification_Text().setText(" invited you into the project "))
                .setObject(this)
                .setText(new Notification_Text().setText(". Do you accept the invitation?"))
                .setButton(new Notification_Button().setAction(NotificationAction.ACCEPT_PROJECT_INVITATION).setPayload(invitation.id.toString()).setColor(Becki_color.byzance_green).setText("Yes"))
                .setButton(new Notification_Button().setAction(NotificationAction.REJECT_PROJECT_INVITATION).setPayload(invitation.id.toString()).setColor(Becki_color.byzance_red).setText("No"));
    }

    @JsonIgnore
    public Model_Notification notificationInvitationRejected(Model_Person person) {
        return new Model_Notification()
                .setImportance(NotificationImportance.NORMAL)
                .setLevel(NotificationLevel.INFO)
                .setText(new Notification_Text().setText("User "))
                .setObject(person)
                .setText(new Notification_Text().setText(" did not accept your invitation to the project "))
                .setObject(this)
                .setText(new Notification_Text().setText("."));
    }

    @JsonIgnore
    public Model_Notification notificationInvitationAccepted(Model_Person person) {
        return new Model_Notification()
                .setImportance(NotificationImportance.NORMAL)
                .setLevel(NotificationLevel.INFO)
                .setText(new Notification_Text().setText("User "))
                .setObject(person)
                .setText(new Notification_Text().setText(" accepted your invitation to the project "))
                .setObject(this)
                .setText(new Notification_Text().setText("."));
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public String getPath() {
        return getProduct().get_path() + "/projects/" + this.id;
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {
        super.save();
        getProduct().idCache().add(this.getClass(), id);
    }

    @JsonIgnore @Override
    public boolean delete() {
        getProduct().idCache().remove(this.getClass(), id);
        return super.delete();
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.PROJECT;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE, Action.ACTIVATE, Action.INVITE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_Project.class)
    public static CacheFinder<Model_Project> find = new CacheFinder<>(Model_Project.class);
}