package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import exceptions.NotFoundException;
import io.ebean.Expr;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.cache.Cached;
import utilities.enums.*;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.model.UnderProject;
import utilities.models_update_echo.EchoHandler;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.swagger.output.Swagger_Bootloader_Update_program;
import utilities.swagger.output.Swagger_C_Program_Update_program;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.*;

@Entity
@ApiModel(value = "UpdateProcedure", description = "Model of UpdateProcedure")
@Table(name="UpdateProcedure")
public class Model_UpdateProcedure extends BaseModel implements Permissible, UnderProject {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_UpdateProcedure.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, value = "Find description on Model Actual_procedure_State")  public Enum_Update_group_procedure_state state;

                                                    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)  public Model_InstanceSnapshot instance; // For updates under instance snapshot records

    @JsonIgnore @OneToMany(mappedBy="actualization_procedure", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_HardwareUpdate> updates = new ArrayList<>();
    
    @ApiModelProperty(required = true, value = "UNIX time in ms", dataType = "number")  public Date date_of_planing;
    @ApiModelProperty(required = true, value = "UNIX time in ms", dataType = "number")  public Date date_of_finish;

    @ApiModelProperty(required = true)  public UpdateType type_of_update;

    @JsonIgnore public UUID project_id; // For Faster Find

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Cached private Integer size = null;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true, value = "Only if type_of_update constant is MANUALLY_RELEASE_MANAGER")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public FirmwareType firmware_type(){
        try {

            if (type_of_update == UpdateType.MANUALLY_RELEASE_MANAGER || type_of_update == UpdateType.MANUALLY_BY_USER_INDIVIDUAL) {
                return getUpdates().get(0).firmware_type;
            }

            return null;
        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true, value = "Only if type_of_update constant is MANUALLY_RELEASE_MANAGER && firmware_type is FIRMWARE or BACKUP")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Swagger_C_Program_Update_program program(){
        try {

            if(firmware_type() != null && ( firmware_type() == FirmwareType.FIRMWARE || firmware_type() == FirmwareType.BACKUP)) {
                return getUpdates().get(0).c_program_detail();
            }

            return null;

        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true, value = "Only of type_of_update constant is MANUALLY_RELEASE_MANAGER && firmware_type is BOOTLOADER")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Swagger_Bootloader_Update_program bootloader(){
        try {

            if(firmware_type() != null && firmware_type() == FirmwareType.BOOTLOADER) {
                return getUpdates().get(0).bootloader_detail();
            }
            return null;

        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true )
    public Enum_Update_group_procedure_state state () {
        return state;
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public Integer procedure_size_all() {
        try {
            if (size == null) {
                size = Model_HardwareUpdate.find.query().where().eq("actualization_procedure.id", id).findCount();
            }

            return size;

        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public int procedure_size_complete () {
        return  Model_HardwareUpdate.find.query().where()
                .eq("actualization_procedure.id", id).where()
                .disjunction()
                    .add(Expr.eq("state", HardwareUpdateState.COMPLETE))
                    .add(Expr.eq("state", HardwareUpdateState.OBSOLETE))
                .endJunction()
                .findCount();
    }


/* GET Variable short type of objects ----------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public List<UUID> getUpdatesId() {

        if (idCache().gets(Model_HardwareUpdate.class) == null) {
            idCache().add( Model_HardwareUpdate.class, Model_HardwareUpdate.find.query().where().eq("actualization_procedure.id", id).select("id").findSingleAttributeList());
        }

        return idCache().gets(Model_HardwareUpdate.class) != null ?  idCache().gets(Model_HardwareUpdate.class) : new ArrayList<>();
    }

    @JsonIgnore @Transient public List<Model_HardwareUpdate> getUpdates() {
        try {

            List<Model_HardwareUpdate> list = new ArrayList<>();

            for (UUID id : getUpdatesId() ) {
                list.add(Model_HardwareUpdate.find.byId(id));
            }

            return list;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public UUID getInstanceSnapshotId() {

        if (idCache().get(Model_InstanceSnapshot.class) == null) {
            idCache().add(Model_InstanceSnapshot.class, (UUID) Model_InstanceSnapshot.find.query().where().eq("procedures.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_InstanceSnapshot.class);
    }

    @JsonIgnore
    public Model_InstanceSnapshot getInstanceSnapShot() {
        try {
            return Model_InstanceSnapshot.find.byId(getInstanceSnapshotId());
        }catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public UUID get_project_id() {
       return project_id;
    }

    @JsonIgnore @Override
    public Model_Project getProject() throws NotFoundException {
        return Model_Project.find.byId(project_id);
    }

/* EXECUTION METHODS ----------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void update_state() {
        try {

            logger.debug("update_state :: operation");

            // Metoda je vyvolána, pokud chceme synchronizovat Aktualizační proceduru a nějakým způsobem jí označit
            // Třeba kolik procent už je vykonáno

            int all = Model_HardwareUpdate.find.query().where()
                    .eq("actualization_procedure.id", id)
                    .findCount();

            int complete = Model_HardwareUpdate.find.query().where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", HardwareUpdateState.COMPLETE)
                    .findCount();

            if (complete == all) {

                logger.trace("update_state :: All updates are successfully complete (complete == all) ");

                date_of_finish = new Date();
                state = Enum_Update_group_procedure_state.SUCCESSFULLY_COMPLETE;

                new Thread(this::notification_update_procedure_complete).start();

                this.update();
                return;
            }

            int canceled = Model_HardwareUpdate.find.query().where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", HardwareUpdateState.CANCELED)
                    .findCount();


            int override = Model_HardwareUpdate.find.query().where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", HardwareUpdateState.OBSOLETE)
                    .findCount();


            if ((complete + canceled + override) == all) {

                logger.trace("update_state :: All updates are complete (complete + canceled + override) == all ");

                date_of_finish = new Date();
                state = Enum_Update_group_procedure_state.COMPLETE;

                new Thread(this::notification_update_procedure_complete).start();

                this.update();
                return;
            }

            int in_progress = Model_HardwareUpdate.find.query().where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", HardwareUpdateState.IN_PROGRESS)
                    .findCount();

            if (in_progress != 0) {

                logger.trace("update_state :: This Actualization procedure is set to \"in_progess\" state");

                state = Enum_Update_group_procedure_state.IN_PROGRESS;

                notification_update_procedure_progress();

                this.update();
            }

            int critical_error = Model_HardwareUpdate.find.query().where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", HardwareUpdateState.CRITICAL_ERROR)
                    .findCount();

            int not_updated = Model_HardwareUpdate.find.query().where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", HardwareUpdateState.NOT_UPDATED)
                    .findCount();

            if (((critical_error + override + canceled + complete + not_updated) * 1.0 / all) == 1.0) {

                logger.debug("update_state :: All updates are complete (critical_error + override + canceled + complete + not_updated) == all But with Errors!");

                date_of_finish = new Date();
                state = Enum_Update_group_procedure_state.COMPLETE_WITH_ERROR;
                this.update();
                return;
            }

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void cancel_procedure() {

        logger.trace("cancel_procedure :: operation");

        List<Model_HardwareUpdate> list = Model_HardwareUpdate.find.query().where()
                .eq("actualization_procedure.id",id).where()
                .disjunction()
                .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_IS_OFFLINE))
                .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_NEVER_CONNECTED))
                .add(Expr.eq("state", HardwareUpdateState.NOT_YET_STARTED))
                .add(Expr.eq("state", HardwareUpdateState.IN_PROGRESS))
                .add(Expr.isNull("state"))
                .select("id")
                .findList();

        for (Model_HardwareUpdate plan_not_cached : list) {
            Model_HardwareUpdate plan = Model_HardwareUpdate.find.byId(plan_not_cached.id);
            if (plan != null) { // TODO handle not found instead
                plan.state = HardwareUpdateState.CANCELED;
                plan.update();
            }
        }

        state = Enum_Update_group_procedure_state.CANCELED;
        date_of_finish = new Date();

        this.update();
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        logger.debug("save::Creating new Object");

        // If date of plane is mission - its mean do it now!
        if (date_of_planing == null) {
            date_of_planing = new Date();
        }

        // State is always not_start_yet on begging
        this.state = Enum_Update_group_procedure_state.NOT_START_YET;

        // Save Object
        super.save();

        // Call notification about model update
        if (get_project_id() != null) {
            new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_UpdateProcedure.class, get_project_id(), this.id))).start();
        }

        if(instance != null) {
            instance.idCache().add(this.getClass(), this.id);
            // Call notification about model update
            if (get_project_id() != null) {
                new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Instance.class, get_project_id(), this.instance.get_instance_id()))).start();
            }
        }

        // If immidietly
        if (date_of_planing == null || date_of_planing.getTime() < new Date().getTime()) {

            logger.debug("save: Start with update Procedure Immediately");
            this.execute_update_procedure();

        } else {
            logger.debug("save: Set the update Procedure by Time scheduler (not now) ");
            // CustomScheduler.scheduleUpdateProcedure(this); // TODO injection
        }
    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update::Update object Id: {}",  this.id);

        // Update Object
        super.update();

        // Call notification about model update
        if (get_project_id() != null)
        new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_UpdateProcedure.class, get_project_id(), this.id))).start();
    }

    @JsonIgnore @Override
    public boolean delete() {
        logger.internalServerError(new Exception("This object is not legitimate to remove."));
        return false;
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void notification_update_procedure_start() {
        try {

            logger.debug("notification_update_procedure_start :: operation ");

            Model_Notification notification = new Model_Notification();
            notification.setImportance(NotificationImportance.LOW);
            notification.setLevel(NotificationLevel.INFO);

            if (type_of_update == UpdateType.MANUALLY_BY_USER_INDIVIDUAL) {

                notification.setText(new Notification_Text().setText("Your manual update "))
                .setObject(this);

                if (updates.size() == 1) {

                    if (getUpdates().get(0).firmware_type == FirmwareType.FIRMWARE)
                        notification.setText(new Notification_Text().setText(" for Hardware "))
                                    .setObject(getUpdates().get(0).getHardware())
                                    .setText( new Notification_Text().setText(" from Code Editor with Program "))
                                    .setObject(getUpdates().get(0).c_program_version_for_update.get_c_program())
                                    .setText( new Notification_Text().setText(" version "))
                                    .setObject(getUpdates().get(0).c_program_version_for_update)
                                    .setText( new Notification_Text().setText("."));

                    else  if (getUpdates().get(0).firmware_type == FirmwareType.BOOTLOADER)
                        notification.setText(new Notification_Text().setText(" for Hardware "))
                                .setObject(getUpdates().get(0).getHardware())
                                .setText( new Notification_Text().setText(" Bootloader version " + getUpdates().get(0).getBootloader().version_identifier))
                                .setText( new Notification_Text().setText("."));

                } else {
                    notification.setText(new Notification_Text().setText(" for " + getUpdates().size()  + " devices from Code Editor"));
                }

                notification.setText(new Notification_Text().setText(" just begun. We will keep you informed about progress."));

            } else if (type_of_update == UpdateType.MANUALLY_BY_USER_BLOCKO_GROUP || type_of_update == UpdateType.MANUALLY_BY_USER_BLOCKO_GROUP_ON_TIME ) {


                notification.setText(new Notification_Text().setText("Update under Instance "))
                .setObject(Model_InstanceSnapshot.find.byId(getInstanceSnapshotId()).getInstance()); // TODO objekt notifikace

                if (getUpdates().size() == 1) {

                    notification.setText(new Notification_Text().setText(" with one device "));
                    notification.setObject(getUpdates().get(0).getHardware());

                } else {
                    notification.setText(new Notification_Text().setText(" with " + getUpdates().size()  + " devices from Blocko Snapshot "));
                }

                notification.setText(new Notification_Text().setText(" just begun. We will keep you informed about progress."));

            } else {

                throw new Exception( "Update procedure has not set the type_of_update.");
            }

            notification.send_under_project(get_project_id());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void notification_update_procedure_progress() {
        try {

            if (get_project_id() != null)
                new Thread(() -> {
                    logger.debug("notification_update_procedure_progress :: operation ");

                    if (state == Enum_Update_group_procedure_state.COMPLETE || state == Enum_Update_group_procedure_state.SUCCESSFULLY_COMPLETE || state == Enum_Update_group_procedure_state.COMPLETE_WITH_ERROR) {
                        logger.warn("notification_update_procedure_progress ::  called inappropriately (complete) !!!!");
                        return;
                    }

                    Model_Notification notification = new Model_Notification();

                    notification.setNotificationId(UUID.randomUUID())
                            .setImportance(NotificationImportance.LOW)
                            .setLevel(NotificationLevel.INFO)
                            .setText(new Notification_Text().setText("Update of Procedure "))
                            .setObject(this)
                            .setText(new Notification_Text().setText(" is done from " + procedure_size_complete() + "/" + procedure_size_all() + " ."))
                            .send_under_project(get_project_id());

                }).start();

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void notification_update_procedure_final_report() {
        try {

            logger.debug("notification_update_procedure_final_report :: operation ");

            if (get_project_id() != null)
                new Thread(() -> {
                    Model_Notification notification = new Model_Notification();

                    // Single Update
                    if (this.getUpdates().size() == 1 && (type_of_update == UpdateType.MANUALLY_BY_USER_INDIVIDUAL || type_of_update == UpdateType.MANUALLY_BY_USER_BLOCKO_GROUP)) {

                        logger.debug("notification_update_procedure_final_report :: Notification is for single update");

                        // Bootloader
                        if (this.getUpdates().get(0).firmware_type == FirmwareType.BOOTLOADER) {

                            logger.debug("notification_update_procedure_final_report :: Single Update bootloader");

                            Model_BootLoader.notification_bootloader_procedure_success_information_single(this.getUpdates().get(0));
                            return;
                        }

                        // Backup
                        if (this.getUpdates().get(0).firmware_type == FirmwareType.BACKUP) {

                            logger.debug("notification_update_procedure_final_report :: Single Update backup");
                            logger.debug("TODOTOTO TODO TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  ");

                        }
                    }

                    notification.setImportance(NotificationImportance.LOW)
                            .setLevel(NotificationLevel.SUCCESS);


                    int successfully_updated = Model_HardwareUpdate.find.query().where()
                            .eq("actualization_procedure.id", id).where()
                            .eq("state", HardwareUpdateState.COMPLETE)
                            .findCount();

                    int waiting_for_device = Model_HardwareUpdate.find.query().where()
                            .eq("actualization_procedure.id", id).where()
                            .disjunction()
                            .eq("state", HardwareUpdateState.HOMER_SERVER_IS_OFFLINE)
                            .endJunction()
                            .findCount();

                    int error_device = Model_HardwareUpdate.find.query().where()
                            .eq("actualization_procedure.id", id).where()
                            .disjunction()
                            .eq("state", HardwareUpdateState.CRITICAL_ERROR)
                            .eq("state", HardwareUpdateState.NOT_UPDATED)
                            .endJunction()
                            .findCount();

                    notification.setText(new Notification_Text().setText("Update Procedure "))
                            .setObject(this)
                            .setText(new Notification_Text().setText(" is complete. \n"))
                            .setText(new Notification_Text().setText("Number of Total devices for update: " + getUpdates().size() + ". "))
                            .setText(new Notification_Text().setText("Successfully updated: " + successfully_updated + ". "));

                    if (waiting_for_device != 0) {
                        notification.setNewLine();
                        notification.setText(new Notification_Text().setText("-> Unavailable \"offline\" devices: " + waiting_for_device + ". "));
                    }

                    if (error_device != 0) {
                        notification.setNewLine();
                        notification.setText(new Notification_Text().setText("-> Unsuccessful updates " + error_device + ". "));
                    }

                    notification.send_under_project(get_project_id());

                }).start();

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void notification_update_procedure_complete() {
        try {

            if (get_project_id() != null)
            new Thread(() -> {

                logger.warn("notification_update_procedure_complete :: operation ");

                Model_Notification notification =  new Model_Notification();

                notification.setImportance(NotificationImportance.NORMAL)
                            .setLevel(NotificationLevel.SUCCESS);

                // Individual update
                if (type_of_update == UpdateType.MANUALLY_BY_USER_INDIVIDUAL) {

                    if (procedure_size_all() == 1) {
                        return;
                    }

                    if (state == Enum_Update_group_procedure_state.SUCCESSFULLY_COMPLETE) {
                        notification.setText(new Notification_Text().setText("Update Procedure "))
                                .setObject(this)
                                .setText(new Notification_Text().setText(" is complete."));
                    } else {
                        notification.setText(new Notification_Text().setText("Update Procedure "))
                                .setObject(this)
                                .setText(new Notification_Text().setText(" is set as complete but something is wrong. Check Update procedure details."));
                    }

                    notification.send_under_project(get_project_id());
                    return;
                }

                // Možná tady zauvažovat o progressu?? TYRION-599

                if (state == Enum_Update_group_procedure_state.SUCCESSFULLY_COMPLETE) {

                    notification.setText( new Notification_Text().setText("Update Procedure "))
                            .setObject(this)
                            .setText(new Notification_Text().setText(" started at: "))
                            .setDate(this.created)
                            .setText(new Notification_Text().setText(" is done with no errors or other issues."));

                    notification.send_under_project(get_project_id());
                    return;
                }

                if (state == Enum_Update_group_procedure_state.COMPLETE) {

                    notification.setText( new Notification_Text().setText("Update Procedure "))
                                .setObject(this)
                                .setText(new Notification_Text().setText(" started at: "))
                                .setDate(this.created)
                                .setText(new Notification_Text().setText(" is done but something is not right. For more information, please visit the update procedure details."));

                    notification.send_under_project(get_project_id());
                }

            }).start();

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }


/* BLOB DATA -----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.UPDATE_PROCEDURE;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE);
    }

    public enum Permission { UpdateProcedure_crate, UpdateProcedure_read, UpdateProcedure_update, UpdateProcedure_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    /**
     *  Cachování slouží primárně pouze pro sumarizaci updatů. Pomocí getById() lze načíst ActualizationProcedure
     *  která obsahuje HashMapu ID C
     */
    @JsonIgnore private HashMap<String, HardwareUpdateState> cProgram_updater_state = new HashMap<>();

    @JsonIgnore public void change_state(Model_HardwareUpdate plan, HardwareUpdateState state) {
        cProgram_updater_state.put(plan.id.toString(), state);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_UpdateProcedure.class)
    public static CacheFinder<Model_UpdateProcedure> find = new CacheFinder<>(Model_UpdateProcedure.class);
}