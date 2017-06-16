package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.enums.*;
import utilities.logger.Class_Logger;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.swagger.outboundClass.Swagger_C_Program_Update_plan_Short_Detail;
import web_socket.message_objects.homer_instance.WS_Message_UpdateProcedure_progress;
import web_socket.message_objects.homer_instance.WS_Message_UpdateProcedure_result;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * Objekt slouží k aktualizačnímu plánu jednotlivých zařízení!
 *
 */

@Entity
@ApiModel(description = "Model of CProgramUpdatePlan",
        value = "CProgramUpdatePlan")
public class Model_CProgramUpdatePlan extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_CProgramUpdatePlan.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                         @Id @ApiModelProperty(required = true) public String id;

                                                       @JsonIgnore @ManyToOne() public Model_ActualizationProcedure actualization_procedure;

                @ApiModelProperty(required = true, value = "UNIX time in ms",
                        example = "1466163478925")                              public Date date_of_create;

                @ApiModelProperty(required = true, value = "UNIX time in ms",
                        example = "1466163478925")                              public Date date_of_finish;


              @JsonIgnore @ManyToOne(fetch = FetchType.EAGER)                   public Model_Board board;                           // Deska k aktualizaci
              @Enumerated(EnumType.STRING)  @ApiModelProperty(required = true)  public Enum_Firmware_type firmware_type;                 // Typ Firmwaru

                                                                                // Aktualizace je vázána buď na verzi C++ kodu nebo na soubor, nahraný uživatelem
    /** OR **/  @JsonIgnore @ManyToOne(fetch = FetchType.EAGER)                 public Model_VersionObject c_program_version_for_update; // C_program k aktualizaci
    /** OR **/  @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                  public Model_BootLoader bootloader;                      // Když nahrávám Firmware
    /** OR **/  @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                  public Model_FileRecord binary_file;                     // Soubor, když firmware nahrává uživatel sám mimo flow

    @ApiModelProperty(required = true, value = "Description on Model C_ProgramUpdater_State")
                                                @Enumerated(EnumType.STRING)    public Enum_CProgram_updater_state state;
                                                                    @JsonIgnore public Integer count_of_tries;                         // Počet celkovbých pokusu doručit update (změny z wait to progres atd..

    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty( value = "Only if state is critical_error or Homer record some error", required = false)  public String error;
    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty( value = "Only if state is critical_error or Homer record some error", required = false)  public Integer error_code;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/


    @JsonProperty @Transient
    public Date date_of_planing() { return actualization_procedure.date_of_planing;}

    @ApiModelProperty(required = false, value = "Is visible only if update is for Firmware or Backup")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty @Transient
    public C_Program_Update_program c_program_detail(){

        if(c_program_version_for_update == null ) return null;

            C_Program_Update_program c_program_detail   = new  C_Program_Update_program();
            c_program_detail.c_program_id               = c_program_version_for_update.c_program.id;
            c_program_detail.c_program_program_name     = c_program_version_for_update.c_program.name;
            c_program_detail.c_program_version_id       = c_program_version_for_update.id;
            c_program_detail.c_program_version_name     = c_program_version_for_update.version_name;

            return c_program_detail;
    }

    @ApiModelProperty(required = false, value = "Is visible only if update is for Bootloader")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty @Transient
    public Bootloader_Update_program bootloader_detail(){

        if(bootloader == null ) return null;

        Bootloader_Update_program bootloader_update_detail  = new  Bootloader_Update_program();
        bootloader_update_detail.bootloader_id                      = bootloader.id.toString();
        bootloader_update_detail.bootloader_name                    = bootloader.name;
        bootloader_update_detail.version_identificator   = bootloader.version_identificator;

        return bootloader_update_detail;
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true) @Transient
    public Board_detail board_detail(){

        Board_detail board_detail = new Board_detail();
        board_detail.board_id = board.id;
        board_detail.personal_description = board.personal_description;
        board_detail.type_of_board_id = board.type_of_board.id;
        board_detail.type_of_board_name = board.type_of_board.name;

        return board_detail;
    }

    @ApiModelProperty(required = false, value = "Is visible only if user send own binary file ( OR state for c_program_detail)") @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty
    public Model_FileRecord binary_file_detail(){
        return binary_file == null ? null : binary_file;
    }



/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


    @JsonIgnore public Swagger_C_Program_Update_plan_Short_Detail get_short_version_for_board(){

        Swagger_C_Program_Update_plan_Short_Detail detail = new Swagger_C_Program_Update_plan_Short_Detail();
        detail.id = this.id;
        detail.date_of_create = date_of_create;
        detail.date_of_finish = date_of_finish;
        detail.firmware_type = firmware_type;
        detail.state = state;

        if(detail.firmware_type == Enum_Firmware_type.FIRMWARE || detail.firmware_type == Enum_Firmware_type.BACKUP){
            detail.c_program_id               = c_program_version_for_update.c_program.id;
            detail.c_program_program_name     = c_program_version_for_update.c_program.name;
            detail.c_program_version_id       = c_program_version_for_update.id;
            detail.c_program_version_name     = c_program_version_for_update.version_name;
        }

        if(detail.firmware_type == Enum_Firmware_type.BOOTLOADER ){
            detail.bootloader_id           = bootloader.id.toString();
            detail.bootloader_name         = bootloader.name;
            detail.version_identificator   = bootloader.version_identificator;
        }

        return detail;
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");
        count_of_tries = 0;

        if(this.state == null) this.state = Enum_CProgram_updater_state.not_start_yet;
        this.date_of_create = new Date();

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_CProgramUpdatePlan.find.byId(this.id) == null) break;
        }
        super.save();

        cache_model_update_plan.put(id, this);
    }

    @JsonIgnore @Override
    public void update() {

        terminal_logger.trace("update :: operation");

        super.update();

        if(actualization_procedure.state == Enum_Update_group_procedure_state.not_start_yet || actualization_procedure.state == Enum_Update_group_procedure_state.in_progress){

            if(this.state == Enum_CProgram_updater_state.overwritten
               || this.state  == Enum_CProgram_updater_state.complete
               || this.state  == Enum_CProgram_updater_state.not_updated
               || this.state  == Enum_CProgram_updater_state.critical_error
               ){

                terminal_logger.trace("update :: call in new thread actualization_procedure.update_state()");
                new Thread(() -> actualization_procedure.update_state()).start();

            }

        }

        cache_model_update_plan.put(id, this);
    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("update :: Delete object Id: {} ", this.id);
        super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    class C_Program_Update_program{
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String c_program_id;
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String c_program_version_id;
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String c_program_program_name;
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String c_program_version_name;
    }

    class Bootloader_Update_program{
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String bootloader_id;
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String bootloader_name;
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String version_identificator;

    }

    class Board_detail{
        @ApiModelProperty(required = true, readOnly = true) public String board_id;
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String personal_description;
        @ApiModelProperty(required = true, readOnly = true) public String type_of_board_id;
        @ApiModelProperty(required = true, readOnly = true) public String type_of_board_name;
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/


/* SERVER WEBSOCKET CONTROLLING OF HOMER SERVER--------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public static void update_procedure_progress(WS_Message_UpdateProcedure_progress progress_message){

        try {

            if(progress_message.percentageProgress == null || progress_message.percentageProgress < 1) return;

            Model_CProgramUpdatePlan plan = Model_CProgramUpdatePlan.get_model(progress_message.updatePlanId);

            if (plan == null) {
                terminal_logger.error( "update_procedure_progress:: Error:: Model_CProgramUpdatePlan id " + progress_message.updatePlanId + " not found");
                return;
            }

            if (Enum_UpdateProcedure_progress_type.fromString(progress_message.typeOfProgress) == Enum_UpdateProcedure_progress_type.MAKING_BACKUP) {

                try {

                    Model_Notification notification = new Model_Notification();

                    notification
                            .setChainType(Enum_Notification_type.CHAIN_UPDATE)
                            .setId(plan.actualization_procedure.id)
                            .setImportance(Enum_Notification_importance.low)
                            .setLevel(Enum_Notification_level.info);

                    notification.setText(new Notification_Text().setText("Update of Procedure "))
                            .setObject(plan.actualization_procedure)
                            .setText(new Notification_Text().setText(". We are making backup on board "))
                            .setObject(plan.board)
                            .setText(new Notification_Text().setText("finished:: " + progress_message.percentageProgress + "%"))
                            .send_under_project(plan.actualization_procedure.get_project_id());

                } catch (Exception e) {
                    terminal_logger.internalServerError("update_procedure_progress", e);
                }

            } else if (Enum_UpdateProcedure_progress_type.fromString(progress_message.typeOfProgress) == Enum_UpdateProcedure_progress_type.TRANSFER_DATA_TO_YODA) {

                try {

                    Model_Notification notification = new Model_Notification();

                    notification
                            .setChainType(Enum_Notification_type.CHAIN_UPDATE)
                            .setId(plan.actualization_procedure.id)
                            .setImportance(Enum_Notification_importance.low)
                            .setLevel(Enum_Notification_level.info);

                    notification.setText(new Notification_Text().setText("Update of Procedure "))
                            .setObject(plan.actualization_procedure)
                            .setText(new Notification_Text().setText(". Transfer firmware to "))
                            .setObject(plan.board)
                            .setText(new Notification_Text().setText(" finished:: " + progress_message.percentageProgress + "%"))
                            .send_under_project(plan.actualization_procedure.get_project_id());

                } catch (Exception e) {
                    terminal_logger.internalServerError("update_procedure_progress", e);
                }

            } else if (Enum_UpdateProcedure_progress_type.fromString(progress_message.typeOfProgress) == Enum_UpdateProcedure_progress_type.TRANSFER_DATA_FROM_YODA_TO_DEVICE) {

                try {

                    Model_Notification notification = new Model_Notification();

                    notification
                            .setId(plan.actualization_procedure.id)
                            .setChainType(Enum_Notification_type.CHAIN_UPDATE)
                            .setImportance(Enum_Notification_importance.low)
                            .setLevel(Enum_Notification_level.info);

                    notification.setText(new Notification_Text().setText("Update of Procedure "))

                            .setObject(plan.actualization_procedure)
                            .setText(new Notification_Text().setText(". We are transfer data from Master device "))
                            .setObject(plan.board) // TODO Master yoda device???
                            .setText(new Notification_Text().setText(" to final device "))
                            .setObject(plan.board)
                            .setText(new Notification_Text().setText(" finished:: " + progress_message.percentageProgress + "%"))
                            .send_under_project(plan.actualization_procedure.get_project_id());

                } catch (Exception e) {
                    terminal_logger.internalServerError("update_procedure_progress", e);
                }

            } else if (Enum_UpdateProcedure_progress_type.fromString(progress_message.typeOfProgress) == Enum_UpdateProcedure_progress_type.CHECKING_RESULT) {
                // TODO Tom - rozmyslet zda neskipnout pro prozatimní nevyužitelnost ??
                System.err.println("Checking devie TODOO");
            } else {
                terminal_logger.error("update_procedure_progress:: Error:: Enum_UpdateProcedure_progress_type id " + progress_message.typeOfProgress + " not recognize");
            }

        }catch (Exception e) {
            terminal_logger.internalServerError("update_procedure_progress", e);
        }
    }

    @JsonIgnore @Transient
    public static void update_procedure_state(WS_Message_UpdateProcedure_result procedure_result){
        try{

            terminal_logger.trace("update_procedure_state: Got quick update about progress of bigger update procedure");

            Model_CProgramUpdatePlan plan = get_model(procedure_result.updatePlanId);

            if(plan == null){
                terminal_logger.error("update_procedure_state:: Error: Model_CProgramUpdatePlan not found under id:: " + procedure_result.updatePlanId);
                return;
            }

            if(plan.state == Enum_CProgram_updater_state.overwritten){
                return;
            }

            Enum_HardwareHomerUpdate_state update_state = Enum_HardwareHomerUpdate_state.getUpdate_state(procedure_result.updateState);

            if(update_state == null){
                terminal_logger.error( "update_procedure_state:: Error: Enum_HardwareHomerUpdate_state not recognize:: " + procedure_result.updateState);
                return;
            }

            if(update_state == Enum_HardwareHomerUpdate_state.SUCCESSFULLY_UPDATE){

                plan.state = Enum_CProgram_updater_state.complete;
                plan.update();

                Model_Board board =  plan.board;

                if(plan.firmware_type == Enum_Firmware_type.FIRMWARE) {

                    board.actual_c_program_version = plan.c_program_version_for_update;
                    board.update();

                }else if(plan.firmware_type == Enum_Firmware_type.BOOTLOADER){

                    board.actual_boot_loader = plan.bootloader;
                    board.update();

                }else if(plan.firmware_type == Enum_Firmware_type.BACKUP){
                    board.actual_backup_c_program_version = plan.c_program_version_for_update;
                    board.update();
                }

                return;
            }

            if(update_state == Enum_HardwareHomerUpdate_state.DEVICE_WAS_OFFLINE || update_state == Enum_HardwareHomerUpdate_state.YODA_WAS_OFFLINE ){
                plan.state = Enum_CProgram_updater_state.waiting_for_device;
                plan.update();
                return;
            }

            if(update_state == Enum_HardwareHomerUpdate_state.TRANSMISSION_CRC_ERROR
                || update_state == Enum_HardwareHomerUpdate_state.UPDATE_PROGRESS_STACK
                || update_state == Enum_HardwareHomerUpdate_state.INVALID_DEVICE_STATE
                || update_state == Enum_HardwareHomerUpdate_state.ERROR
                || update_state == Enum_HardwareHomerUpdate_state.DEVICE_NOT_RECONNECTED
              ){
                plan.state = Enum_CProgram_updater_state.critical_error;
                plan.update();
                return;
            }



        }catch (Exception e) {
            terminal_logger.internalServerError(Model_CProgramUpdatePlan.class.getClass().getSimpleName() + ":: update_procedure_state :: Error", e);
        }
    }
/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE = Model_CProgramUpdatePlan.class.getName() + "_MODEL";

    public static Cache<String, Model_CProgramUpdatePlan> cache_model_update_plan = null; // Server_cache Override during server initialization

    public static Model_CProgramUpdatePlan get_model(String id){

        Model_CProgramUpdatePlan model = cache_model_update_plan.get(id);

        if(model == null){

            model = Model_CProgramUpdatePlan.find.byId(id);

            if (model == null) {
                terminal_logger.error( ":: get_model :: id not found:: " + id);
                return null;
            }

            cache_model_update_plan.put(id, model);
        }

        return model;
    }


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_CProgramUpdatePlan> find = new Model.Finder<>(Model_CProgramUpdatePlan.class);

}


