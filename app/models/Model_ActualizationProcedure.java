package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.BaseController;
import io.ebean.Expr;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.enums.*;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.models_update_echo.EchoHandler;
import utilities.notifications.helps_objects.Notification_Text;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.*;

@Entity
@ApiModel(value = "ActualizationProcedure", description = "Model of ActualizationProcedure")
@Table(name="ActualizationProcedure")
public class Model_ActualizationProcedure extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_ActualizationProcedure.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, value = "Find description on Model Actual_procedure_State")  public Enum_Update_group_procedure_state state;

                                                    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)  public Model_InstanceSnapshot instance; // For updates under instance snapshot records

    @JsonIgnore @OneToMany(mappedBy="actualization_procedure", cascade = CascadeType.ALL, fetch = FetchType.LAZY) @OrderBy("date_of_finish DESC") public List<Model_CProgramUpdatePlan> updates = new ArrayList<>();
    
    @ApiModelProperty(required = true, value = "UNIX time in ms")  public Date date_of_planing;
    @ApiModelProperty(required = true, value = "UNIX time in ms")  public Date date_of_finish;

    @Enumerated(EnumType.STRING)  @ApiModelProperty(required = true)  public UpdateType type_of_update;

    @JsonIgnore public UUID project_id;

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    // For Faster reload
    @Transient @JsonIgnore @Cached  public Integer cache_procedure_size_all;
    @Transient @JsonIgnore @Cached  public UUID cache_instance_snapshot_id;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true ) public Enum_Update_group_procedure_state state () {
        return state;
    }

    @JsonProperty @Transient @ApiModelProperty(required = true, readOnly = true)
    public Integer procedure_size_all() {

        if (cache_procedure_size_all == null) {
            cache_procedure_size_all = Model_CProgramUpdatePlan.find.query().where()
                    .eq("actualization_procedure.id", id)
                    .findCount();
        }

        return cache_procedure_size_all;
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public Integer procedure_size_complete () {
        return  Model_CProgramUpdatePlan.find.query().where()
                .eq("actualization_procedure.id", id).where()
                .eq("state", Enum_CProgram_updater_state.complete)
                .findCount();
    }


/* GET Variable short type of objects ----------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID getInstanceSnapshotId() {
        try {
            if (cache_instance_snapshot_id == null) {
                Model_InstanceSnapshot snapshot = Model_InstanceSnapshot.find.query().where()
                        .eq("procedures.id", id)
                        .select("id")
                        .findOne();

                if (snapshot != null) {
                    cache_instance_snapshot_id = snapshot.id;
                }
            }

            return cache_instance_snapshot_id;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }
    @JsonIgnore
    public void update_state() {
        try {

            logger.debug("update_state :: operation");

            // Metoda je vyvolána, pokud chceme synchronizovat Aktualizační proceduru a nějakým způsobem jí označit
            // Třeba kolik procent už je vykonáno

            int all = Model_CProgramUpdatePlan.find.query().where()
                    .eq("actualization_procedure.id", id)
                    .findCount();

            int complete = Model_CProgramUpdatePlan.find.query().where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", Enum_CProgram_updater_state.complete)
                    .findCount();

            if (complete == all) {

                logger.trace("update_state :: All updates are successfully complete (complete == all) ");

                date_of_finish = new Date();
                state = Enum_Update_group_procedure_state.successful_complete;

                new Thread(this::notification_update_procedure_complete).start();

                this.update();
                return;
            }

            int canceled = Model_CProgramUpdatePlan.find.query().where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", Enum_CProgram_updater_state.canceled)
                    .findCount();


            int override = Model_CProgramUpdatePlan.find.query().where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", Enum_CProgram_updater_state.overwritten)
                    .findCount();


            if ((complete + canceled + override) == all) {

                logger.trace("update_state :: All updates are complete (complete + canceled + override) == all ");

                date_of_finish = new Date();
                state = Enum_Update_group_procedure_state.complete;

                new Thread(this::notification_update_procedure_complete).start();

                this.update();
                return;
            }

            int in_progress = Model_CProgramUpdatePlan.find.query().where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", Enum_CProgram_updater_state.in_progress)
                    .findCount();

            if (in_progress != 0) {

                logger.trace("update_state :: This Actualization procedure is set to \"in_progess\" state");

                state = Enum_Update_group_procedure_state.in_progress;

               notification_update_procedure_progress();

                this.update();
            }

            int critical_error = Model_CProgramUpdatePlan.find.query().where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", Enum_CProgram_updater_state.critical_error)
                    .findCount();

            int not_updated = Model_CProgramUpdatePlan.find.query().where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", Enum_CProgram_updater_state.not_updated)
                    .findCount();

            if (((critical_error + override + canceled + complete + not_updated) * 1.0 / all) == 1.0) {

                logger.debug("update_state :: All updates are complete (critical_error + override + canceled + complete + not_updated) == all But with Errors!");

                date_of_finish = new Date();
                state = Enum_Update_group_procedure_state.complete_with_error;
                this.update();
                return;
            }

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore @Transient
    public void cancel_procedure() {

       logger.trace("cancel_procedure :: operation");

       List<Model_CProgramUpdatePlan> list = Model_CProgramUpdatePlan.find.query().where()
                                            .eq("actualization_procedure.id",id).where()
                                                .disjunction()
                                                    .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline))
                                                    .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                                                    .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_never_connected))
                                                    .add(Expr.eq("state", Enum_CProgram_updater_state.waiting_for_device))
                                                    .add(Expr.eq("state", Enum_CProgram_updater_state.not_start_yet))
                                                    .add(Expr.eq("state", Enum_CProgram_updater_state.in_progress))
                                                    .add(Expr.isNull("state"))
                                            .select("id")
                                            .findList();

       for (Model_CProgramUpdatePlan plan_not_cached : list) {
           Model_CProgramUpdatePlan plan = Model_CProgramUpdatePlan.getById(plan_not_cached.id.toString());
           if (plan != null) {
               plan.state = Enum_CProgram_updater_state.canceled;
               plan.update();
           }
       }

       state = Enum_Update_group_procedure_state.canceled;
       date_of_finish = new Date();

       this.update();
    }

    @JsonIgnore
    public UUID get_project_id() {
       return project_id;
    }

    @JsonIgnore @Transient
    public void execute_update_procedure() {
        Model_Hardware.execute_update_procedure(this);
    }

/* SERVER WEBSOCKET ----------------------------------------------------------------------------------------------------*/


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        logger.trace("save :: Creating new Object");

        if (date_of_planing == null) {
            date_of_planing = new Date();
        }

        this.state = Enum_Update_group_procedure_state.not_start_yet;

        // ORM
        super.save();

        // Cache
        cache.put(this.id, this);

        // Call notification about model update
        if (get_project_id() != null) {
            new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_ActualizationProcedure.class, get_project_id(), this.id))).start();
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

        logger.trace("update :: Update object Id: " + this.id);

        //ORM
        super.update();

        // Cache
        cache.put(id, this);

        // Call notification about model update
        if (get_project_id() != null)
        new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_ActualizationProcedure.class, get_project_id(), this.id))).start();
    }

    @JsonIgnore @Override
    public boolean delete() {
        logger.internalServerError(new Exception("This object is not legitimate to remove."));
        return false;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
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

                    if (updates.get(0).firmware_type == Enum_Firmware_type.FIRMWARE)
                        notification.setText(new Notification_Text().setText(" for Board "))
                                    .setObject(updates.get(0).get_board())
                                    .setText( new Notification_Text().setText(" from Code Editor with Program "))
                                    .setObject(updates.get(0).c_program_version_for_update.get_c_program())
                                    .setText( new Notification_Text().setText(" version "))
                                    .setObject(updates.get(0).c_program_version_for_update)
                                    .setText( new Notification_Text().setText("."));

                    else  if (updates.get(0).firmware_type == Enum_Firmware_type.BOOTLOADER)
                        notification.setText(new Notification_Text().setText(" for Board "))
                                .setObject(updates.get(0).get_board())
                                .setText( new Notification_Text().setText(" Bootloader version " + updates.get(0).get_bootloader().version_identifier))
                                .setText( new Notification_Text().setText("."));

                } else {
                    notification.setText(new Notification_Text().setText(" for " + updates.size()  + " devices from Code Editor"));
                }

                notification.setText(new Notification_Text().setText(" just begun. We will keep you informed about progress."));

            } else if (type_of_update == UpdateType.MANUALLY_BY_USER_BLOCKO_GROUP || type_of_update == UpdateType.MANUALLY_BY_USER_BLOCKO_GROUP_ON_TIME ) {


                notification.setText(new Notification_Text().setText("Update under Instance "))
                .setObject(Model_InstanceSnapshot.getById(getInstanceSnapshotId()).instance); // TODO objekt notifikace

                if (updates.size() == 1) {

                    notification.setText(new Notification_Text().setText(" with one device "));
                    notification.setObject(updates.get(0).get_board());

                } else {
                    notification.setText(new Notification_Text().setText(" with " + updates.size()  + " devices from Blocko Snapshot "));
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

    @JsonIgnore @Transient
    public void notification_update_procedure_progress() {
        try {

            if (get_project_id() != null)
                new Thread( () -> {
                    logger.debug("notification_update_procedure_progress :: operation ");

                    if (state == Enum_Update_group_procedure_state.complete || state == Enum_Update_group_procedure_state.successful_complete || state == Enum_Update_group_procedure_state.complete_with_error) {
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

    @JsonIgnore @Transient
    public void notification_update_procedure_final_report() {
        try {

            logger.debug("notification_update_procedure_final_report :: operation ");

            if (get_project_id() != null)
                new Thread( () -> {
                    Model_Notification notification = new Model_Notification();

                    // Single Update
                    if (this.updates.size() == 1 && (type_of_update == UpdateType.MANUALLY_BY_USER_INDIVIDUAL || type_of_update == UpdateType.MANUALLY_BY_USER_BLOCKO_GROUP)) {

                        logger.debug("notification_update_procedure_final_report :: Notification is for single update");

                        // Bootloader
                        if (this.updates.get(0).firmware_type == Enum_Firmware_type.BOOTLOADER) {

                            logger.debug("notification_update_procedure_final_report :: Single Update bootloader");

                            Model_BootLoader.notification_bootloader_procedure_success_information_single(this.updates.get(0));
                            return;
                        }

                        // Backup
                        if (this.updates.get(0).firmware_type == Enum_Firmware_type.BACKUP) {

                            logger.debug("notification_update_procedure_final_report :: Single Update backup");
                            logger.debug("TODOTOTO TODO TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  ");

                        }


                    }

                    notification.setImportance(NotificationImportance.LOW)
                            .setLevel(NotificationLevel.SUCCESS);


                    int successfully_updated = Model_CProgramUpdatePlan.find.query().where()
                            .eq("actualization_procedure.id", id).where()
                            .eq("state", Enum_CProgram_updater_state.complete)
                            .findCount();

                    int waiting_for_device = Model_CProgramUpdatePlan.find.query().where()
                            .eq("actualization_procedure.id", id).where()
                            .disjunction()
                            .eq("state", Enum_CProgram_updater_state.waiting_for_device)
                            .eq("state", Enum_CProgram_updater_state.homer_server_is_offline)
                            .eq("state", Enum_CProgram_updater_state.instance_inaccessible)
                            .endJunction()
                            .findCount();

                    int error_device = Model_CProgramUpdatePlan.find.query().where()
                            .eq("actualization_procedure.id", id).where()
                            .disjunction()
                            .eq("state", Enum_CProgram_updater_state.critical_error)
                            .eq("state", Enum_CProgram_updater_state.not_updated)
                            .endJunction()
                            .findCount();

                    notification.setText(new Notification_Text().setText("Update Procedure "))
                            .setObject(this)
                            .setText(new Notification_Text().setText(" is complete. \n"))
                            .setText(new Notification_Text().setText("Number of Total devices for update: " + updates.size() + ". "))
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

    @JsonIgnore @Transient
    public void notification_update_procedure_complete() {
        try {

            if (get_project_id() != null)
            new Thread( () -> {

                logger.warn("notification_update_procedure_complete :: operation ");

                Model_Notification notification =  new Model_Notification();

                notification.setImportance(NotificationImportance.NORMAL)
                            .setLevel(NotificationLevel.SUCCESS);

                // Individual update
                if (type_of_update == UpdateType.MANUALLY_BY_USER_INDIVIDUAL) {

                    if (procedure_size_all() == 1) {
                        return;
                    }

                    if (state == Enum_Update_group_procedure_state.successful_complete) {
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

                if (state == Enum_Update_group_procedure_state.successful_complete) {

                    notification.setText( new Notification_Text().setText("Update Procedure "))
                            .setObject(this)
                            .setText(new Notification_Text().setText(" started at: "))
                            .setDate(this.created)
                            .setText(new Notification_Text().setText(" is done with no errors or other issues."));

                    notification.send_under_project(get_project_id());
                    return;
                }

                if (state == Enum_Update_group_procedure_state.complete) {

                    notification.setText( new Notification_Text().setText("Update Procedure "))
                                .setObject(this)
                                .setText(new Notification_Text().setText(" started at: "))
                                .setDate(this.created)
                                .setText(new Notification_Text().setText(" is done but something is not right. For more information, please visit the update procedure details."));

                    notification.send_under_project(get_project_id());
                    return;

                }

            }).start();

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }


/* BLOB DATA  --------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient   public static final String read_permission_docs   = "User can read Actualization_procedure if they have ID of Actualization_procedure";

    @JsonIgnore @Transient  public boolean read_permission()      {
        return Model_Project.getById(project_id).read_permission() || BaseController.person().has_permission("Actualization_procedure_read");
    }

    @JsonProperty @Transient  public boolean edit_permission()      {
        return Model_Project.getById(project_id).update_permission() || BaseController.person().has_permission("Actualization_procedure_edit");
    }

    public enum permissions{Actualization_procedure_read, Actualization_procedure_edit}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    /**
     *  Cachování slouží primárně pouze pro sumarizaci updatů. Pomocí getById() lze načíst ActualizationProcedure
     *  která obsahuje HashMapu ID C
     */
    @JsonIgnore private HashMap<String, Enum_CProgram_updater_state> cProgram_updater_state = new HashMap<>();
    
    @CacheField(value = Model_ActualizationProcedure.class, timeToIdle = 600)
    @JsonIgnore public static Cache<UUID, Model_ActualizationProcedure> cache;

    @JsonIgnore public void change_state(Model_CProgramUpdatePlan plan, Enum_CProgram_updater_state state) {
        cProgram_updater_state.put(plan.id.toString(), state);
    }

    public static Model_ActualizationProcedure getById(String id) {
        return getById(UUID.fromString(id));
    }
    
    public static Model_ActualizationProcedure getById(UUID id) {

        Model_ActualizationProcedure procedure = cache.get(id);

        if (procedure == null) {

            procedure = Model_ActualizationProcedure.find.byId(id);
            if (procedure == null) return null;

            cache.put(id, procedure);
        }

        return procedure;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_ActualizationProcedure> find = new Finder<>(Model_ActualizationProcedure.class);
}