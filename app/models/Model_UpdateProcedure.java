package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.ebean.Expr;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.enums.*;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.models_update_echo.EchoHandler;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.swagger.output.Swagger_Bootloader_Update_program;
import utilities.swagger.output.Swagger_C_Program_Update_program;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.*;

@Entity
@ApiModel(value = "UpdateProcedure", description = "Model of UpdateProcedure")
@Table(name="UpdateProcedure")
public class Model_UpdateProcedure extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_UpdateProcedure.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, value = "Find description on Model Actual_procedure_State")  public Enum_Update_group_procedure_state state;

                                                    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)  public Model_InstanceSnapshot instance; // For updates under instance snapshot records

    @JsonIgnore @OneToMany(mappedBy="actualization_procedure", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_HardwareUpdate> updates = new ArrayList<>();
    
    @ApiModelProperty(required = true, value = "UNIX time in ms", dataType = "number")  public Date date_of_planing;
    @ApiModelProperty(required = true, value = "UNIX time in ms", dataType = "number")  public Date date_of_finish;

    @Enumerated(EnumType.STRING)  @ApiModelProperty(required = true)  public UpdateType type_of_update;

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
        }catch (_Base_Result_Exception e){
            //nothing
            return null;
        }catch (Exception e){
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

        }catch (_Base_Result_Exception e){
            //nothing
            return null;
        }catch (Exception e){
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

        }catch (_Base_Result_Exception e){
            //nothing
            return null;
        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true )
    public Enum_Update_group_procedure_state state () {
        try{


        return state;
        }catch (_Base_Result_Exception e){
            //nothing
            return null;
        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public Integer procedure_size_all() {
        try {
            if (size == null) {
                size = Model_HardwareUpdate.find.query().where().eq("actualization_procedure.id", id).findCount();
            }

            return size;

        }catch (_Base_Result_Exception e){
                //nothing
                return null;
        }catch (Exception e){
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
                    .add(Expr.eq("state", HardwareUpdateState.PROHIBITED_BY_CONFIG))
                    .add(Expr.eq("state", HardwareUpdateState.OBSOLETE))
                .endJunction()
                .findCount();
    }


/* GET Variable short type of objects ----------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public List<UUID> getUpdatesId() {

        // TODO všechny  System.out.println odstranit
        System.out.println("Model_UpdateProcedure:: getUpdatesId");
        System.out.println("Model_UpdateProcedure:: getUpdatesId:: actualization_procedure.id: " + id );

        System.out.println("Model_UpdateProcedure:: getUpdatesId:: Co jsem našel bez sort?:" + Model_HardwareUpdate.find.query().where().eq("actualization_procedure.id", id).select("id").findSingleAttributeList());
        System.out.println("Model_UpdateProcedure:: getUpdatesId:: Co jsem našel se  sort?:" + Model_HardwareUpdate.find.query().where().eq("actualization_procedure.id", id).order().asc("date_of_finish").select("id").findSingleAttributeList());
        System.out.println("Model_UpdateProcedure:: getUpdatesId:: Co jsem našel se  sort2?:" + Model_HardwareUpdate.find.query().where().eq("actualization_procedure.id", id).orderBy("date_of_finish").select("id").findSingleAttributeList());

        if (cache().gets(Model_HardwareUpdate.class) == null) {
            System.out.println("Model_UpdateProcedure:: getUpdatesId cache je prázdná! Hledám");
            System.out.println("Model_UpdateProcedure:: Co ukládám do Cache Paměti:: " + Model_HardwareUpdate.find.query().where().eq("actualization_procedure.id", id).select("id").findSingleAttributeList());

            cache().add( Model_HardwareUpdate.class, Model_HardwareUpdate.find.query().where().eq("actualization_procedure.id", id).select("id").findSingleAttributeList());


            System.out.println("Model_UpdateProcedure:: co jsem uložit? " +  cache().gets(Model_HardwareUpdate.class));
        }

        System.out.println("Model_UpdateProcedure:: co vracím? : " + cache().gets(Model_HardwareUpdate.class));

        if(cache().gets(Model_HardwareUpdate.class).isEmpty()) {
            System.out.println("Model_UpdateProcedure:: getUpdatesId:: žádný jsem nenašel v cache paěti ");
        }

        return cache().gets(Model_HardwareUpdate.class);
    }

    @JsonIgnore @Transient public List<Model_HardwareUpdate> getUpdates() {
        try {

            List<Model_HardwareUpdate> list = new ArrayList<>();

            for (UUID id : getUpdatesId() ) {
                list.add(Model_HardwareUpdate.getById(id));
            }

            return list;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public UUID getInstanceSnapshotId() {

        if (cache().get(Model_InstanceSnapshot.class) == null) {
            cache().add(Model_InstanceSnapshot.class, (UUID) Model_InstanceSnapshot.find.query().where().eq("procedures.id", id).select("id").findSingleAttribute());
        }

        return cache().get(Model_InstanceSnapshot.class);
    }

    @JsonIgnore
    public Model_InstanceSnapshot getInstanceSnapShot() {
        try {
            return Model_InstanceSnapshot.getById(getInstanceSnapshotId());
        }catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public UUID get_project_id() {
       return project_id;
    }

    @JsonIgnore @Transient
    public Model_Project get_project() throws _Base_Result_Exception  {
        return  Model_Project.getById(get_project_id());
    }



/* EXECUTION METHODS ----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void execute_update_procedure() {
        logger.warn("execute_update_procedure()");
        Model_Hardware.execute_update_procedure(this);
    }

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

        check_update_permission();

        logger.trace("cancel_procedure :: operation");

        List<Model_HardwareUpdate> list = Model_HardwareUpdate.find.query().where()
                .eq("actualization_procedure.id",id).where()
                .disjunction()
                .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_IS_OFFLINE))
                .add(Expr.eq("state", HardwareUpdateState.INSTANCE_INACCESSIBLE))
                .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_NEVER_CONNECTED))
                .add(Expr.eq("state", HardwareUpdateState.WAITING_FOR_DEVICE))
                .add(Expr.eq("state", HardwareUpdateState.NOT_YET_STARTED))
                .add(Expr.eq("state", HardwareUpdateState.IN_PROGRESS))
                .add(Expr.isNull("state"))
                .select("id")
                .findList();

        for (Model_HardwareUpdate plan_not_cached : list) {
            Model_HardwareUpdate plan = Model_HardwareUpdate.getById(plan_not_cached.id.toString());
            if (plan != null) {
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

        super.refresh();

        // Cache
        cache.put(this.id, this);

        // Call notification about model update
        if (get_project_id() != null) {
            new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_UpdateProcedure.class, get_project_id(), this.id))).start();
        }

        if(instance != null) {
            instance.cache().add(this.getClass(), this.id);
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

        // Cache
        cache.put(id, this);

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
                .setObject(Model_InstanceSnapshot.getById(getInstanceSnapshotId()).instance); // TODO objekt notifikace

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
                            .eq("state", HardwareUpdateState.WAITING_FOR_DEVICE)
                            .eq("state", HardwareUpdateState.HOMER_SERVER_IS_OFFLINE)
                            .eq("state", HardwareUpdateState.INSTANCE_INACCESSIBLE)
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


/* BLOB DATA  --------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override
    public void check_create_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.UpdateProcedure_crate.name())) return;
        get_project().check_update_permission();
    }

    @JsonIgnore @Transient @Override public void check_read_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.UpdateProcedure_read.name())) return;
        get_project().check_read_permission();
    }

    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.UpdateProcedure_update.name())) return;
        get_project().check_update_permission();
    }

    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.UpdateProcedure_delete.name())) return;
        get_project().check_update_permission();
    }

    public enum Permission { UpdateProcedure_crate, UpdateProcedure_read, UpdateProcedure_update, UpdateProcedure_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    /**
     *  Cachování slouží primárně pouze pro sumarizaci updatů. Pomocí getById() lze načíst ActualizationProcedure
     *  která obsahuje HashMapu ID C
     */
    @JsonIgnore private HashMap<String, HardwareUpdateState> cProgram_updater_state = new HashMap<>();
    
    @CacheField(Model_UpdateProcedure.class)
    @JsonIgnore public static Cache<UUID, Model_UpdateProcedure> cache;

    @JsonIgnore public void change_state(Model_HardwareUpdate plan, HardwareUpdateState state) {
        cProgram_updater_state.put(plan.id.toString(), state);
    }

    public static Model_UpdateProcedure getById(String id) {
        return getById(UUID.fromString(id));
    }
    
    public static Model_UpdateProcedure getById(UUID id) {

        Model_UpdateProcedure procedure = cache.get(id);

        if (procedure == null) {

            procedure = Model_UpdateProcedure.find.byId(id);
            if (procedure == null)  throw new Result_Error_NotFound(Model_UpdateProcedure.class);

            cache.put(id, procedure);
        }
        // Check Permission
        if(procedure.its_person_operation()) {
            procedure.check_read_permission();
        }
        return procedure;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_UpdateProcedure> find = new Finder<>(Model_UpdateProcedure.class);
}