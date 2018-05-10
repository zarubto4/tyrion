package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.ebean.Expr;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.api.libs.json.Json;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.cache.IdsList;
import utilities.enums.*;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.models_update_echo.EchoHandler;
import utilities.notifications.helps_objects.Becki_color;
import utilities.notifications.helps_objects.Notification_Button;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.swagger.output.Swagger_ProjectStats;
import utilities.swagger.output.Swagger_Short_Reference;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_online_status;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import javax.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "Project", description = "Model of Project")
@Table(name="Project")
public class Model_Project extends TaggedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Project.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Product product;

    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_BProgram>              b_programs      = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_CProgram>              c_programs      = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_Library>               libraries       = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_GridProject>           grid_projects   = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_Block>                 blocks          = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_Widget>                widgets         = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_Hardware>              hardware        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_HardwareGroup>         hardware_groups = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_Invitation>            invitations     = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_ProjectParticipant>    participants    = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_Instance>              instances       = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_HomerServer>           servers         = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_GSM>                   gsm             = new ArrayList<>();

    @JsonIgnore @Transient public Swagger_ProjectStats project_stats;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public Swagger_Short_Reference product(){
        try {

            Model_Product product = getProduct();
            if (product != null) {
                product.check_read_permission();
                return new Swagger_Short_Reference(product.id, product.name, product.description);
            } else {
                return null;
            }


        } catch (Result_Error_PermissionDenied e) {
            return null;

        } catch (_Base_Result_Exception e) {
            return null;

        } catch (Exception e) {
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true)
    public boolean active(){
        try {
            return getProduct().active;

        } catch (_Base_Result_Exception e){
            //nothing
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Making List of Model_ProjectParticipant from Model_ProjectParticipant and also from all invitations!
     * @return Model_ProjectParticipant[]
     */
    @JsonProperty @ApiModelProperty(required = true)
    public List<Model_ProjectParticipant> participants() {
        try{
            List<Model_ProjectParticipant> project_participants = new ArrayList<>(this.participants);

            for (Model_Invitation invitation : invitations) {

                    Model_Person person = Model_Person.getByEmail(invitation.email);

                    Model_ProjectParticipant project_participant = new Model_ProjectParticipant();

                    if (person != null) {

                        project_participant.person = person;
                    } else {
                        project_participant.user_email = invitation.email;
                    }

                    project_participant.state = ParticipantStatus.INVITED;

                    if (!project_participants.contains(project_participant)) {
                        project_participants.add(project_participant);
                    }
                }

            return project_participants;
        } catch (_Base_Result_Exception e){
            //nothing
            return new ArrayList<>();
        } catch (Exception e){
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }


/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* GET SQL PARAMETER - CACHE OBJECTS ------------------------------------------------------------------------------------*/

    @JsonIgnore
    public List<UUID> getHardwareIds() {

        if (cache().gets(Model_Hardware.class) == null) {
            cache().add(Model_Hardware.class, Model_Hardware.find.query().where().eq("project.id", id).ne("deleted", true).select("id").findSingleAttributeList());
        }

        return cache().gets(Model_Hardware.class);
    }

    @JsonIgnore
    public List<Model_Hardware> getHardware() {
        try {

            List<Model_Hardware> list = new ArrayList<>();

            for (UUID id : getHardwareIds() ) {
                list.add(Model_Hardware.getById(id));
            }

            return list;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public List<UUID> getCProgramsIds() {

        if (cache().gets(Model_CProgram.class) == null) {
            cache().add(Model_CProgram.class, Model_CProgram.find.query().where().eq("project.id", id).ne("deleted", true).orderBy("UPPER(name) ASC").select("id").findSingleAttributeList());
        }

        return cache().gets(Model_CProgram.class);
    }

    @JsonIgnore
    public List<Model_CProgram> getCPrograms() {
        try {

            List<Model_CProgram> list = new ArrayList<>();

            for (UUID id : getCProgramsIds() ) {
                list.add(Model_CProgram.getById(id));
            }

            return list;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public List<UUID> getLibrariesIds() {

        if (cache().gets(Model_Library.class) == null) {
            cache().add(Model_Library.class, Model_Library.find.query().where().eq("project.id", id).ne("deleted", true).orderBy("UPPER(name) ASC").select("id").findSingleAttributeList());
        }

        return cache().gets(Model_Library.class);
    }

    @JsonIgnore
    public List<Model_Library> getLibraries() {
        try {

            List<Model_Library> list = new ArrayList<>();

            for (UUID id : getLibrariesIds() ) {
                list.add(Model_Library.getById(id));
            }

            return list;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public List<UUID> getBProgramsIds() {

        if (cache().gets(Model_BProgram.class) == null) {
            cache().add(Model_BProgram.class, Model_BProgram.find.query().where().eq("project.id", id).ne("deleted", true).orderBy("UPPER(name) ASC").select("id").findSingleAttributeList());
        }

        return cache().gets(Model_BProgram.class);
    }

    @JsonIgnore
    public List<Model_BProgram> getBPrograms() {
        try {

            List<Model_BProgram> list = new ArrayList<>();

            for (UUID id : getBProgramsIds() ) {
                list.add(Model_BProgram.getById(id));
            }

            return list;


        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public List<UUID> getGridProjectsIds() {

        if (cache().gets(Model_GridProject.class) == null) {
            cache().add(Model_GridProject.class, Model_GridProject.find.query().where().eq("project.id", id).ne("deleted", true).orderBy("UPPER(name) ASC").select("id").findSingleAttributeList());
        }

        return cache().gets(Model_GridProject.class);
    }

    @JsonIgnore
    public List<Model_GridProject> getGridProjects() {
        try {

            List<Model_GridProject> list = new ArrayList<>();

            for (UUID id : getGridProjectsIds() ) {
                list.add(Model_GridProject.getById(id));
            }

            return list;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public List<UUID> getHardwareGroupsIds() {

        if (cache().gets(Model_HardwareGroup.class) == null) {
            cache().add(Model_HardwareGroup.class, Model_HardwareGroup.find.query().where().eq("project.id", id).ne("deleted", true).orderBy("UPPER(name) ASC").select("id").findSingleAttributeList());
        }

        return cache().gets(Model_HardwareGroup.class);
    }

    @JsonIgnore
    public List<Model_HardwareGroup> getHardwareGroups() {
        try {

            List<Model_HardwareGroup> list = new ArrayList<>();

            for (UUID id : getHardwareGroupsIds() ) {
                list.add(Model_HardwareGroup.getById(id));
            }

            return list;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public List<UUID> getWidgetsIds() {

        if (cache().gets(Model_Widget.class) == null) {
            cache().add(Model_Widget.class, Model_Widget.find.query().where().eq("project.id", id).ne("deleted", true).orderBy("UPPER(name) ASC").select("id").findSingleAttributeList());
        }

        return cache().gets(Model_Widget.class);
    }

    @JsonIgnore
    public List<Model_Widget> getWidgets() {
        try {

            List<Model_Widget> list = new ArrayList<>();

            for (UUID id : getWidgetsIds() ) {
                list.add(Model_Widget.getById(id));
            }

            return list;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public List<UUID> getBlocksIds() {

        if (cache().gets(Model_Block.class) == null) {
            cache().add(Model_Block.class, Model_Block.find.query().where().eq("project.id", id).ne("deleted", true).orderBy("UPPER(name) ASC").select("id").findSingleAttributeList());
        }

        return cache().gets(Model_Block.class);
    }

    @JsonIgnore
    public List<Model_Block> getBlocks() {
        try {

            List<Model_Block> list = new ArrayList<>();

            for (UUID id : getBlocksIds() ) {
                list.add(Model_Block.getById(id));
            }

            return list;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public List<UUID> getHomerServerIds() {

        if (cache().gets(Model_HomerServer.class) == null) {
            cache().add(Model_HomerServer.class, Model_HomerServer.find.query().where().eq("project.id", id).ne("deleted", true).orderBy("UPPER(name) ASC").select("id").findSingleAttributeList());
        }

        return cache().gets(Model_HomerServer.class);
    }

    @JsonIgnore
    public List<Model_HomerServer> getHomerServers() {
        try {

            List<Model_HomerServer> list = new ArrayList<>();

            for (UUID id : getHomerServerIds() ) {
                list.add(Model_HomerServer.getById(id));
            }

            return list;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public List<UUID> getInstancesIds() {

        if (cache().gets(Model_Instance.class) == null) {
            cache().add(Model_Instance.class, Model_Instance.find.query().where().eq("project.id", id).ne("deleted", true).orderBy("UPPER(name) ASC").select("id").findSingleAttributeList());
        }

        return cache().gets(Model_Instance.class);
    }

    @JsonIgnore
    public List<Model_Instance> getInstances() {
        try {

            List<Model_Instance> list = new ArrayList<>();

            for (UUID id : getInstancesIds() ) {
                list.add(Model_Instance.getById(id));
            }

            return list;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public UUID getProductId() {

        if (cache().get(Model_Product.class) == null) {
            cache().add(Model_Product.class, Model_Product.find.query().where().eq("projects.id", id).select("id").findSingleAttributeList());
        }

        return cache().get(Model_Product.class);
    }

    @JsonIgnore
    public Model_Product getProduct() {
        try {
            return Model_Product.getByIdWithoutPermission(getProductId());

        }catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE METHOD && VALUES --------------------------------------------------------------------------------------*/


    @JsonIgnore  @Transient  public boolean isParticipant(Model_Person person) {

        return participants.stream().anyMatch(participant -> participant.person.id.equals(person.id));
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void notification_project_invitation(Model_Person person, Model_Invitation invitation) {
        try {
            Model_Person owner = _BaseController.person();

            new Model_Notification()
                    .setImportance(NotificationImportance.NORMAL)
                    .setLevel(NotificationLevel.INFO)
                    .setText(new Notification_Text().setText("User "))
                    .setObject(owner)
                    .setText(new Notification_Text().setText(" invited you into the project "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(". Do you accept the invitation?"))
                    .setButton(new Notification_Button().setAction(NotificationAction.ACCEPT_PROJECT_INVITATION).setPayload(invitation.id.toString()).setColor(Becki_color.byzance_green).setText("Yes"))
                    .setButton(new Notification_Button().setAction(NotificationAction.REJECT_PROJECT_INVITATION).setPayload(invitation.id.toString()).setColor(Becki_color.byzance_red).setText("No"))
                    .send(person);
        } catch (Result_Error_NotFound e){
            logger.error("notification_project_invitation::Result_Error_NotFound::Person Not Found");
        } catch (Exception e){
            logger.internalServerError(e);
        }
    }

    @JsonIgnore @Transient
    public void notification_project_invitation_rejected(Model_Person owner) {
        try {

            Model_Person person = _BaseController.person();

            new Model_Notification()
                    .setImportance(NotificationImportance.NORMAL)
                    .setLevel(NotificationLevel.INFO)
                    .setText(new Notification_Text().setText("User "))
                    .setObject(person)
                    .setText(new Notification_Text().setText(" did not accept your invitation to the project "))
                    .setObject(this)
                    .setText(new Notification_Text().setText("."))
                    .send(owner);
        } catch (Result_Error_NotFound e){
            logger.error("notification_project_invitation_rejected::Result_Error_NotFound::Person Not Found");
        } catch (Exception e){
            logger.internalServerError(e);
        }
    }

    @JsonIgnore @Transient
    public void notification_project_invitation_accepted(Model_Person invitee, Model_Person owner) {

        new Model_Notification()
                .setImportance(NotificationImportance.NORMAL)
                .setLevel(NotificationLevel.INFO)
                .setText(new Notification_Text().setText("User "))
                .setObject(invitee)
                .setText(new Notification_Text().setText(" accepted your invitation to the project "))
                .setObject(this)
                .setText(new Notification_Text().setText("."))
                .send(owner);
    }

    @JsonIgnore @Transient
    public void notification_project_participant_change_status(Model_ProjectParticipant participant) {
        try {

            Model_Person person = _BaseController.person();
            new Model_Notification()
                    .setImportance(NotificationImportance.NORMAL)
                    .setLevel(NotificationLevel.INFO)
                    .setText(new Notification_Text().setText("User "))
                    .setObject(person)
                    .setText(new Notification_Text().setText(" changed your status in project "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" to " + participant.state.name() + ". You have different permissions now."))
                    .send(participant.person);

        } catch (Result_Error_NotFound e){
            logger.error("notification_project_participant_change_status::Result_Error_NotFound::Person Not Found");
        } catch (Exception e){
            logger.internalServerError(e);
        }
    }


/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/


    @JsonProperty @ApiModelProperty(required = false, value = "Its Asynchronous Cached Value and it visible only, when system has cached everything. " +
            "If not, the system automatically searches for all data in a special thread, and when it gets it, it sends them to the client via Websocket. ")
    public Swagger_ProjectStats project_stats(){

        if(!getProduct().active){
            return null;
        }

        if(project_stats != null) {
            return project_stats;
        }

        new Thread(() -> {
            try {

                Swagger_ProjectStats project_stats = new Swagger_ProjectStats();
                project_stats.hardware = getHardware().size();
                project_stats.b_programs = getBProgramsIds().size();
                project_stats.c_programs = getCProgramsIds().size();
                project_stats.libraries = getLibrariesIds().size();
                project_stats.grid_projects = getGridProjectsIds().size();
                project_stats.hardware_groups = getHardwareGroupsIds().size();
                project_stats.widgets = getWidgetsIds().size();
                project_stats.blocks = getBlocksIds().size();
                project_stats.instances = getInstancesIds().size();
                project_stats.servers = getHomerServerIds().size();

                project_stats.hardware_online = 0;
                project_stats.instance_online = 0;
                project_stats.servers_online = 0;

                List<UUID> homer_server_list = Model_Hardware.find.query().where().eq("project.id", id).select("connected_server_id").setDistinct(true).findSingleAttributeList();

                for(UUID homer_id: homer_server_list) {

                    if(homer_id == null) continue;

                    try {
                        Model_HomerServer server = Model_HomerServer.getById(homer_id);
                        if (server.online_state() == NetworkStatus.ONLINE) {
                            WS_Message_Hardware_online_status response = server.device_online_synchronization_ask(Model_Hardware.find.query().where().eq("project.id", id).eq("connected_server_id", homer_id).select("id").findSingleAttributeList());
                            if (response.status.equals("success")) {

                                for (WS_Message_Hardware_online_status.DeviceStatus status : response.hardware_list) {

                                    if (status.online_status) {
                                        ++project_stats.hardware_online;

                                    }

                                    Model_Hardware.cache_status.put(status.uuid, status.online_status);
                                }
                            }

                        }
                    } catch (Exception e) {
                        logger.error("project_stats: Homer Server ID: {} not found", homer_id);
                        // Nothing
                    }
                }

                for(Model_HomerServer server : getHomerServers()) {
                    if(server.online_state() == NetworkStatus.ONLINE) {
                        ++project_stats.servers_online;
                    }
                }

                for(Model_Instance instance : getInstances()) {
                    if(instance.online_state() == NetworkStatus.ONLINE) {
                        ++project_stats.instance_online;
                    }
                }

                this.project_stats = project_stats;
                EchoHandler.addToQueue(new WSM_Echo(Model_Project.class, this.id, this.id));

            } catch (_Base_Result_Exception e) {
                // Nothing

            }
        }).start();

        return null;

    }


/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore private String blob_project_link;

    @JsonIgnore
    public String getPath() {
        return  blob_project_link;
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        financial_permission();

        this.blob_project_link = product.get_path() + "/projects/" + UUID.randomUUID();
        super.save();

        product.cache().add(this.getClass(), id);
    }

    @JsonIgnore @Override public void update() {

        logger.debug("update - updating in database, id: {}",  this.id);

        try {

            if (cache.containsKey(this.id)) cache.replace(this.id, this);

        } catch (Exception e) {
            logger.internalServerError(e);
        }

        super.update();
    }

    @JsonIgnore @Override public boolean delete() {
        logger.debug("delete - deleting from database, id: {} ", this.id);

        getProduct().cache().remove(this.getClass(), id);
        return super.delete();
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception  {
        if (_BaseController.person().has_permission(Permission.Project_read.name())) return;
        if(!product.active) throw new Result_Error_PermissionDenied();
        product.check_create_permission();
    }

    @JsonIgnore @Transient @Override public void check_read_permission() throws _Base_Result_Exception {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_read_" + id)) {
            _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_read_" + id);
        }
        if (_BaseController.person().has_permission(Permission.Project_read.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_Project.find.query().where().eq("participants.person.id", _BaseController.personId()).eq("id", id).findCount() > 0) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_read_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_read_" + id, false);
        throw new Result_Error_PermissionDenied();
    }

    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception   {
        try {
            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_update_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_update_" + id);
            }
            if (_BaseController.person().has_permission(Permission.Project_delete.name())) return;

            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
            if ( Model_Project.find.query().where().eq("participants.person.id", _BaseController.personId()).eq("id", id).findCount() > 0) {
                _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_update_" + id, true);
                return;
            }

            throw new Result_Error_PermissionDenied();

        } catch (_Base_Result_Exception e) {
            // Přidávám do listu false a vracím false
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_update_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }

    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_delete_" + id)) {
            _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_delete_" + id);
        }
        if (_BaseController.person().has_permission(Permission.Project_delete.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if (Model_ProjectParticipant.find.query().where().eq("project.id", id).eq("person.id", _BaseController.personId()).eq("state", ParticipantStatus.OWNER).findCount() > 0) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, false);
        throw new Result_Error_PermissionDenied();
    }

    @JsonIgnore @Transient public void check_share_permission ()throws _Base_Result_Exception {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_unshare_" + id)) {
            _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_unshare_" + id);
        }

        if (_BaseController.person().has_permission(Permission.Project_share.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if (Model_ProjectParticipant.find.query().where().eq("project.id", id).eq("person.id", _BaseController.personId()).disjunction().add(Expr.eq("state", ParticipantStatus.OWNER)).add(Expr.eq("state", ParticipantStatus.ADMIN)).findCount()> 0) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_unshare_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        _BaseController.person().cache_permission(this.getClass().getSimpleName() + "unshare_" + id, false);
        throw new Result_Error_PermissionDenied();
    }

    @JsonIgnore @Transient public void admin_permission () throws _Base_Result_Exception {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (_BaseController.person().has_permission("project_admin_permission_" + id)) if(!_BaseController.person().has_permission("project_admin_permission_"+ id)) throw new Result_Error_PermissionDenied();
        if (_BaseController.person().has_permission(Permission.Project_admin.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if (Model_ProjectParticipant.find.query().where().eq("project.id", id).where().eq("person.id", _BaseController.personId()).where().disjunction().add(Expr.eq("state", ParticipantStatus.OWNER)).add(Expr.eq("state", ParticipantStatus.ADMIN)).findCount()> 0) {
            _BaseController.person().cache_permission("project_admin_permission_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        _BaseController.person().cache_permission("project_admin_permission_" + id, false);
        throw new Result_Error_PermissionDenied();
    }

    @JsonIgnore @Transient public void financial_permission() throws _Base_Result_Exception {
        // TODO Doplnit oprávnění na tvorbu Projektů
        //throw new Result_Error_PermissionDenied("You cannot create project right now. Buy an extension for projects.");
        // return this.product.financial_permission("project");
    }

    public enum Permission { Project_create, Project_update, Project_read, Project_unshare , Project_share, Project_delete, Project_admin }

/* NOTIFICATION ---------------------------------------------------------------------------------------------------------------*/

    public static void becki_person_id_subscribe(UUID person_id) {

        List<Model_Project> list_of_projects = Model_Project.find.query().where().eq("participants.person.id", person_id).disjunction()
                .eq("state", ParticipantStatus.ADMIN)
                .eq("state", ParticipantStatus.MEMBER)
                .eq("state", ParticipantStatus.OWNER)
                .endJunction()
                .findList();

        for (Model_Project project : list_of_projects ) {

            IdsList idlist = token_cache.get(project.id);

            if (idlist == null) idlist = new IdsList();

            if (!idlist.list.contains(person_id)) idlist.list.add(person_id);

            token_cache.put(project.id, idlist);
        }
    }

    public static void becki_person_id_unsubscribe(UUID person_id) {

        List<Model_Project> list_of_projects = Model_Project.find.query().where().eq("participants.person.id", person_id).disjunction()
                .eq("state", ParticipantStatus.ADMIN)
                .eq("state", ParticipantStatus.MEMBER)
                .eq("state", ParticipantStatus.OWNER)
                .endJunction()
                .findList();

        for (Model_Project project : list_of_projects ) {

            IdsList idlist = token_cache.get(project.id);

            if (idlist != null) {
                if (idlist.list.contains(person_id)) {
                    idlist.list.remove(person_id);
                }
            }
        }
    }

    public static List<UUID> get_project_becki_person_ids_list(UUID project_id) {

        if (token_cache == null) {
            logger.error("get_project_becki_person_ids_list - token_cache is null");
            return new ArrayList<>();
        }

        if (project_id == null) {
            logger.error("get_project_becki_person_ids_list - project_id is null");
            return new ArrayList<>();
        }

        IdsList idlist = token_cache.get(project_id);

        try {
            if (idlist == null) {

                idlist = new IdsList();

                Model_Project project = getById(project_id);

                for (Model_ProjectParticipant participant : project.participants) {

                    if (participant.state == ParticipantStatus.INVITED) continue;

                    if (!idlist.list.contains(participant.person.id)) idlist.list.add(participant.person.id);
                }

                token_cache.put(project_id, idlist);
            }
        } catch (Result_Error_NotFound exception){
            // Its Legal Operation
        } catch (Exception exception){
            logger.internalServerError(exception);
        } finally {
            return idlist.list;
        }
    }


/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_Project.class)
    public static Cache<UUID, Model_Project> cache ;

    @CacheField(value = IdsList.class, name = "Model_Project_Person_Ids")
    public static Cache<UUID, IdsList> token_cache;

    public static Model_Project getById(UUID id) throws _Base_Result_Exception {

        Model_Project project = getByIdWithoutPermission(id);

        // Check Permission
        if(project.its_person_operation()) {
            project.check_read_permission();
        }

        return project;
    }

    public static Model_Project getByIdWithoutPermission(UUID id) throws _Base_Result_Exception {

        Model_Project project = cache.get(id);
        if (project == null) {
            project = find.query().where().idEq(id).eq("deleted", false).findOne();
            if (project == null) throw new Result_Error_NotFound(Model_Project.class);
            cache.put(id, project);
        }

        return project;
    }

    public void cache_refresh() {
        if (cache.containsKey(this.id)) {
            this.refresh();
            cache.replace(this.id, this);
        }
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Project> find = new Finder<>(Model_Project.class);
}