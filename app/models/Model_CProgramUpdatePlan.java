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
import utilities.swagger.outboundClass.Swagger_UpdatePlan_brief_for_homer;
import web_socket.message_objects.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Status;
import web_socket.message_objects.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Progress;

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

                                         @Id @ApiModelProperty(required = true) public UUID id;

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
        board_detail.name = board.name;
        board_detail.description = board.description;
        board_detail.type_of_board_id = board.get_type_of_board().id;
        board_detail.type_of_board_name = board.get_type_of_board().name;

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
        detail.id = this.id.toString();
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

    @JsonIgnore public Swagger_UpdatePlan_brief_for_homer get_brief_for_update_homer_server(){
        try {

            Swagger_UpdatePlan_brief_for_homer brief_for_homer = new Swagger_UpdatePlan_brief_for_homer();
            brief_for_homer.actualization_procedure_id = actualization_procedure.id;
            brief_for_homer.c_program_update_plan_id = id.toString();
            brief_for_homer.device_id = board.id;

            if(actualization_procedure.type_of_update == Enum_Update_type_of_update.MANUALLY_BY_USER_INDIVIDUAL){
                brief_for_homer.progress_subscribe = true;
            }

            if(firmware_type == Enum_Firmware_type.FIRMWARE || firmware_type == Enum_Firmware_type.BACKUP){
                brief_for_homer.blob_link =  c_program_version_for_update.c_compilation.bin_compilation_file.file_path;
                brief_for_homer.build_id =  c_program_version_for_update.c_compilation.firmware_build_id;
                brief_for_homer.program_name = c_program_version_for_update.c_program.name;
                brief_for_homer.program_version_name = c_program_version_for_update.version_name;
            }
            else if(firmware_type == Enum_Firmware_type.BOOTLOADER){
                brief_for_homer.blob_link = bootloader.file.file_path;
                brief_for_homer.build_id = bootloader.version_identificator;
            }
            else{
                brief_for_homer.blob_link = binary_file.file_path;
                brief_for_homer.build_id = "TODO";
            }

            return brief_for_homer;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");
        count_of_tries = 0;

        if(this.state == null) this.state = Enum_CProgram_updater_state.not_start_yet;
        this.date_of_create = new Date();

        super.save();

        cache.put(id.toString(), this);
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

        cache.put(id.toString(), this);
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
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String description;
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String name;
        @ApiModelProperty(required = true, readOnly = true) public String type_of_board_id;
        @ApiModelProperty(required = true, readOnly = true) public String type_of_board_name;
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/


/* SERVER WEBSOCKET CONTROLLING OF HOMER SERVER--------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public static void update_procedure_progress(WS_Message_Hardware_UpdateProcedure_Progress progress_message){
        try {

            if(progress_message.percentage_progress == null || progress_message.percentage_progress < 1) return;

            Model_CProgramUpdatePlan plan = Model_CProgramUpdatePlan.get_byId(progress_message.c_program_update_plan_id);

            if (plan == null) throw new Exception("Model_CProgramUpdatePlan ID = " + progress_message.c_program_update_plan_id + " not found");

            if (Enum_UpdateProcedure_progress_type.fromString(progress_message.type_of_progress) == Enum_UpdateProcedure_progress_type.MAKING_BACKUP) {
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
                            .setText(new Notification_Text().setText("finished:: " + progress_message.percentage_progress + "%"))
                            .send_under_project(plan.actualization_procedure.get_project_id());

                } catch (Exception e) {
                    terminal_logger.internalServerError(e);
                }

            } else if (Enum_UpdateProcedure_progress_type.fromString(progress_message.type_of_progress) == Enum_UpdateProcedure_progress_type.TRANSFER_DATA_TO_DEVICE) {

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
                            .setText(new Notification_Text().setText(" finished:: " + progress_message.percentage_progress + "%"))
                            .send_under_project(plan.actualization_procedure.get_project_id());

                } catch (Exception e) {
                    terminal_logger.internalServerError(e);
                }

            } else if (Enum_UpdateProcedure_progress_type.fromString(progress_message.type_of_progress) == Enum_UpdateProcedure_progress_type.CHECKING_RESULT) {
                // TODO Tom - rozmyslet zda neskipnout pro prozatimní nevyužitelnost ??
                System.err.println("Checking devie TODOO");
            } else {
                throw new Exception("Enum_UpdateProcedure_progress_type " + progress_message.type_of_progress + " not recognized.");
            }

        }catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

    @JsonIgnore @Transient
    public static void update_procedure_state(WS_Message_Hardware_UpdateProcedure_Status report){
        try{

            terminal_logger.trace("update_procedure_state: Got quick update about progress of bigger update procedure");


            Enum_HardwareHomerUpdate_state status = Enum_HardwareHomerUpdate_state.getUpdate_state(report.update_state);
            if (status == null) throw new NullPointerException("Hardware_update_state_from_Homer " + report.update_state + " is not recognize in Json!");

            Model_CProgramUpdatePlan plan = Model_CProgramUpdatePlan.get_byId(report.c_program_update_plan_id);
            if (plan == null) throw new NullPointerException("Plan id" + report.c_program_update_plan_id + " not found!");


            if (plan.state == Enum_CProgram_updater_state.overwritten){
                return;
            }

            if(status == Enum_HardwareHomerUpdate_state.OVERWRITTEN){
                if(plan.state != Enum_CProgram_updater_state.overwritten){
                    plan.state = Enum_CProgram_updater_state.overwritten;
                    plan.update();
                    return;
                }
            }



            if(status == Enum_HardwareHomerUpdate_state.SUCCESSFULLY_UPDATE){

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

                    board.make_log_backup_arrise_change();
                }

                return;

            }else {

                if (status == Enum_HardwareHomerUpdate_state.DEVICE_WAS_OFFLINE) {
                    plan.state = Enum_CProgram_updater_state.waiting_for_device;
                    plan.update();
                    Model_ActualizationProcedure.get_byId(report.actualization_procedure_id).change_state(plan, plan.state);
                    return;
                }

                else if (status == Enum_HardwareHomerUpdate_state.DEVICE_WAS_NOT_UPDATED_TO_RIGHT_VERSION) {

                    plan.state = Enum_CProgram_updater_state.not_updated;
                    plan.date_of_finish = new Date();
                    plan.update();
                    Model_ActualizationProcedure.get_byId(report.actualization_procedure_id).change_state(plan, plan.state);
                    return;
                }

                else {
                    plan.state = Enum_CProgram_updater_state.critical_error;
                    plan.error_code = report.error_code;
                    plan.date_of_finish = new Date();
                    plan.update();
                }
            }

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE = Model_CProgramUpdatePlan.class.getName() + "_MODEL";

    public static Cache<String, Model_CProgramUpdatePlan> cache = null; // Server_cache Override during server initialization

    public static Model_CProgramUpdatePlan get_byId(String id){

        Model_CProgramUpdatePlan plan = cache.get(id);
        if(plan == null){

            plan = find.byId(id);

            if (plan == null) return null;

            cache.put(id, plan);
        }

        return plan;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_CProgramUpdatePlan> find = new Model.Finder<>(Model_CProgramUpdatePlan.class);
}


