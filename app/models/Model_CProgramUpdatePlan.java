package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.helps_objects.TyrionCachedList;
import utilities.enums.*;
import utilities.errors.ErrorCode;
import utilities.logger.Class_Logger;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.swagger.outboundClass.Swagger_C_Program_Update_plan_Short_Detail;
import utilities.swagger.outboundClass.Swagger_UpdatePlan_brief_for_homer;
import utilities.swagger.outboundClass.Swagger_UpdatePlan_brief_for_homer_BinaryComponent;
import web_socket.message_objects.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Progress;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Objekt slouží k aktualizačnímu plánu jednotlivých zařízení!
 *
 */

@Entity
@ApiModel(description = "Model of CProgramUpdatePlan",
        value = "CProgramUpdatePlan")
@Table(name="CProgramUpdatePlan")
public class Model_CProgramUpdatePlan extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_CProgramUpdatePlan.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                         @Id @ApiModelProperty(required = true) public UUID id;

                                                       @JsonIgnore @ManyToOne() public Model_ActualizationProcedure actualization_procedure;

              @ApiModelProperty(required = true, value = "UNIX time in ms", example = "1466163478925") public Date date_of_finish;


              @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                    public Model_Board board;                           // Deska k aktualizaci
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


/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @TyrionCachedList public String cache_actualization_procedure_id;
    @JsonIgnore @Transient @TyrionCachedList public String cache_board_id;
    @JsonIgnore @Transient @TyrionCachedList public String cache_c_program_version_for_update_id;
    @JsonIgnore @Transient @TyrionCachedList public String cache_bootloader_id;

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
        board_detail.online_state = board.online_state();
        board_detail.name = board.name;
        board_detail.description = board.description;
        board_detail.type_of_board_id = board.get_type_of_board().id;
        board_detail.type_of_board_name = board.get_type_of_board().name;

        return board_detail;
    }

    @ApiModelProperty(required = false, value = "Is visible only if user send own binary file ( OR state for c_program_detail)")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty @Transient
    public Model_FileRecord binary_file_detail(){
        return binary_file == null ? null : binary_file;
    }



/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public Swagger_C_Program_Update_plan_Short_Detail get_short_version_for_board(){

        Swagger_C_Program_Update_plan_Short_Detail detail = new Swagger_C_Program_Update_plan_Short_Detail();
        detail.id = this.id.toString();
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
    @JsonIgnore @Transient public Swagger_UpdatePlan_brief_for_homer get_brief_for_update_homer_server(){
        try {

            Swagger_UpdatePlan_brief_for_homer brief_for_homer = new Swagger_UpdatePlan_brief_for_homer();
            brief_for_homer.actualization_procedure_id = actualization_procedure.id.toString();
            brief_for_homer.c_program_update_plan_id = id.toString();
            brief_for_homer.hardware_ids.add(board.id);

            Swagger_UpdatePlan_brief_for_homer_BinaryComponent binary = new Swagger_UpdatePlan_brief_for_homer_BinaryComponent();
            binary.firmware_type = firmware_type;

            brief_for_homer.binnary = binary;

            if(actualization_procedure.type_of_update == Enum_Update_type_of_update.MANUALLY_BY_USER_INDIVIDUAL){
                brief_for_homer.progress_subscribe = true;
            }

            if(firmware_type == Enum_Firmware_type.FIRMWARE || firmware_type == Enum_Firmware_type.BACKUP){
                binary.download_id =  c_program_version_for_update.c_compilation.id.toString();
                binary.build_id =  c_program_version_for_update.c_compilation.firmware_build_id;
                binary.program_name = c_program_version_for_update.c_program.name;
                binary.program_version_name = c_program_version_for_update.version_name;
            }
            else if(firmware_type == Enum_Firmware_type.BOOTLOADER){
                binary.download_id = bootloader.version_identificator;
                binary.build_id = bootloader.version_identificator;
            }
            else{
                terminal_logger.internalServerError(new IllegalAccessException("Unsupported type of Enum_Firmware_type or not set firmware_type in Model_CProgramUpdatePlan"));
                binary.download_id = binary_file.file_path;
                binary.build_id = "TODO"; // TODO ???
            }

            return brief_for_homer;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient  @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");
        count_of_tries = 0;

        if(this.state == null) this.state = Enum_CProgram_updater_state.not_start_yet;

        // Set Cache parameter
        cache_board_id = board.id;

        // set Cache Parameter
        if(c_program_version_for_update != null){
            cache_c_program_version_for_update_id = c_program_version_for_update.id;
        }

        // set Cache Parameter
        if(bootloader != null){
            cache_bootloader_id = bootloader.id.toString();
        }


        super.save();

        cache.put(id.toString(), this);
    }

    @JsonIgnore @Transient @Override
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

    @JsonIgnore @Transient  @Override
    public void delete() {

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
        @ApiModelProperty(required = true, readOnly = true) public Enum_Online_status online_state;
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String description;
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String name;
        @ApiModelProperty(required = true, readOnly = true) public String type_of_board_id;
        @ApiModelProperty(required = true, readOnly = true) public String type_of_board_name;
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/


/* SERVER WEBSOCKET CONTROLLING OF HOMER SERVER--------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public static void update_procedure_progress(WS_Message_Hardware_UpdateProcedure_Progress report){
        try {


            Enum_HardwareHomerUpdate_state status = report.get_phase();
            if (status == null){
                terminal_logger.error("Hardware_update_state_from_Homer " + report.phase + " is not recognize in Json!");
                return;
            }

            Model_CProgramUpdatePlan plan = Model_CProgramUpdatePlan.get_byId(report.tracking_id);
            if (plan == null) {
                throw new NullPointerException("Plan id" + report.tracking_id + " not found!");
            }

            switch (status){

                case ERASING_FLASH_STARTED: {
                    try {

                        Model_Notification notification = new Model_Notification();

                        notification
                                .setChainType(Enum_Notification_type.CHAIN_UPDATE)
                                .setId(plan.actualization_procedure.id.toString())
                                .setImportance(Enum_Notification_importance.low)
                                .setLevel(Enum_Notification_level.info);

                        notification.setText(new Notification_Text().setText("Update of Procedure "))
                                .setObject(plan.actualization_procedure)
                                .setText(new Notification_Text().setText(". We are making backup on board "))
                                .setObject(plan.board)
                                .setText(new Notification_Text().setText("finished:: " + report.percentage_progress + "%"))
                                .send_under_project(plan.actualization_procedure.get_project_id());

                    } catch (Exception e) {
                        terminal_logger.internalServerError(e);
                    }
                }


                case SENDING_PART: {
                    try {

                        Model_Notification notification = new Model_Notification();

                        notification
                                .setChainType(Enum_Notification_type.CHAIN_UPDATE)
                                .setId(plan.actualization_procedure.id.toString())
                                .setImportance(Enum_Notification_importance.low)
                                .setLevel(Enum_Notification_level.info);

                        notification.setText(new Notification_Text().setText("Update of Procedure "))
                                .setObject(plan.actualization_procedure)
                                .setText(new Notification_Text().setText(". Transfer firmware to "))
                                .setObject(plan.board)
                                .setText(new Notification_Text().setText(" finished:: " + report.percentage_progress + "%"))
                                .send_under_project(plan.actualization_procedure.get_project_id());

                    } catch (Exception e) {
                        terminal_logger.internalServerError(e);
                    }
                }


                case UPDATE_DONE: {
                    try {

                        plan.state = Enum_CProgram_updater_state.complete;
                        plan.update();

                        Model_Board board = plan.board;

                        if (plan.firmware_type == Enum_Firmware_type.FIRMWARE) {

                            board.actual_c_program_version = plan.c_program_version_for_update;
                            board.update();

                        } else if (plan.firmware_type == Enum_Firmware_type.BOOTLOADER) {

                            board.actual_boot_loader = plan.bootloader;
                            board.update();


                        } else if (plan.firmware_type == Enum_Firmware_type.BACKUP) {
                            board.actual_backup_c_program_version = plan.c_program_version_for_update;
                            board.update();

                            board.make_log_backup_arrise_change();
                        }

                    } catch (Exception e) {
                        terminal_logger.internalServerError(e);
                    }
                }

                case PHASE_WAITING: {
                    try {

                        plan.state = Enum_CProgram_updater_state.waiting_for_device;
                        plan.update();
                        Model_ActualizationProcedure.get_byId(report.tracking_group_id).change_state(plan, plan.state);

                    }catch (Exception e) {
                        terminal_logger.internalServerError(e);
                    }
                }

                case NEW_VERSION_DOESNT_MATCH: {
                    try {

                        plan.state = Enum_CProgram_updater_state.not_updated;
                        plan.error_code = ErrorCode.NEW_VERSION_DOESNT_MATCH.error_code();
                        plan.error = ErrorCode.NEW_VERSION_DOESNT_MATCH.error_message();
                        plan.date_of_finish = new Date();
                        plan.update();
                        Model_ActualizationProcedure.get_byId(report.tracking_group_id).change_state(plan, plan.state);

                    }catch (Exception e) {
                        terminal_logger.internalServerError(e);
                    }
                }

                case ALREADY_SAME: {
                    try {

                        plan.state = Enum_CProgram_updater_state.overwritten;
                        plan.update();

                    } catch (Exception e) {
                        terminal_logger.internalServerError(e);
                    }
                }

            }


        }catch (Exception e) {
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


