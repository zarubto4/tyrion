package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.libs.Json;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.*;
import utilities.errors.ErrorCode;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.model.UnderProject;
import utilities.models_update_echo.EchoHandler;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.swagger.output.*;
import websocket.messages.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Progress;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Objekt slouží k aktualizačnímu plánu jednotlivých zařízení!
 *
 */

@Entity
@ApiModel(description = "Model of HardwareUpdate",
        value = "HardwareUpdate")
@Table(name="HardwareUpdate")
public class Model_HardwareUpdate extends BaseModel implements Permissible, UnderProject {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_HardwareUpdate.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                 @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_UpdateProcedure actualization_procedure;

                                            @ApiModelProperty(required = true,
                                                    value = "UNIX time in ms",
                                                    example = "1466163478925")  public Date date_of_finish;

              @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                    public Model_Hardware hardware; // Deska k aktualizaci
                                            @ApiModelProperty(required = true)  public FirmwareType firmware_type;          // Typ Firmwaru

                                                                                // Aktualizace je vázána buď na verzi C++ kodu nebo na soubor, nahraný uživatelem
    /** OR **/  @JsonIgnore @ManyToOne(fetch = FetchType.EAGER)                 public Model_CProgramVersion c_program_version_for_update; // C_program k aktualizaci
    /** OR **/  @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                  public Model_BootLoader bootloader;                      // Když nahrávám Firmware
    /** OR **/  @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                  public Model_Blob binary_file;                     // Soubor, když firmware nahrává uživatel sám mimo flow

                                                                                public HardwareUpdateState state;
                                                                    @JsonIgnore public Integer count_of_tries;                         // Počet celkovbých pokusu doručit update (změny z wait to progres atd..

    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty("Only if state is critical_error or Homer record some error")  public String error;
    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty("Only if state is critical_error or Homer record some error")  public Integer error_code;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = false, readOnly = true)
    public UpdateType type_of_update () {
        try {
            return getActualizationProcedure().type_of_update;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty  @ApiModelProperty(readOnly = true, required = false)
    public UUID actualization_procedure_id(){
        try {
            return getActualizationProcedureId();
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty
    public Date date_of_planing() {
        try {
            return getActualizationProcedure().date_of_planing;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty
    public Date created() {
        try {
            return getActualizationProcedure().created;
        } catch(Exception e) {
            logger.internalServerError(e);
            return null;
        }
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
            bootloader_update_detail.hardware_type_name = cached_bootLoader.getHardwareType().name;
            bootloader_update_detail.hardware_type_id = cached_bootLoader.getHardwareTypeId();

            return bootloader_update_detail;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public Swagger_Short_Reference hardware() {
        try {
            return getHardware().ref();
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @ApiModelProperty(required = false, value = "Is visible only if user send own binary file ( OR state for c_program_detail)")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty
    public Model_Blob binary_file_detail() {
        try{
            return binary_file;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID getComponentId() {
        switch (this.firmware_type) {
            case FIRMWARE:
            case BACKUP: return this.c_program_version_for_update.id;
            case BOOTLOADER: return this.getBootloader().id;
            default: return null;
        }
    }

    @JsonIgnore @Override
    public Model_Project getProject() {
        return getHardware().getProject();
    }

    @JsonIgnore
    public Model_Hardware getHardware() {
        return isLoaded("hardware") ? hardware : Model_Hardware.find.query().nullable().where().eq("updates.id", id).findOne();
    }

    @JsonIgnore
    public UUID getActualizationProcedureId() {

        if (idCache().get(Model_UpdateProcedure.class) == null) {
            idCache().add(Model_UpdateProcedure.class, (UUID) Model_UpdateProcedure.find.query().where().eq("updates.id", id).ne("deleted", true).select("id").findSingleAttribute());
        }

        return idCache().get(Model_UpdateProcedure.class);
    }

    @JsonIgnore
    public Model_UpdateProcedure getActualizationProcedure() {
        return isLoaded("actualization_procedure") ? actualization_procedure : Model_UpdateProcedure.find.query().where().eq("updates.id", id).findOne();
    }

    @JsonIgnore
    public UUID getBootloaderId() {
        if (idCache().get(Model_BootLoader.class) == null) {
            idCache().add(Model_BootLoader.class, (UUID) Model_BootLoader.find.query().where().eq("updates.id", id).ne("deleted", true).select("id").findSingleAttribute());
        }
        return idCache().get(Model_BootLoader.class);
    }

    @JsonIgnore
    public Model_BootLoader getBootloader() {
        return isLoaded("bootloader") ? bootloader : Model_BootLoader.find.query().nullable().where().eq("updates.id", id).findOne();
    }

    @JsonIgnore
    public Swagger_UpdatePlan_brief_for_homer get_brief_for_update_homer_server() {
        try {

            Swagger_UpdatePlan_brief_for_homer brief_for_homer = new Swagger_UpdatePlan_brief_for_homer();
            brief_for_homer.update_procedure_id = getActualizationProcedureId().toString();
            brief_for_homer.hardware_update_id = id.toString();
            brief_for_homer.uuid_ids.add(getHardware().id);

            Swagger_UpdatePlan_brief_for_homer_BinaryComponent binary = new Swagger_UpdatePlan_brief_for_homer_BinaryComponent();
            binary.firmware_type = firmware_type;

            brief_for_homer.binary = binary;

            logger.debug("get_brief_for_update_homer_server:: getActualizationProcedure: ID:  {} ", getActualizationProcedureId());
            logger.debug("get_brief_for_update_homer_server:: getActualizationProcedure: Type Of Update:  {} ", getActualizationProcedure().type_of_update );

            if (getActualizationProcedure().type_of_update == UpdateType.MANUALLY_BY_USER_INDIVIDUAL) {
                brief_for_homer.progress_subscribe = true;
            }

            if (firmware_type == FirmwareType.FIRMWARE || firmware_type == FirmwareType.BACKUP) {

                if(c_program_version_for_update != null) {

                    binary.download_id              = c_program_version_for_update.compilation.id;
                    binary.build_id                 = c_program_version_for_update.compilation.firmware_build_id;
                    binary.program_name             = c_program_version_for_update.get_c_program().name.length() > 32 ? c_program_version_for_update.get_c_program().name.substring(0, 32) : c_program_version_for_update.get_c_program().name;
                    binary.program_version_name     = c_program_version_for_update.name.length() > 32 ? c_program_version_for_update.name.substring(0, 32) : c_program_version_for_update.name;
                    binary.compilation_lib_version  = c_program_version_for_update.compilation.firmware_version_lib;
                    binary.time_stamp               = c_program_version_for_update.compilation.firmware_build_datetime;

                    // Update přímo z kompilace souboru bez archivace verze
                } else if(binary_file  != null && binary_file.c_compilations_binary_file != null) {

                    binary.download_id              = binary_file.c_compilations_binary_file.id;
                    binary.build_id                 = binary_file.c_compilations_binary_file.firmware_build_id;
                    binary.program_name             = "Manual Update";
                    binary.program_version_name     = "Manual Update";
                    binary.compilation_lib_version  = binary_file.c_compilations_binary_file.firmware_version_lib;
                    binary.time_stamp               = binary_file.c_compilations_binary_file.firmware_build_datetime;

                    // Update manuálně nahraným souborem bez jakkékoliv vazby
                    // nutné vyseparovat id z binárky
                } else if(binary_file  != null) {

                    throw new IllegalAccessException("get_brief_for_update_homer_server:: ¨Firmware is FIRMWARE or BACKUP but there is no binary_file or file!");
                    /*
                    binary.download_id              = binary_file.id;
                    binary.build_id                 = "TODO";           // TODO! https://stackoverflow.com/questions/5600422/method-to-find-string-inside-of-the-text-file-then-getting-the-following-lines
                    binary.program_name             = "Manual Update";
                    binary.program_version_name     = "Manual Update";
                    binary.compilation_lib_version  = "TODO";           // TODO!
                    binary.time_stamp               = binary_file.created;
                    */

                } else {
                    throw new IllegalAccessException("get_brief_for_update_homer_server:: ¨Firmware is FIRMWARE or BACKUP but there is no c_program_version_for_update or file!");
                }

            } else if (firmware_type == FirmwareType.BOOTLOADER) {

                Model_BootLoader cached_bootLoader = getBootloader();
                if (cached_bootLoader != null) {

                    binary.download_id = cached_bootLoader.id;
                    binary.build_id = cached_bootLoader.version_identifier;
                    binary.program_name = cached_bootLoader.name.length() > 32 ? cached_bootLoader.name.substring(0, 32) : cached_bootLoader.name;
                    binary.program_version_name = cached_bootLoader.version_identifier.length() > 32 ? cached_bootLoader.version_identifier.substring(0, 32) : cached_bootLoader.version_identifier;
                    binary.time_stamp = cached_bootLoader.created;

                } else {
                    throw new  IllegalAccessException("get_brief_for_update_homer_server:: ¨Firmware is BOOTLOADER but there is no bootloader or file!");
                }

            } else {
                logger.internalServerError(new IllegalAccessException("get_brief_for_update_homer_server:: nsupported type of Enum_Firmware_type or not set firmware_type in Model_CProgramUpdatePlan"));
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

        if (this.state == null) this.state = HardwareUpdateState.PENDING;
        super.save();
    }

    @JsonIgnore @Override
    public void update() {

        super.update();

        Model_UpdateProcedure procedure = getActualizationProcedure();
        if (procedure != null) {
            if (procedure.state == Enum_Update_group_procedure_state.NOT_START_YET || procedure.state == Enum_Update_group_procedure_state.IN_PROGRESS) {

                if (this.state == HardwareUpdateState.OBSOLETE
                        || this.state == HardwareUpdateState.COMPLETE
                        || this.state == HardwareUpdateState.FAILED
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
    }

    @JsonIgnore @Override
    public boolean delete() {
        this.state = HardwareUpdateState.CANCELED;
        this.date_of_finish = new Date();
        super.update();

        return true;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* SERVER WEBSOCKET CONTROLLING OF HOMER SERVER--------------------------------------------------------------------------*/

    private class Model_CProgramFakeBackup {}
    private class Model_CProgramVersionFakeBackup {}

    @JsonIgnore @Transient
    public static void update_procedure_progress(WS_Message_Hardware_UpdateProcedure_Progress report) {
        try {

            Enum_HardwareHomerUpdate_state phase = report.get_phase();
            if (phase == null) {
                logger.error("update_procedure_progress " + report.phase + " is not recognize in Json!");
                return;
            }

            Model_HardwareUpdate plan = Model_HardwareUpdate.find.byId(report.tracking_id);
            if (plan == null) {
                logger.error("update_procedure_progress :: Plan is null Return null NullPointerException");
                throw new NullPointerException("Plan id" + report.tracking_id + " not found!");
            }

            logger.debug("update_procedure_progress :: {} Progress: {}", plan.id, report.percentage_progress);


            // Pokud se vrátí fáze špatně - ukončuji celý update
            if (report.error_message != null || report.error_code != null) {
                logger.warn("update_procedure_progress  Update Fail! Device ID: {}, update procedure: {}", plan.getHardware().id, plan.id);

                plan.date_of_finish = new Date();
                plan.state = HardwareUpdateState.FAILED;
                plan.error_code = report.error_code;
                plan.error = report.error + report.error_message;
                plan.update();
                Model_UpdateProcedure.find.byId(report.tracking_group_id).change_state(plan, plan.state);

                Model_Notification notification = new Model_Notification();

                notification
                        .setChainType(NotificationType.CHAIN_END)
                        .setNotificationId(plan.getActualizationProcedureId())
                        .setImportance(NotificationImportance.LOW)
                        .setLevel(NotificationLevel.ERROR);

                notification.setText(new Notification_Text().setText("Update of Procedure "))
                        .setObject(plan.getActualizationProcedure())
                        .setText(new Notification_Text().setText(". Transfer firmware to device "))
                        .setObject(plan.getHardware())
                        .setText(new Notification_Text().setText(" failed. Error Code " + report.error_code + "."))
                        .send_under_project(plan.getActualizationProcedure().get_project_id());


                return;
            }

            logger.debug("update_procedure_progress :: Checking phase: Phase {} ", phase);
            // Fáze jsou volány jen tehdá, když má homer instrukce je zasílat
            switch (phase) {

                case PHASE_UPLOAD_START: {
                    try {
                        logger.debug("update_procedure_progress - procedure {} is PHASE_UPLOAD_START", plan.id);

                        Model_Notification notification = new Model_Notification();

                        notification
                                .setChainType(NotificationType.CHAIN_START)
                                .setNotificationId(plan.getActualizationProcedureId())
                                .setImportance(NotificationImportance.LOW)
                                .setLevel(NotificationLevel.INFO);

                        notification.setText(new Notification_Text().setText("Update of Procedure "))
                                .setObject(plan.getActualizationProcedure())
                                .setText(new Notification_Text().setText(". Transfer firmware to device "))
                                .setObject(plan.getHardware())
                                .setText(new Notification_Text().setText(" start."))
                                .send_under_project(plan.getActualizationProcedure().get_project_id());

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
                                .setNotificationId(plan.getActualizationProcedureId())
                                .setImportance(NotificationImportance.LOW)
                                .setLevel(NotificationLevel.INFO);

                        notification.setText(new Notification_Text().setText("Update of Procedure "))
                                .setObject(plan.getActualizationProcedure())
                                .setText(new Notification_Text().setText(". Transfer firmware to "))
                                .setObject(plan.getHardware())
                                .setText(new Notification_Text().setText(" firmware file  was transferred and stored in memory."))
                                .send_under_project(plan.getActualizationProcedure().get_project_id());

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
                                .setNotificationId(plan.getActualizationProcedureId())
                                .setImportance(NotificationImportance.LOW)
                                .setLevel(NotificationLevel.INFO);

                        notification.setText(new Notification_Text().setText("Update of Procedure "))
                                .setObject(plan.getActualizationProcedure())
                                .setText(new Notification_Text().setText(". Transfer firmware to "))
                                .setObject(plan.getHardware())
                                .setText(new Notification_Text().setText(" progress:: " + report.percentage_progress + "%"))
                                .send_under_project(plan.getActualizationProcedure().get_project_id());

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
                                .setNotificationId(plan.getActualizationProcedureId())
                                .setImportance(NotificationImportance.LOW)
                                .setLevel(NotificationLevel.INFO);

                        notification.setText(new Notification_Text().setText("Update of Procedure "))
                                .setObject(plan.getActualizationProcedure())
                                .setText(new Notification_Text().setText(". We are making backup on hardware "))
                                .setObject(plan.getHardware())
                                .setText(new Notification_Text().setText("finished:: " + report.percentage_progress + "%"))
                                .setText(new Notification_Text().setText("Flash Memory Erasing..."))
                                .send_under_project(plan.getActualizationProcedure().get_project_id());

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
                                .setNotificationId(plan.getActualizationProcedureId())
                                .setImportance(NotificationImportance.LOW)
                                .setLevel(NotificationLevel.INFO);

                        notification.setText(new Notification_Text().setText("Update of Procedure "))
                                .setObject(plan.getActualizationProcedure())
                                .setText(new Notification_Text().setText(". We are making backup on hardware "))
                                .setObject(plan.getHardware())
                                .setText(new Notification_Text().setText("finished:: " + report.percentage_progress + "%"))
                                .setText(new Notification_Text().setText("Flash Memory Erased"))
                                .send_under_project(plan.getActualizationProcedure().get_project_id());

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
                            .setNotificationId(plan.getActualizationProcedureId())
                            .setImportance(NotificationImportance.LOW)
                            .setLevel(NotificationLevel.INFO);

                    notification.setText(new Notification_Text().setText("Update of Procedure "))
                            .setObject(plan.getActualizationProcedure())
                            .setText(new Notification_Text().setText(". Transfer firmware to "))
                            .setObject(plan.getHardware())
                            .setText(new Notification_Text().setText(" finished:: " + report.percentage_progress + "%"))
                            .setText(new Notification_Text().setText("The device is just rebooting."))
                            .send_under_project(plan.getActualizationProcedure().get_project_id());

                    return;
                }

                case PHASE_CONNECTED_AFTER_RESTART: {

                    logger.debug("update_procedure_progress - procedure {} is PHASE_CONNECTED_AFTER_RESTART", plan.id);

                    Model_Notification notification = new Model_Notification();

                    notification
                            .setChainType(NotificationType.CHAIN_UPDATE)
                            .setNotificationId(plan.getActualizationProcedureId())
                            .setImportance(NotificationImportance.LOW)
                            .setLevel(NotificationLevel.INFO);

                    notification.setText(new Notification_Text().setText("Update of Procedure "))
                            .setObject(plan.getActualizationProcedure())
                            .setText(new Notification_Text().setText(". Transfer firmware to "))
                            .setObject(plan.getHardware())
                            .setText(new Notification_Text().setText(" finished:: " + report.percentage_progress + "%"))
                            .setText(new Notification_Text().setText("Device Restarted successfully - system check all registers"))
                            .send_under_project(plan.getActualizationProcedure().get_project_id());

                    return;
                }


                case PHASE_UPDATE_DONE: {
                    try {

                        logger.debug("update_procedure_progress :: UPDATE DONE :: for Hardware: {} ", plan.getHardware().name);

                        Model_Notification notification = new Model_Notification();

                        notification
                                .setChainType(NotificationType.CHAIN_END)
                                .setNotificationId(plan.getActualizationProcedureId())
                                .setImportance(NotificationImportance.LOW)
                                .setLevel(NotificationLevel.INFO);
                        notification.setText(new Notification_Text().setText("Update of Procedure "))
                                .setObject(plan.getActualizationProcedure())
                                .setText(new Notification_Text().setText(". Transfer firmware to "))
                                .setObject(plan.getHardware())
                                .setText(new Notification_Text().setText(" successfully done."))
                                .send_under_project(plan.getActualizationProcedure().get_project_id());


                        logger.debug("update_procedure_progress - procedure {} is UPDATE_DONE", plan.id);


                        Model_Hardware hardware = plan.getHardware();

                        logger.warn("update_procedure_progress :: UPDATE DONE :: plan.firmware_type {} ", plan.firmware_type);

                        if (plan.firmware_type == FirmwareType.FIRMWARE) {

                            logger.debug("update_procedure_progress: firmware:: on HW id: {} now:: {} ", hardware.id,  hardware.getCurrentFirmware() == null ? " nothing by DB" : hardware.getCurrentFirmware().compilation.firmware_build_id);

                            if(plan.c_program_version_for_update != null ) {

                                logger.debug("update_procedure_progress: required by update: {} ", plan.c_program_version_for_update.compilation.firmware_build_id);

                                logger.debug("update_procedure_progress: Na Hardwaru je teď CProgram " + hardware.get_actual_c_program().name);
                                logger.debug("update_procedure_progress: Na Hardwaru je teď CProgram Verze " + hardware.getCurrentFirmware().name + " id: " + hardware.getCurrentFirmware().id);


                                logger.debug("update_procedure_progress: Co by tam ale mělo za chvíli být je CProgram " + plan.c_program_version_for_update.get_c_program().name);
                                logger.debug("update_procedure_progress: Co by tam ale mělo za chvíli být je CProgram Verze " + plan.c_program_version_for_update.name + " id: " + plan.c_program_version_for_update.id);

                                hardware.actual_c_program_version = plan.c_program_version_for_update;

                                logger.debug("update_procedure_progress: Na Hardwar jsem nastavil " + hardware.actual_c_program_version.name);

                                hardware.idCache().removeAll(Model_CProgram.class);
                                hardware.idCache().removeAll(Model_CProgramVersion.class);
                                logger.debug("update_procedure_progress: Udělal jsem clean ");
                                logger.debug("update_procedure_progress: zkontroluji clean: " + hardware.idCache().get(Model_CProgram.class));
                                logger.debug("update_procedure_progress: zkontroluji clean: " + hardware.idCache().get(Model_CProgramVersion.class));


                                hardware.idCache().add(Model_CProgram.class, plan.c_program_version_for_update.get_c_program().id);
                                hardware.idCache().add(Model_CProgramVersion.class, plan.c_program_version_for_update.id);

                                logger.debug("update_procedure_progress: Před updatem Na Hardwaru je teď CProgram " + hardware.get_actual_c_program().name);
                                logger.debug("update_procedure_progress: Před updatem Na Hardwaru je teď CProgram Verze " + hardware.getCurrentFirmware().name + " id: " + hardware.getCurrentFirmware().id);

                                hardware.update();

                                logger.debug("update_procedure_progress: ještě blbý check: " + hardware.actual_c_program_version.name);

                                hardware.idCache().add(Model_CProgram.class, hardware.actual_c_program_version.get_c_program().id);
                                hardware.idCache().add(Model_CProgramVersion.class, hardware.actual_c_program_version.id);

                                logger.debug("update_procedure_progress: PO updatu Na Hardwaru je teď CProgram " + hardware.get_actual_c_program().name);
                                logger.debug("update_procedure_progress: PO updatu Na Hardwaru je teď CProgram Verze " + hardware.getCurrentFirmware().name + " id: " + hardware.getCurrentFirmware().id);

                            } else {

                                logger.debug("update_procedure_progress: required by update: {} ", plan.binary_file.c_compilations_binary_file.firmware_build_id);
                                hardware.idCache().removeAll(Model_CProgram.class);
                                hardware.idCache().removeAll(Model_CProgramVersion.class);

                                // TODO nějak poznamenat na Hardwaru
                            }

                        } else if (plan.firmware_type == FirmwareType.BOOTLOADER) {

                            hardware.idCache().removeAll(Model_Hardware.Model_hardware_update_update_in_progress_bootloader.class);
                            hardware.idCache().removeAll(Model_BootLoader.class);

                            hardware.actual_boot_loader = plan.getBootloader();
                            hardware.update();

                            hardware.idCache().add(Model_BootLoader.class, plan.getBootloaderId());


                        } else if (plan.firmware_type == FirmwareType.BACKUP) {

                            hardware.actual_backup_c_program_version = plan.c_program_version_for_update;

                            hardware.idCache().removeAll(Model_CProgramFakeBackup.class);
                            hardware.idCache().removeAll(Model_CProgramVersionFakeBackup.class);

                            hardware.idCache().add(Model_CProgramFakeBackup.class, plan.c_program_version_for_update.get_c_program().id);
                            hardware.idCache().add(Model_CProgramVersionFakeBackup.class, plan.c_program_version_for_update.id);
                            hardware.update();

                            hardware.make_log_backup_arrise_change();
                        }

                        plan.state = HardwareUpdateState.COMPLETE;
                        plan.date_of_finish = new Date();
                        plan.update();

                        EchoHandler.addToQueue(new WSM_Echo(Model_Hardware.class, hardware.get_project_id(), hardware.id));

                        return;
                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }

                case NEW_VERSION_DOESNT_MATCH: {
                    try {

                        logger.error("update_procedure_progress - procedure {} is NEW_VERSION_DOESNT_MATCH", plan.id);

                        plan.state = HardwareUpdateState.FAILED;
                        plan.error_code = ErrorCode.NEW_VERSION_DOESNT_MATCH.error_code();
                        plan.error = ErrorCode.NEW_VERSION_DOESNT_MATCH.error_message();
                        plan.date_of_finish = new Date();
                        plan.update();
                        Model_UpdateProcedure.find.byId(report.tracking_group_id).change_state(plan, plan.state);

                        return;
                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }

                case ALREADY_SAME: {
                    try {

                        logger.trace("update_procedure_progress - procedure {} is ALREADY_SAME", plan.id);

                        if ( plan.getActualizationProcedure().type_of_update == UpdateType.MANUALLY_BY_USER_INDIVIDUAL) {
                            Model_Notification notification = new Model_Notification();

                            notification
                                    .setChainType(NotificationType.INDIVIDUAL)
                                    .setNotificationId(plan.getActualizationProcedureId())
                                    .setImportance(NotificationImportance.LOW)
                                    .setLevel(NotificationLevel.INFO);

                            notification.setText(new Notification_Text().setText("Update of Procedure "))
                                    .setObject(plan.getActualizationProcedure())
                                    .setText(new Notification_Text().setText(" to Hardware "))
                                    .setObject(plan.getHardware())
                                    .setText(new Notification_Text().setText(" is done. The required firmware on the device is already running."))
                                    .send_under_project(plan.getActualizationProcedure().get_project_id());

                        }

                        plan.state = HardwareUpdateState.COMPLETE;
                        plan.date_of_finish = new Date();
                        plan.update();

                        Model_Hardware hardware = plan.getHardware();

                        if (plan.firmware_type == FirmwareType.FIRMWARE) {

                            if(hardware.getCurrentFirmware().id == null || !hardware.get_actual_c_program_version_id().equals(plan.c_program_version_for_update.id)) {
                                hardware.actual_c_program_version = plan.c_program_version_for_update;
                                hardware.idCache().removeAll(Model_CProgram.class);
                                hardware.idCache().removeAll(Model_CProgramVersion.class);

                                if(plan.c_program_version_for_update != null) {
                                    hardware.idCache().add(Model_CProgram.class, plan.c_program_version_for_update.get_c_program().id);
                                    hardware.idCache().add(Model_CProgramVersion.class, plan.c_program_version_for_update.id);
                                } else if(plan.binary_file != null) {
                                    // TODO for file
                                }

                                hardware.update();
                            }

                        } else if (plan.firmware_type == FirmwareType.BOOTLOADER) {

                            hardware.actual_boot_loader = plan.getBootloader();
                            hardware.idCache().removeAll(Model_Hardware.Model_hardware_update_update_in_progress_bootloader.class);
                            hardware.idCache().removeAll(Model_BootLoader.class);
                            hardware.idCache().add(Model_BootLoader.class, plan.getBootloader().id);
                            hardware.update();

                        } else if (plan.firmware_type == FirmwareType.BACKUP) {

                            if (hardware.getCurrentBackup().id == null || !hardware.getCurrentBackup().id.equals(plan.c_program_version_for_update.id)) {

                                hardware.idCache().removeAll(Model_CProgramFakeBackup.class);
                                hardware.idCache().removeAll(Model_CProgramVersionFakeBackup.class);

                                if(plan.c_program_version_for_update != null) {
                                    hardware.idCache().add(Model_CProgramFakeBackup.class, plan.c_program_version_for_update.get_c_program().id);
                                    hardware.idCache().add(Model_CProgramVersionFakeBackup.class, plan.c_program_version_for_update.id);
                                } else if(plan.binary_file != null) {
                                    // TODO for file
                                }

                                hardware.update();

                                hardware.make_log_backup_arrise_change();
                            }

                        } else {
                            logger.debug("update_procedure_progress: nebylo třeba vůbec nic měnit.");
                        }

                        EchoHandler.addToQueue(new WSM_Echo(Model_Hardware.class, hardware.get_project_id(), hardware.id));

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

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.HARDWARE_UPDATE;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.READ, Action.UPDATE, Action.DELETE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_HardwareUpdate.class)
    public static CacheFinder<Model_HardwareUpdate> find = new CacheFinder<>(Model_HardwareUpdate.class);
}
