package models;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.enums.*;
import utilities.logger.Class_Logger;
import utilities.models_update_echo.Update_echo_handler;
import utilities.notifications.helps_objects.Notification_Text;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;

import javax.persistence.*;
import java.util.*;

@Entity
@ApiModel(value = "ActualizationProcedure", description = "Model of ActualizationProcedure")
public class Model_ActualizationProcedure extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_ActualizationProcedure.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                                                               @Id  public String id; // Vlastní id je přidělováno

    @ApiModelProperty(required = true, value = "Find description on Model Actual_procedure_State")  public Enum_Update_group_procedure_state state;

                                                    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)  public Model_HomerInstanceRecord homer_instance_record; // For updates under instance snapshot records

    @OneToMany(mappedBy="actualization_procedure", cascade = CascadeType.ALL) @OrderBy("date_of_create DESC") public List<Model_CProgramUpdatePlan> updates = new ArrayList<>();

    @ApiModelProperty(required = true, value = "UNIX time in ms")  public Date date_of_create;
    @ApiModelProperty(required = true, value = "UNIX time in ms")  public Date date_of_planing;
    @ApiModelProperty(required = true, value = "UNIX time in ms")  public Date date_of_finish;

    @Enumerated(EnumType.STRING)  @ApiModelProperty(required = true)  public Enum_Update_type_of_update type_of_update;


    // Temporary variable for reasons of incompleteness when calling save() and also for acceleration and for reduce database load.
    // - is only added when the new Model_ActualizationProcedure object is created and it does not yet contain any references that project_id could find throw database search
    @JsonIgnore @Transient public String project_id;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true ) public Enum_Update_group_procedure_state state (){
        return state;
    }

    @JsonProperty @Transient @ApiModelProperty(required = true, readOnly = true)
    public Integer procedure_size_all(){
        return   Model_CProgramUpdatePlan.find.where()
                .eq("actualization_procedure.id", id)
                .findRowCount();
    }

    @JsonProperty @Transient @ApiModelProperty(required = true, readOnly = true)
    public Integer procedure_size_complete (){
        return  Model_CProgramUpdatePlan.find.where()
                .eq("actualization_procedure.id", id).where()
                .eq("state", Enum_CProgram_updater_state.complete)
                .findRowCount();
    }


/* GET Variable short type of objects ----------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void update_state(){
        try {

            terminal_logger.debug("update_state :: operation");

            // Metoda je vyvolána, pokud chceme synchronizovat Aktualizační proceduru a nějakým způsobem jí označit
            // Třeba kolik procent už je vykonáno

            int all = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id)
                    .findRowCount();

            int complete = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", Enum_CProgram_updater_state.complete)
                    .findRowCount();

            if (complete == all) {

                terminal_logger.trace("update_state :: All updates are successfully complete (complete == all) ");

                date_of_finish = new Date();
                state = Enum_Update_group_procedure_state.successful_complete;

                new Thread(this::notification_update_procedure_complete).start();

                this.update();
                return;
            }

            int canceled = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", Enum_CProgram_updater_state.canceled)
                    .findRowCount();


            int override = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", Enum_CProgram_updater_state.overwritten)
                    .findRowCount();


            if ((complete + canceled + override) == all) {

                terminal_logger.trace("update_state :: All updates are complete (complete + canceled + override) == all ");

                date_of_finish = new Date();
                state = Enum_Update_group_procedure_state.complete;

                new Thread(this::notification_update_procedure_complete).start();

                this.update();
                return;
            }

            int in_progress = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", Enum_CProgram_updater_state.in_progress)
                    .findRowCount();

            if (in_progress != 0) {

                terminal_logger.trace("update_state :: This Actualization procedure is set to \"in_progess\" state");

                state = Enum_Update_group_procedure_state.in_progress;

                new Thread(this::notification_update_procedure_progress).start();

                this.update();
            }

            int critical_error = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", Enum_CProgram_updater_state.critical_error)
                    .findRowCount();

            int not_updated = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", Enum_CProgram_updater_state.not_updated)
                    .findRowCount();

            if (((critical_error + override + canceled + complete + not_updated) * 1.0 / all) == 1.0) {

                terminal_logger.debug("update_state :: All updates are complete (critical_error + override + canceled + complete + not_updated) == all But with Errors!");

                date_of_finish = new Date();
                state = Enum_Update_group_procedure_state.complete_with_error;
                this.update();
                return;
            }

        }catch (Exception e){
            terminal_logger.internalServerError("update_state:",e);
        }
    }

    @JsonIgnore @Transient
    public void cancel_procedure(){

       terminal_logger.debug("cancel_procedure :: operation");

       List<Model_CProgramUpdatePlan> list = Model_CProgramUpdatePlan.find.where()
                                            .eq("actualization_procedure.id",id).where()
                                                .disjunction()
                                                    .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline       ))
                                                    .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                                                    .add(Expr.isNull("state"))
                                            .findList();

       for(Model_CProgramUpdatePlan plan : list) {
           plan.state = Enum_CProgram_updater_state.canceled;
           plan.update();
       }

        state = Enum_Update_group_procedure_state.canceled;

        this.update();
    }

    @JsonIgnore @Transient
    public String get_project_id(){

        if(project_id != null) return project_id;

        if(type_of_update == Enum_Update_type_of_update.MANUALLY_BY_USER_BLOCKO_GROUP || type_of_update == Enum_Update_type_of_update.MANUALLY_BY_USER_BLOCKO_GROUP_ON_TIME ) {
            return Model_Project.find.where().eq("b_programs.instance.instance_history.procedures.id", id).select("id").findUnique().id;
        }

        if(type_of_update == Enum_Update_type_of_update.MANUALLY_BY_USER_INDIVIDUAL){
            System.out.println("Hledám Project ID pod aktualizačním plánem ID " + id);
            return Model_Project.find.where().eq("boards.c_program_update_plans.actualization_procedure.id", id).findUnique().id;
        }

        return null;
    }


    public void execute_update_procedure(){
        Model_Board.execute_update_procedure(this);
    }

/* SERVER WEBSOCKET ----------------------------------------------------------------------------------------------------*/


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");

        date_of_create = new Date();

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_ActualizationProcedure.find.byId(this.id) == null) break;
        }

        this.state = Enum_Update_group_procedure_state.not_start_yet;

        // ORM
        super.save();

        // Cache
        cache.put(id, this);

        // Call notification about model update
        new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_HomerInstance.class, get_project_id(), this.id))).start();
    }

    @JsonIgnore @Override
    public void update(){

        terminal_logger.debug("update :: Update object Id: " + this.id);

        //ORM
        super.update();

        // Cache
        cache.put(id, this);

        // Call notification about model update
        new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_ActualizationProcedure.class, get_project_id(), this.id))).start();
    }

    @JsonIgnore @Override
    public void delete(){
        terminal_logger.internalServerError(new Exception("This object is not legitimate to remove."));
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/


/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void notification_update_procedure_start(){
        try {


            terminal_logger.debug("notification_update_procedure_start :: operation ");

            Model_Notification notification = new Model_Notification();

            notification.setImportance(Enum_Notification_importance.low)
                        .setLevel(Enum_Notification_level.info);

            if(type_of_update == Enum_Update_type_of_update.MANUALLY_BY_USER_INDIVIDUAL){
                notification.setText(new Notification_Text().setText("Your manual update "))
                .setObject(this);

                if(updates.size() == 1){

                    if(updates.get(0).firmware_type == Enum_Firmware_type.FIRMWARE)
                        notification.setText(new Notification_Text().setText(" for Board "))
                                    .setObject(updates.get(0).board)
                                    .setText( new Notification_Text().setText(" from Code Editor with Program "))
                                    .setObject(updates.get(0).c_program_version_for_update.c_program)
                                    .setText( new Notification_Text().setText(" version "))
                                    .setObject(updates.get(0).c_program_version_for_update)
                                    .setText( new Notification_Text().setText("."));

                    else  if(updates.get(0).firmware_type == Enum_Firmware_type.BOOTLOADER)
                        notification.setText(new Notification_Text().setText(" for Board "))
                                .setObject(updates.get(0).board)
                                .setText( new Notification_Text().setText(" Bootloader version " + updates.get(0).bootloader.version_identificator ))
                                .setText( new Notification_Text().setText("."));

                }
                else{
                    notification.setText(new Notification_Text().setText(" for " + updates.size()  + " devices from Code Editor"));
                }

                notification.setText(new Notification_Text().setText(" just begun. We will keep you informed about progress."));

            }else if(type_of_update == Enum_Update_type_of_update.MANUALLY_BY_USER_BLOCKO_GROUP || type_of_update == Enum_Update_type_of_update.MANUALLY_BY_USER_BLOCKO_GROUP_ON_TIME ){

                notification.setText(new Notification_Text().setText("Update under Instance "))
                .setObject(homer_instance_record.actual_running_instance);

                if(updates.size() == 1){

                    notification.setText(new Notification_Text().setText(" with one device "));
                    notification.setObject(updates.get(0).board);

                }
                else{
                    notification.setText(new Notification_Text().setText(" with " + updates.size()  + " devices from Blocko Snapshot "));
                }

                notification.setText(new Notification_Text().setText(" just begun. We will keep you informed about progress."));

            }else {

                throw new Exception( "Update procedure has not set the type_of_update.");
            }

            notification.send_under_project(get_project_id());

        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }
    }

    @JsonIgnore @Transient
    public void notification_update_procedure_progress(){
        try {

            terminal_logger.debug("notification_update_procedure_progress :: operation ");

            if(state == Enum_Update_group_procedure_state.complete || state == Enum_Update_group_procedure_state.successful_complete  || state == Enum_Update_group_procedure_state.complete_with_error ){
                terminal_logger.warn("notification_update_procedure_progress ::  called inappropriately (complete) !!!!");
                return;
            }

            Model_Notification notification = new Model_Notification();

            notification.setId(UUID.randomUUID().toString())
                        .setImportance( Enum_Notification_importance.low)
                        .setLevel( Enum_Notification_level.info)
                        .setText(new Notification_Text().setText("Update of Procedure "))
                        .setObject(this)
                        .setText( new Notification_Text().setText(" is done from " +  procedure_size_complete() + "/" + procedure_size_all() + " ." ))
                        .send_under_project(get_project_id());

        }catch (Exception e){
            terminal_logger.internalServerError("notification_update_procedure_progress:", e);
        }
    }

    @JsonIgnore @Transient
    public void notification_update_procedure_final_report(){
        try {

            terminal_logger.debug("notification_update_procedure_final_report :: operation ");

            Model_Notification notification =  new Model_Notification();


            // Single Update
            if(this.updates.size() == 1 && ( type_of_update == Enum_Update_type_of_update.MANUALLY_BY_USER_INDIVIDUAL || type_of_update == Enum_Update_type_of_update.MANUALLY_BY_USER_BLOCKO_GROUP) ){

                terminal_logger.debug("notification_update_procedure_final_report :: Notification is for single update");

                // Bootloader
                if(this.updates.get(0).firmware_type == Enum_Firmware_type.BOOTLOADER) {

                    terminal_logger.debug("notification_update_procedure_final_report :: Single Update bootloader");

                    Model_BootLoader.notification_bootloader_procedure_success_information_single(this.updates.get(0));
                    return;
                }

                // Backup
                if(this.updates.get(0).firmware_type == Enum_Firmware_type.BACKUP) {

                    terminal_logger.debug("notification_update_procedure_final_report :: Single Update backup");
                    terminal_logger.debug("TODOTOTO TODO TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  ");

                }


            }


            notification.setImportance( Enum_Notification_importance.low )
                        .setLevel( Enum_Notification_level.success );


            int successfully_updated = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", Enum_CProgram_updater_state.complete)
                    .findRowCount();

            int waiting_for_device = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id).where()
                    .disjunction()
                        .eq("state", Enum_CProgram_updater_state.waiting_for_device)
                        .eq("state", Enum_CProgram_updater_state.homer_server_is_offline)
                        .eq("state", Enum_CProgram_updater_state.instance_inaccessible)
                    .endJunction()
                    .findRowCount();

            int error_device = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id).where()
                    .disjunction()
                        .eq("state", Enum_CProgram_updater_state.critical_error)
                        .eq("state", Enum_CProgram_updater_state.not_updated)
                    .endJunction()
                    .findRowCount();

            notification.setText(new Notification_Text().setText("Update Procedure "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" is complete. \n"))
                    .setText(new Notification_Text().setText("Number of Total devices for update: " + updates.size() + ". "))
                    .setText(new Notification_Text().setText("Successfully updated: " + successfully_updated + ". " ));

            if(waiting_for_device != 0){
                notification.setnewLine();
                notification.setText(new Notification_Text().setText("-> Unavailable \"offline\" devices: "  + waiting_for_device + ". " ));
            }

            if(error_device != 0){
                notification.setnewLine();
                notification.setText(new Notification_Text().setText("-> Unsuccessful updates "  + error_device + ". " ));
            }
            
            notification.send_under_project(get_project_id());

        }catch (Exception e){
            terminal_logger.internalServerError("notification_update_procedure_final_report:", e);
        }
    }

    @JsonIgnore @Transient
    public void notification_update_procedure_complete(){
        try {

            new Thread( () -> {

                terminal_logger.warn("notification_update_procedure_complete :: operation ");

                Model_Notification notification =  new Model_Notification();

                notification.setImportance( Enum_Notification_importance.normal )
                            .setLevel( Enum_Notification_level.success );

                // Individual update
                if(type_of_update == Enum_Update_type_of_update.MANUALLY_BY_USER_INDIVIDUAL){

                    if(state == Enum_Update_group_procedure_state.successful_complete) {
                        notification.setText(new Notification_Text().setText("Update Procedure "))
                                .setObject(this)
                                .setText(new Notification_Text().setText(" is complete."));
                    } else{
                        notification.setText(new Notification_Text().setText("Update Procedure "))
                                .setObject(this)
                                .setText(new Notification_Text().setText(" is set as complete but something is wrong. Check Update procedure details."));
                    }

                    notification.send_under_project(get_project_id());
                    return;
                }

                if(state == Enum_Update_group_procedure_state.successful_complete) {

                    notification.setText( new Notification_Text().setText("Update Procedure "))
                            .setObject(this)
                            .setText(new Notification_Text().setText(" started at: "))
                            .setDate(date_of_create)
                            .setText(new Notification_Text().setText(" is done with no errors or other issues."));

                    notification.send_under_project(get_project_id());
                    return;
                }

                if(state == Enum_Update_group_procedure_state.complete) {

                    notification.setText( new Notification_Text().setText("Update Procedure "))
                                .setObject(this)
                                .setText(new Notification_Text().setText(" started at: "))
                                .setDate(date_of_create)
                                .setText(new Notification_Text().setText(" is done but something is not right. For more information, please visit the update procedure details."));

                    notification.send_under_project(get_project_id());
                    return;

                }

            }).start();
        }catch (Exception e){
            terminal_logger.internalServerError("notification_update_procedure_complete:", e);
        }
    }


/* BLOB DATA  --------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient   public static final String read_permission_docs   = "User can read Actualization_procedure if they have ID of Actualization_procedure";

    @JsonIgnore @Transient   public boolean read_permission()      {  return Model_Project.find.where().eq("b_programs.instance.instance_history.procedures.id",id ).findUnique().read_permission() || Controller_Security.get_person().has_permission("Actualization_procedure_read"); }

    public enum permissions{Actualization_procedure_read}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    /**
     *  Cachování slouží primárně pouze pro sumarizaci updatů. Pomocí get_byId() lze načíst ActualizationProcedure
     *  která obsahuje HashMapu ID C
     */
    @JsonIgnore private HashMap<String, Enum_CProgram_updater_state> cProgram_updater_state = new HashMap<>();
    @JsonIgnore public static final String CACHE        = Model_ActualizationProcedure.class.getSimpleName();

    @JsonIgnore public static Cache<String, Model_ActualizationProcedure> cache; // Server_cache Override during server initialization

    public void change_state(Model_CProgramUpdatePlan plan, Enum_CProgram_updater_state state){
        cProgram_updater_state.put(plan.id, state);
    }


    @JsonIgnore
    public static Model_ActualizationProcedure get_byId(String id){

        Model_ActualizationProcedure procedure = cache.get(id);

        if(procedure == null){

            procedure = Model_ActualizationProcedure.find.byId(id);

            cache.put(id, procedure);
        }

        return procedure;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_ActualizationProcedure> find = new Model.Finder<>(Model_ActualizationProcedure.class);

}
