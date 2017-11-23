package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.libs.Json;
import utilities.cache.helps_objects.TyrionCachedList;
import utilities.enums.*;
import utilities.errors.ErrorCode;
import utilities.logger.Class_Logger;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.swagger.outboundClass.*;
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
@Table(name="CProgramUpdatePlan")
public class Model_CProgramUpdatePlan extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_CProgramUpdatePlan.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                         @Id @ApiModelProperty(required = true) public UUID id;

                                                       @JsonIgnore @ManyToOne() public Model_ActualizationProcedure actualization_procedure;

                                            @ApiModelProperty(required = true, 
                                                    value = "UNIX time in ms",
                                                    example = "1466163478925")  public Date date_of_finish;

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
    public Date date_of_planing() {
        return actualization_procedure.date_of_planing;
    }

    @JsonProperty @Transient
    public Date date_of_create() {
        return actualization_procedure.date_of_create;
    }

    @ApiModelProperty(required = false, value = "Is visible only if update is for Firmware or Backup")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty @Transient
    public Swagger_C_Program_Update_program c_program_detail() {

        try {
            if (c_program_version_for_update == null) return null;

            Swagger_C_Program_Update_program c_program_detail = new Swagger_C_Program_Update_program();
            c_program_detail.c_program_id = c_program_version_for_update.get_c_program().id;
            c_program_detail.c_program_program_name = c_program_version_for_update.get_c_program().name;
            c_program_detail.c_program_version_id = c_program_version_for_update.id;
            c_program_detail.c_program_version_name = c_program_version_for_update.version_name;

            return c_program_detail;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @ApiModelProperty(required = false, value = "Is visible only if update is for Bootloader")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty @Transient
    public Swagger_Bootloader_Update_program bootloader_detail() {
        try {

            Model_BootLoader cached_bootLoader = get_bootloader();

            if (cached_bootLoader == null) return null;

            Swagger_Bootloader_Update_program bootloader_update_detail = new Swagger_Bootloader_Update_program();
            bootloader_update_detail.bootloader_id = cached_bootLoader.id.toString();
            bootloader_update_detail.bootloader_name = cached_bootLoader.name;
            bootloader_update_detail.version_identificator = cached_bootLoader.version_identificator;
            bootloader_update_detail.type_of_board_name = cached_bootLoader.type_of_board.name;
            bootloader_update_detail.type_of_board_id = cached_bootLoader.type_of_board.id;

            return bootloader_update_detail;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true) @Transient
    public Swagger_Board_Update_Short_Detail board_detail() {

        Swagger_Board_Update_Short_Detail board_detail = new Swagger_Board_Update_Short_Detail();
        board_detail.board_id = get_board().id;
        board_detail.online_state =  get_board().online_state();
        board_detail.name =  get_board().name;
        board_detail.description =  get_board().description;
        board_detail.type_of_board_id =  get_board().get_type_of_board().id;
        board_detail.type_of_board_name =  get_board().get_type_of_board().name;

        return board_detail;
    }

    @ApiModelProperty(required = false, value = "Is visible only if user send own binary file ( OR state for c_program_detail)")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty @Transient
    public Model_FileRecord binary_file_detail() {
        return binary_file == null ? null : binary_file;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public Swagger_C_Program_Update_plan_Short_Detail get_short_version_for_board() {

        Swagger_C_Program_Update_plan_Short_Detail detail = new Swagger_C_Program_Update_plan_Short_Detail();
        detail.id = this.id.toString();
        detail.actualization_procedure_id = this.actualization_procedure.id.toString();

        detail.type_of_update = this.actualization_procedure.type_of_update;
        detail.date_of_create = date_of_create();
        detail.date_of_planed = date_of_planing();
        detail.date_of_finish = date_of_finish;
        detail.firmware_type = firmware_type;
        detail.state = state;

        if (detail.firmware_type == Enum_Firmware_type.FIRMWARE || detail.firmware_type == Enum_Firmware_type.BACKUP) {
                detail.program = c_program_detail();
        }

        if (detail.firmware_type == Enum_Firmware_type.BOOTLOADER ) {
            detail.bootloader = bootloader_detail();
        }

        return detail;
    }

    @JsonIgnore @Transient public Model_Board get_board() {

        if (cache_board_id != null) {
            return Model_Board.get_byId(cache_board_id);
        }

        Model_Board board_not_cached = Model_Board.find.where().eq("c_program_update_plans.id", id).select("id").findUnique();
        if (board_not_cached != null) {
            cache_board_id = board_not_cached.id;
            return get_board();
        }

        return null;
    }

    @JsonIgnore @Transient public Model_BootLoader get_bootloader() {

        if (cache_bootloader_id != null) {
            return Model_BootLoader.get_byId(cache_bootloader_id);
        }

        Model_BootLoader bootloader_not_cached = Model_BootLoader.find.where().eq("c_program_update_plans.id", id).select("id").findUnique();
        if (bootloader_not_cached != null) {
            cache_bootloader_id = bootloader_not_cached.id.toString();
            return get_bootloader();
        }

        return null;
    }

    @JsonIgnore @Transient public Swagger_UpdatePlan_brief_for_homer get_brief_for_update_homer_server() {
        try {

            Swagger_UpdatePlan_brief_for_homer brief_for_homer = new Swagger_UpdatePlan_brief_for_homer();
            brief_for_homer.actualization_procedure_id = actualization_procedure.id.toString();
            brief_for_homer.c_program_update_plan_id = id.toString();
            brief_for_homer.hardware_ids.add(get_board().id);

            Swagger_UpdatePlan_brief_for_homer_BinaryComponent binary = new Swagger_UpdatePlan_brief_for_homer_BinaryComponent();
            binary.firmware_type = firmware_type;

            brief_for_homer.binary = binary;

            if (actualization_procedure.type_of_update == Enum_Update_type_of_update.MANUALLY_BY_USER_INDIVIDUAL) {
                brief_for_homer.progress_subscribe = true;
            }

            if (firmware_type == Enum_Firmware_type.FIRMWARE || firmware_type == Enum_Firmware_type.BACKUP) {
                binary.download_id              = c_program_version_for_update.c_compilation.id.toString();
                binary.build_id                 = c_program_version_for_update.c_compilation.firmware_build_id;
                binary.program_name             = c_program_version_for_update.get_c_program().name.length() > 32 ? c_program_version_for_update.get_c_program().name.substring(0, 32) : c_program_version_for_update.get_c_program().name;
                binary.program_version_name     = c_program_version_for_update.version_name.length() > 32 ? c_program_version_for_update.version_name.substring(0, 32) : c_program_version_for_update.version_name;
                binary.compilation_lib_version  = c_program_version_for_update.c_compilation.firmware_version_lib;
                binary.time_stamp               = c_program_version_for_update.c_compilation.firmware_build_datetime;

            } else if (firmware_type == Enum_Firmware_type.BOOTLOADER) {

                Model_BootLoader cached_bootLoader = get_bootloader();
                if (cached_bootLoader == null) return null;

                binary.download_id          = cached_bootLoader.id.toString();
                binary.build_id             = cached_bootLoader.version_identificator;
                binary.program_name         = cached_bootLoader.name.length() > 32 ? cached_bootLoader.name.substring(0, 32) : cached_bootLoader.name;
                binary.program_version_name = cached_bootLoader.version_identificator.length() > 32 ? cached_bootLoader.version_identificator.substring(0, 32) : cached_bootLoader.version_identificator;
                binary.time_stamp           = cached_bootLoader.date_of_create;
            } else {
                terminal_logger.internalServerError(new IllegalAccessException("Unsupported type of Enum_Firmware_type or not set firmware_type in Model_CProgramUpdatePlan"));
                binary.download_id = binary_file.file_path;
                binary.build_id = "TODO"; // TODO ???
            }

            return brief_for_homer;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient  @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");
        count_of_tries = 0;

        if (this.state == null) this.state = Enum_CProgram_updater_state.not_start_yet;

        // Set Cache parameter
        cache_board_id = board.id;

        // set Cache Parameter
        if (c_program_version_for_update != null) {
            cache_c_program_version_for_update_id = c_program_version_for_update.id;
        }

        // set Cache Parameter
        if (bootloader != null) {
            cache_bootloader_id = bootloader.id.toString();
        }


        super.save();

        cache.put(id.toString(), this);
    }

    @JsonIgnore @Transient @Override
    public void update() {

        terminal_logger.trace("update :: operation");

        super.update();

        if (actualization_procedure.state == Enum_Update_group_procedure_state.not_start_yet || actualization_procedure.state == Enum_Update_group_procedure_state.in_progress) {

            if (this.state == Enum_CProgram_updater_state.overwritten
               || this.state  == Enum_CProgram_updater_state.complete
               || this.state  == Enum_CProgram_updater_state.not_updated
               || this.state  == Enum_CProgram_updater_state.critical_error
               ) {

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

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* SERVER WEBSOCKET CONTROLLING OF HOMER SERVER--------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public static void update_procedure_progress(WS_Message_Hardware_UpdateProcedure_Progress report) {
        try {

            Enum_HardwareHomerUpdate_state phase = report.get_phase();
            if (phase == null) {
                terminal_logger.error("update_procedure_progress " + report.phase + " is not recognize in Json!");
                return;
            }

            Model_CProgramUpdatePlan plan = Model_CProgramUpdatePlan.get_byId(report.tracking_id);
            if (plan == null) {
                throw new NullPointerException("Plan id" + report.tracking_id + " not found!");
            }


            // Pokud se vrátí fáze špatně - ukončuji celý update
            if (report.error != null || report.error_code != null) {
                terminal_logger.warn("update_procedure_progress  Update Fail! Device ID: {}, update procedure: {}", plan.get_board().id, plan.id);

                plan.state = Enum_CProgram_updater_state.critical_error;
                plan.error_code = report.error_code;
                plan.error = report.error;
                plan.update();
                Model_ActualizationProcedure.get_byId(report.tracking_group_id).change_state(plan, plan.state);
                return;
            }

            switch (phase) {

                case PHASE_UPLOADING: {
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
                                .setObject(plan.get_board())
                                .setText(new Notification_Text().setText(" finished:: " + report.percentage_progress + "%"))
                                .send_under_project(plan.actualization_procedure.get_project_id());

                        return;

                    } catch (Exception e) {
                        terminal_logger.internalServerError(e);
                    }
                }

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
                                .setObject(plan.get_board())
                                .setText(new Notification_Text().setText("finished:: " + report.percentage_progress + "%"))
                                .send_under_project(plan.actualization_procedure.get_project_id());

                        return;

                    } catch (Exception e) {
                        terminal_logger.internalServerError(e);
                    }
                }

                case UPDATE_DONE: {
                    try {

                        terminal_logger.debug("update_procedure_progress - procedure {} is completed", plan.id);

                        plan.state = Enum_CProgram_updater_state.complete;
                        plan.date_of_finish = new Date();
                        plan.update();

                        Model_Board board = plan.get_board();

                        if (plan.firmware_type == Enum_Firmware_type.FIRMWARE) {

                            board.actual_c_program_version = plan.c_program_version_for_update;
                            board.update();

                        } else if (plan.firmware_type == Enum_Firmware_type.BOOTLOADER) {

                            board.actual_boot_loader = plan.get_bootloader();
                            board.update();

                        } else if (plan.firmware_type == Enum_Firmware_type.BACKUP) {
                            board.actual_backup_c_program_version = plan.c_program_version_for_update;
                            board.update();

                            board.make_log_backup_arrise_change();
                        }

                        return;
                    } catch (Exception e) {
                        terminal_logger.internalServerError(e);
                    }
                }

                case PHASE_WAITING: {
                    try {

                        plan.state = Enum_CProgram_updater_state.waiting_for_device;
                        plan.update();
                        Model_ActualizationProcedure.get_byId(report.tracking_group_id).change_state(plan, plan.state);

                        return;
                    } catch (Exception e) {
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

                        return;
                    } catch (Exception e) {
                        terminal_logger.internalServerError(e);
                    }
                }

                case ALREADY_SAME: {
                    try {

                        plan.state = Enum_CProgram_updater_state.overwritten;
                        plan.update();

                        return;
                    } catch (Exception e) {
                        terminal_logger.internalServerError(e);
                    }
                }

                case PHASE_RESTARTING: return;

                default: {
                    throw new UnsupportedOperationException("Unknown update phase. Report: " + Json.toJson(report));
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

    public static Model_CProgramUpdatePlan get_byId(String id) {

        Model_CProgramUpdatePlan plan = cache.get(id);
        if (plan == null) {

            plan = find.byId(id);

            if (plan == null) return null;

            cache.put(id, plan);
        }

        return plan;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_CProgramUpdatePlan> find = new Model.Finder<>(Model_CProgramUpdatePlan.class);
}
