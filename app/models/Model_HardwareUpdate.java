package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.libs.Json;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.enums.*;
import utilities.errors.ErrorCode;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.models_update_echo.EchoHandler;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.swagger.output.Swagger_Bootloader_Update_program;
import utilities.swagger.output.Swagger_C_Program_Update_program;
import utilities.swagger.output.Swagger_UpdatePlan_brief_for_homer;
import utilities.swagger.output.Swagger_UpdatePlan_brief_for_homer_BinaryComponent;
import websocket.messages.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Progress;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * Objekt slouží k aktualizačnímu plánu jednotlivých zařízení!
 *
 */

@Entity
@ApiModel(description = "Model of HardwareUpdate",
        value = "HardwareUpdate")
@Table(name="HardwareUpdate")
public class Model_HardwareUpdate extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_HardwareUpdate.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                       @JsonIgnore @ManyToOne() public Model_UpdateProcedure actualization_procedure;    // TODO CACHE

                                            @ApiModelProperty(required = true, 
                                                    value = "UNIX time in ms",
                                                    example = "1466163478925")  public Date finished;

              @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                    public Model_Hardware hardware; // Deska k aktualizaci
              @Enumerated(EnumType.STRING)  @ApiModelProperty(required = true)  public FirmwareType firmware_type;          // Typ Firmwaru

                                                                                // Aktualizace je vázána buď na verzi C++ kodu nebo na soubor, nahraný uživatelem
    /** OR **/  @JsonIgnore @ManyToOne(fetch = FetchType.EAGER)                 public Model_CProgramVersion c_program_version_for_update; // C_program k aktualizaci
    /** OR **/  @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                  public Model_BootLoader bootloader;                      // Když nahrávám Firmware
    /** OR **/  @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                  public Model_Blob binary_file;                     // Soubor, když firmware nahrává uživatel sám mimo flow

                                                @Enumerated(EnumType.STRING)    public HardwareUpdateState state;
                                                                    @JsonIgnore public Integer count_of_tries;                         // Počet celkovbých pokusu doručit update (změny z wait to progres atd..

    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty("Only if state is critical_error or Homer record some error")  public String error;
    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty("Only if state is critical_error or Homer record some error")  public Integer error_code;

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached public UUID cache_actualization_procedure_id;
    @JsonIgnore @Transient @Cached public UUID cache_hardware_id;
    @JsonIgnore @Transient @Cached public UUID cache_c_program_version_id;
    @JsonIgnore @Transient @Cached public UUID cache_bootloader_id;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = false, readOnly = true)
    public UpdateType type_of_update () {
       return actualization_procedure.type_of_update;
    }

    @JsonProperty @ApiModelProperty(required = false, readOnly = true)
    public UUID actualization_procedure_id() {
        if (cache_actualization_procedure_id == null) {
            Model_UpdateProcedure procedure_not_cached = Model_UpdateProcedure.find.query().where().eq("updates.id", id).select("id").findOne();
            cache_actualization_procedure_id = procedure_not_cached.id;
        }
        return cache_actualization_procedure_id;
    }


    @JsonProperty
    public Date date_of_planing() {
        return actualization_procedure.date_of_planing;
    }

    @JsonProperty
    public Date created() {
        return actualization_procedure.created;
    }

    @ApiModelProperty(required = false, value = "Is visible only if update is for Firmware or Backup")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty
    public Swagger_C_Program_Update_program c_program_detail() {

        try {
            if (c_program_version_for_update == null) return null;

            Swagger_C_Program_Update_program c_program_detail = new Swagger_C_Program_Update_program();
            c_program_detail.c_program_id = c_program_version_for_update.get_c_program().id;
            c_program_detail.c_program_program_name = c_program_version_for_update.get_c_program().name;
            c_program_detail.c_program_version_id = c_program_version_for_update.id;
            c_program_detail.c_program_version_name = c_program_version_for_update.name;

            return c_program_detail;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @ApiModelProperty(required = false, value = "Is visible only if update is for Bootloader")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty
    public Swagger_Bootloader_Update_program bootloader_detail() {
        try {

            Model_BootLoader cached_bootLoader = getBootloader();

            if (cached_bootLoader == null) return null;

            Swagger_Bootloader_Update_program bootloader_update_detail = new Swagger_Bootloader_Update_program();
            bootloader_update_detail.bootloader_id = cached_bootLoader.id;
            bootloader_update_detail.bootloader_name = cached_bootLoader.name;
            bootloader_update_detail.version_identificator = cached_bootLoader.version_identifier;
            bootloader_update_detail.hardware_type_name = cached_bootLoader.hardware_type.name;
            bootloader_update_detail.hardware_type_id = cached_bootLoader.hardware_type.id;

            return bootloader_update_detail;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public Model_Hardware hardware() {
        return getHardware();
    }

    @ApiModelProperty(required = false, value = "Is visible only if user send own binary file ( OR state for c_program_detail)")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty
    public Model_Blob binary_file_detail() {
        return binary_file;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_Hardware getHardware() {

        if (cache_hardware_id != null) {
            return Model_Hardware.getById(cache_hardware_id);
        }

        Model_Hardware hardware_not_cached = Model_Hardware.find.query().where().eq("registration.updates.id", id).select("id").findOne();
        if (hardware_not_cached != null) {
            cache_hardware_id = hardware_not_cached.id;
            return getHardware();
        }

        return null;
    }

    @JsonIgnore
    public Model_BootLoader getBootloader() {

        if (cache_bootloader_id != null) {
            return Model_BootLoader.getById(cache_bootloader_id);
        }

        Model_BootLoader bootloader_not_cached = Model_BootLoader.find.query().where().eq("updates.id", id).select("id").findOne();
        if (bootloader_not_cached != null) {
            cache_bootloader_id = bootloader_not_cached.id;
            return getBootloader();
        }

        return null;
    }

    @JsonIgnore
    public Swagger_UpdatePlan_brief_for_homer get_brief_for_update_homer_server() {
        try {

            Swagger_UpdatePlan_brief_for_homer brief_for_homer = new Swagger_UpdatePlan_brief_for_homer();
            brief_for_homer.update_procedure_id = actualization_procedure.id.toString();
            brief_for_homer.hardware_update_id = id.toString();
            brief_for_homer.hardware_ids.add(getHardware().id);

            Swagger_UpdatePlan_brief_for_homer_BinaryComponent binary = new Swagger_UpdatePlan_brief_for_homer_BinaryComponent();
            binary.firmware_type = firmware_type;

            brief_for_homer.binary = binary;

            if (actualization_procedure.type_of_update == UpdateType.MANUALLY_BY_USER_INDIVIDUAL) {
                brief_for_homer.progress_subscribe = true;
            }

            if (firmware_type == FirmwareType.FIRMWARE || firmware_type == FirmwareType.BACKUP) {
                binary.download_id              = c_program_version_for_update.compilation.id.toString();
                binary.build_id                 = c_program_version_for_update.compilation.firmware_build_id;
                binary.program_name             = c_program_version_for_update.get_c_program().name.length() > 32 ? c_program_version_for_update.get_c_program().name.substring(0, 32) : c_program_version_for_update.get_c_program().name;
                binary.program_version_name     = c_program_version_for_update.name.length() > 32 ? c_program_version_for_update.name.substring(0, 32) : c_program_version_for_update.name;
                binary.compilation_lib_version  = c_program_version_for_update.compilation.firmware_version_lib;
                binary.time_stamp               = c_program_version_for_update.compilation.firmware_build_datetime;

            } else if (firmware_type == FirmwareType.BOOTLOADER) {

                Model_BootLoader cached_bootLoader = getBootloader();
                if (cached_bootLoader == null) return null;

                binary.download_id          = cached_bootLoader.id.toString();
                binary.build_id             = cached_bootLoader.version_identifier;
                binary.program_name         = cached_bootLoader.name.length() > 32 ? cached_bootLoader.name.substring(0, 32) : cached_bootLoader.name;
                binary.program_version_name = cached_bootLoader.version_identifier.length() > 32 ? cached_bootLoader.version_identifier.substring(0, 32) : cached_bootLoader.version_identifier;
                binary.time_stamp           = cached_bootLoader.created;
            } else {
                logger.internalServerError(new IllegalAccessException("Unsupported type of Enum_Firmware_type or not set firmware_type in Model_CProgramUpdatePlan"));
                binary.download_id = binary_file.path;
                binary.build_id = "TODO"; // TODO ???
            }

            return brief_for_homer;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        logger.debug("save :: Creating new Object");
        count_of_tries = 0;

        if (this.state == null) this.state = HardwareUpdateState.NOT_YET_STARTED;

        // Set Cache parameter
        cache_hardware_id = hardware.id;

        // set Cache Parameter
        if (c_program_version_for_update != null) {
            cache_c_program_version_id = c_program_version_for_update.id;
        }

        // set Cache Parameter
        if (bootloader != null) {
            cache_bootloader_id = bootloader.id;
        }


        super.save();

        cache.put(id, this);
    }

    @JsonIgnore @Override
    public void update() {

        super.update();

        Model_UpdateProcedure procedure = Model_UpdateProcedure.getById(actualization_procedure_id());
        if (procedure != null) {
            if (procedure.state == Enum_Update_group_procedure_state.not_start_yet || procedure.state == Enum_Update_group_procedure_state.in_progress) {

                if (this.state == HardwareUpdateState.OBSOLETE
                        || this.state == HardwareUpdateState.COMPLETE
                        || this.state == HardwareUpdateState.NOT_UPDATED
                        || this.state == HardwareUpdateState.CRITICAL_ERROR
                        ) {

                    logger.trace("update :: call in new thread actualization_procedure.update_state()");
                    new Thread(() -> procedure.update_state()).start();
                }
            }

            // Call notification about model update
            if (procedure.get_project_id() != null) {
                new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_HardwareUpdate.class, procedure.get_project_id() , this.id))).start();
            }

        }


        cache.put(id, this);
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* SERVER WEBSOCKET CONTROLLING OF HOMER SERVER--------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public static void update_procedure_progress(WS_Message_Hardware_UpdateProcedure_Progress report) {
        try {

            Enum_HardwareHomerUpdate_state phase = report.get_phase();
            if (phase == null) {
                logger.error("update_procedure_progress " + report.phase + " is not recognize in Json!");
                return;
            }

            Model_HardwareUpdate plan = Model_HardwareUpdate.getById(report.tracking_id);
            if (plan == null) {
                throw new NullPointerException("Plan id" + report.tracking_id + " not found!");
            }


            // Pokud se vrátí fáze špatně - ukončuji celý update
            if (report.error != null || report.error_code != null) {
                logger.warn("update_procedure_progress  Update Fail! Device ID: {}, update procedure: {}", plan.getHardware().id, plan.id);

                plan.state = HardwareUpdateState.CRITICAL_ERROR;
                plan.error_code = report.error_code;
                plan.error = report.error;
                plan.update();
                Model_UpdateProcedure.getById(report.tracking_group_id).change_state(plan, plan.state);

                Model_Notification notification = new Model_Notification();

                notification
                        .setChainType(NotificationType.CHAIN_END)
                        .setNotificationId(plan.actualization_procedure.id)
                        .setImportance(NotificationImportance.LOW)
                        .setLevel(NotificationLevel.ERROR);

                notification.setText(new Notification_Text().setText("Update of Procedure "))
                        .setObject(plan.actualization_procedure)
                        .setText(new Notification_Text().setText(". Transfer firmware to device "))
                        .setObject(plan.getHardware())
                        .setText(new Notification_Text().setText(" failed. Error Code " + report.error_code + "."))
                        .send_under_project(plan.actualization_procedure.get_project_id());


                return;
            }

            // Fáze jsou volány jen tehdá, když má homer instrukce je zasílat
            switch (phase) {

                case PHASE_UPLOAD_START: {
                    try {
                        logger.debug("update_procedure_progress - procedure {} is PHASE_UPLOAD_START", plan.id);



                        Model_Notification notification = new Model_Notification();

                        notification
                                .setChainType(NotificationType.CHAIN_START)
                                .setNotificationId(plan.actualization_procedure.id)
                                .setImportance(NotificationImportance.LOW)
                                .setLevel(NotificationLevel.INFO);

                        notification.setText(new Notification_Text().setText("Update of Procedure "))
                                .setObject(plan.actualization_procedure)
                                .setText(new Notification_Text().setText(". Transfer firmware to device "))
                                .setObject(plan.getHardware())
                                .setText(new Notification_Text().setText(" start."))
                                .send_under_project(plan.actualization_procedure.get_project_id());

                        return;
                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }

                case PHASE_UPLOAD_DONE: {
                    try {
                        logger.debug("update_procedure_progress - procedure {} is PHASE_UPLOAD_DONE", plan.id);

                        Model_Notification notification = new Model_Notification();

                        notification
                                .setChainType(NotificationType.CHAIN_UPDATE)
                                .setNotificationId(plan.actualization_procedure.id)
                                .setImportance(NotificationImportance.LOW)
                                .setLevel(NotificationLevel.INFO);

                        notification.setText(new Notification_Text().setText("Update of Procedure "))
                                .setObject(plan.actualization_procedure)
                                .setText(new Notification_Text().setText(". Transfer firmware to "))
                                .setObject(plan.getHardware())
                                .setText(new Notification_Text().setText(" firmware file  was transferred and stored in memory."))
                                .send_under_project(plan.actualization_procedure.get_project_id());

                        return;
                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }

                // Je nejčastěji proto je nahoře
                case PHASE_UPLOADING: {
                    try {

                        logger.debug("update_procedure_progress - procedure {} is PHASE_UPLOADING", plan.id);

                        Model_Notification notification = new Model_Notification();

                        notification
                                .setChainType(NotificationType.CHAIN_UPDATE)
                                .setNotificationId(plan.actualization_procedure.id)
                                .setImportance(NotificationImportance.LOW)
                                .setLevel(NotificationLevel.INFO);

                        notification.setText(new Notification_Text().setText("Update of Procedure "))
                                .setObject(plan.actualization_procedure)
                                .setText(new Notification_Text().setText(". Transfer firmware to "))
                                .setObject(plan.getHardware())
                                .setText(new Notification_Text().setText(" progress:: " + report.percentage_progress + "%"))
                                .send_under_project(plan.actualization_procedure.get_project_id());

                        return;

                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }

                case PHASE_FLASH_ERASING: {
                    try {

                        logger.debug("update_procedure_progress - procedure {} is PHASE_FLASH_ERASING", plan.id);

                        Model_Notification notification = new Model_Notification();

                        notification
                                .setChainType(NotificationType.CHAIN_UPDATE)
                                .setNotificationId(plan.actualization_procedure.id)
                                .setImportance(NotificationImportance.LOW)
                                .setLevel(NotificationLevel.INFO);

                        notification.setText(new Notification_Text().setText("Update of Procedure "))
                                .setObject(plan.actualization_procedure)
                                .setText(new Notification_Text().setText(". We are making backup on hardware "))
                                .setObject(plan.getHardware())
                                .setText(new Notification_Text().setText("finished:: " + report.percentage_progress + "%"))
                                .setText(new Notification_Text().setText("Flash Memory Erasing..."))
                                .send_under_project(plan.actualization_procedure.get_project_id());

                        return;

                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }

                case PHASE_FLASH_ERASED: {
                    try {

                        logger.debug("update_procedure_progress - procedure {} is PHASE_FLASH_ERASED", plan.id);

                        Model_Notification notification = new Model_Notification();

                        notification
                                .setChainType(NotificationType.CHAIN_UPDATE)
                                .setNotificationId(plan.actualization_procedure.id)
                                .setImportance(NotificationImportance.LOW)
                                .setLevel(NotificationLevel.INFO);

                        notification.setText(new Notification_Text().setText("Update of Procedure "))
                                .setObject(plan.actualization_procedure)
                                .setText(new Notification_Text().setText(". We are making backup on hardware "))
                                .setObject(plan.getHardware())
                                .setText(new Notification_Text().setText("finished:: " + report.percentage_progress + "%"))
                                .setText(new Notification_Text().setText("Flash Memory Erased"))
                                .send_under_project(plan.actualization_procedure.get_project_id());

                        return;

                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }

                case PHASE_RESTARTING: {

                    logger.debug("update_procedure_progress - procedure {} is PHASE_RESTARTING", plan.id);

                    Model_Notification notification = new Model_Notification();

                    notification
                            .setChainType(NotificationType.CHAIN_UPDATE)
                            .setNotificationId(plan.actualization_procedure.id)
                            .setImportance(NotificationImportance.LOW)
                            .setLevel(NotificationLevel.INFO);

                    notification.setText(new Notification_Text().setText("Update of Procedure "))
                            .setObject(plan.actualization_procedure)
                            .setText(new Notification_Text().setText(". Transfer firmware to "))
                            .setObject(plan.getHardware())
                            .setText(new Notification_Text().setText(" finished:: " + report.percentage_progress + "%"))
                            .setText(new Notification_Text().setText("The device is just rebooting."))
                            .send_under_project(plan.actualization_procedure.get_project_id());

                    return;
                }

                case PHASE_CONNECTED_AFTER_RESTART: {

                    logger.debug("update_procedure_progress - procedure {} is PHASE_CONNECTED_AFTER_RESTART", plan.id);

                    Model_Notification notification = new Model_Notification();

                    notification
                            .setChainType(NotificationType.CHAIN_UPDATE)
                            .setNotificationId(plan.actualization_procedure.id)
                            .setImportance(NotificationImportance.LOW)
                            .setLevel(NotificationLevel.INFO);

                    notification.setText(new Notification_Text().setText("Update of Procedure "))
                            .setObject(plan.actualization_procedure)
                            .setText(new Notification_Text().setText(". Transfer firmware to "))
                            .setObject(plan.getHardware())
                            .setText(new Notification_Text().setText(" finished:: " + report.percentage_progress + "%"))
                            .setText(new Notification_Text().setText("Device Restarted successfully - system check all registers"))
                            .send_under_project(plan.actualization_procedure.get_project_id());

                    return;
                }


                case PHASE_UPDATE_DONE: {
                    try {

                        Model_Notification notification = new Model_Notification();

                        notification
                                .setChainType(NotificationType.CHAIN_END)
                                .setNotificationId(plan.actualization_procedure.id)
                                .setImportance(NotificationImportance.LOW)
                                .setLevel(NotificationLevel.INFO);
                        notification.setText(new Notification_Text().setText("Update of Procedure "))
                                .setObject(plan.actualization_procedure)
                                .setText(new Notification_Text().setText(". Transfer firmware to "))
                                .setObject(plan.getHardware())
                                .setText(new Notification_Text().setText(" successfully done."))
                                .send_under_project(plan.actualization_procedure.get_project_id());


                        logger.debug("update_procedure_progress - procedure {} is UPDATE_DONE", plan.id);


                        Model_Hardware hardware = plan.getHardware();

                        if (plan.firmware_type == FirmwareType.FIRMWARE) {

                            logger.debug("update_procedure_progress: firmware:: on HW now:: {} ",  hardware.get_actual_c_program_version().compilation.firmware_build_id);
                            logger.debug("update_procedure_progress: required by update: {} ",  plan.c_program_version_for_update.compilation.firmware_build_id);

                            hardware.actual_c_program_version = plan.c_program_version_for_update;
                            hardware.cache_actual_c_program_id = plan.c_program_version_for_update.get_c_program().id;
                            hardware.cache_actual_c_program_version_id = plan.c_program_version_for_update.id;
                            hardware.update();

                        } else if (plan.firmware_type == FirmwareType.BOOTLOADER) {

                            hardware.actual_boot_loader = plan.getBootloader();
                            hardware.cache_actual_boot_loader_id = plan.getBootloader().id;
                            hardware.update();

                        } else if (plan.firmware_type == FirmwareType.BACKUP) {

                            hardware.actual_backup_c_program_version = plan.c_program_version_for_update;
                            hardware.cache_actual_c_program_backup_id = plan.c_program_version_for_update.get_c_program().id;
                            hardware.cache_actual_c_program_backup_version_id = plan.c_program_version_for_update.id;
                            hardware.update();

                            hardware.make_log_backup_arrise_change();
                        }

                        plan.state = HardwareUpdateState.COMPLETE;
                        plan.finished = new Date();
                        plan.update();

                        return;
                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }

                case PHASE_WAITING: {
                    try {

                        logger.debug("update_procedure_progress - procedure {} is PHASE_WAITING", plan.id);

                        plan.state = HardwareUpdateState.WAITING_FOR_DEVICE;
                        plan.update();
                        Model_UpdateProcedure.getById(report.tracking_group_id).change_state(plan, plan.state);

                        return;
                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }

                case NEW_VERSION_DOESNT_MATCH: {
                    try {

                        logger.error("update_procedure_progress - procedure {} is NEW_VERSION_DOESNT_MATCH", plan.id);

                        plan.state = HardwareUpdateState.NOT_UPDATED;
                        plan.error_code = ErrorCode.NEW_VERSION_DOESNT_MATCH.error_code();
                        plan.error = ErrorCode.NEW_VERSION_DOESNT_MATCH.error_message();
                        plan.finished = new Date();
                        plan.update();
                        Model_UpdateProcedure.getById(report.tracking_group_id).change_state(plan, plan.state);

                        return;
                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }

                case ALREADY_SAME: {
                    try {

                        logger.error("update_procedure_progress - procedure {} is ALREADY_SAME", plan.id);

                        Model_Notification notification = new Model_Notification();

                        notification
                                .setChainType(NotificationType.INDIVIDUAL)
                                .setNotificationId(plan.actualization_procedure.id)
                                .setImportance(NotificationImportance.LOW)
                                .setLevel(NotificationLevel.INFO);

                        notification.setText(new Notification_Text().setText("Update of Procedure "))
                                .setObject(plan.actualization_procedure)
                                .setText(new Notification_Text().setText(" to Hardware "))
                                .setObject(plan.getHardware())
                                .setText(new Notification_Text().setText(" is done. The required firmware on the device is already running."))
                                .send_under_project(plan.actualization_procedure.get_project_id());

                        logger.error("update_procedure_progress - procedure {} is ALREADY_SAME", plan.id);

                        plan.state = HardwareUpdateState.COMPLETE;
                        plan.update();

                        Model_Hardware hardware = plan.getHardware();

                        if (plan.firmware_type == FirmwareType.FIRMWARE) {

                            hardware.actual_c_program_version = plan.c_program_version_for_update;
                            hardware.cache_actual_c_program_id = plan.c_program_version_for_update.get_c_program().id;
                            hardware.cache_actual_c_program_version_id = plan.c_program_version_for_update.id;
                            hardware.update();

                        } else if (plan.firmware_type == FirmwareType.BOOTLOADER) {

                            hardware.actual_boot_loader = plan.getBootloader();
                            hardware.cache_actual_boot_loader_id = plan.getBootloader().id;
                            hardware.update();

                        } else if (plan.firmware_type == FirmwareType.BACKUP) {
                            hardware.cache_actual_c_program_backup_id =plan.c_program_version_for_update.get_c_program().id;
                            hardware.cache_actual_c_program_backup_version_id = plan.c_program_version_for_update.id;
                            hardware.update();

                            hardware.make_log_backup_arrise_change();
                        }

                        return;
                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }

                default: {
                    throw new UnsupportedOperationException("Unknown update phase. Report: " + Json.toJson(report));
                }
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_read_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.UpdateProcedure_read.name())) return;
        getHardware().get_project().check_read_permission();
    }

    @JsonIgnore @java.beans.Transient @Override public void check_create_permission() throws _Base_Result_Exception { throw new Result_Error_NotSupportedException();}
    @JsonIgnore @java.beans.Transient @Override public void check_update_permission() throws _Base_Result_Exception { throw new Result_Error_NotSupportedException();}
    @JsonIgnore @java.beans.Transient @Override public void check_delete_permission() throws _Base_Result_Exception { throw new Result_Error_NotSupportedException();}

    public enum Permission { UpdateProcedure_read, UpdateProcedure_edit }
/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(Model_HardwareUpdate.class)
    public static Cache<UUID, Model_HardwareUpdate> cache;

    public static Model_HardwareUpdate getById(String id) {
        return getById(UUID.fromString(id));
    }

    public static Model_HardwareUpdate getById(UUID id) {

        Model_HardwareUpdate plan = cache.get(id);
        if (plan == null) {

            plan = find.byId(id);

            if (plan == null) return null;

            cache.put(id, plan);
        }

        return plan;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_HardwareUpdate> find = new Finder<>(Model_HardwareUpdate.class);
}
