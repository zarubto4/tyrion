package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import exceptions.NotFoundException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.*;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.model.UnderProject;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.swagger.output.*;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of HardwareUpdate",
        value = "HardwareUpdate")
@Table(name="HardwareUpdate")
public class Model_HardwareUpdate extends BaseModel implements Permissible, UnderProject {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_HardwareUpdate.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                @ApiModelProperty(required = true,
                                                        value = "UNIX time",
                                                        dataType = "integer",
                                                        example = "1466163471") public Date finished;
                                                @ApiModelProperty(required = true,
                                                        value = "UNIX time",
                                                        dataType = "integer",
                                                        example = "1466163471") public Date planned;

                                @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)  public Model_Hardware hardware;     // Deska k aktualizaci
                                            @ApiModelProperty(required = true)  public FirmwareType firmware_type;  // Typ Firmwaru

                                                                                // Aktualizace je vázána buď na verzi C++ kodu nebo na soubor, nahraný uživatelem
    /** OR **/  @JsonIgnore @ManyToOne(fetch = FetchType.EAGER)                 public Model_CProgramVersion c_program_version_for_update;  // C_program k aktualizaci
    /** OR **/  @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                  public Model_BootLoader bootloader;                         // Když nahrávám Firmware
    /** OR **/  @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                  public Model_Blob binary_file;                              // Soubor, když firmware nahrává uživatel sám mimo flow

                                                                                public HardwareUpdateState state;
                                                                                public UpdateType type;
                                                                    @JsonIgnore public Integer count_of_tries;                              // Počet celkovbých pokusu doručit update (změny z wait to progres atd...

    /**
     * Naprosto nekonfliktní hodnoty které pouze a výlučně slouží pro frontend
     * Ukládáme zde "filtrační" ID
     */
    @JsonIgnore public UUID tracking_id;


    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty("Only if state is critical_error or Homer record some error")  public String error;
    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty("Only if state is critical_error or Homer record some error")  public Integer error_code; // ERROR CODE from HOMER SERVER ABOUT UPDATE PROCEDURE

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/


    @ApiModelProperty(required = false, value = "Is visible only if update is for Firmware or Backup")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty
    public Swagger_C_Program_Update_program c_program_detail() {
        try {

            if (c_program_version_for_update == null) return null;

            Swagger_C_Program_Update_program c_program_detail = new Swagger_C_Program_Update_program();
            c_program_detail.c_program_id = c_program_version_for_update.getProgram().id;
            c_program_detail.c_program_program_name = c_program_version_for_update.getProgram().name;
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

            Model_BootLoader bootloader = getBootloader();

            if (bootloader == null) return null;

            Swagger_Bootloader_Update_program bootloaderUpdate = new Swagger_Bootloader_Update_program();
            bootloaderUpdate.bootloader_id = bootloader.id;
            bootloaderUpdate.bootloader_name = bootloader.name;
            bootloaderUpdate.version_identificator = bootloader.version_identifier;
            bootloaderUpdate.hardware_type_name = bootloader.getHardwareType().name;
            bootloaderUpdate.hardware_type_id = bootloader.getHardwareType().id;

            return bootloaderUpdate;

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

    @JsonProperty @ApiModelProperty(required = false, readOnly = true, value = "Only if its under Release Update")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Swagger_Short_Reference release_update() {
        try {

            if (tracking_id != null) {
                return Model_HardwareReleaseUpdate.find.byId(this.tracking_id).ref();
            }
        } catch (NotFoundException e) {
          // nothing
        } catch (Exception e) {
            logger.internalServerError(e);
        }
        return null;
    }

    @JsonProperty @ApiModelProperty(required = false, readOnly = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Swagger_Short_Reference instance_snapshot() {
        try {
            if (tracking_id != null) {
                return Model_InstanceSnapshot.find.byId(this.tracking_id).ref();
            }
        } catch (NotFoundException e) {
            // nothing
        } catch (Exception e) {
            logger.internalServerError(e);
        }
        return null;
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
        return isLoaded("hardware") ? hardware : Model_Hardware.find.query().where().eq("updates.id", id).findOne();
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
            brief_for_homer.tracking_group_id = tracking_id;
            brief_for_homer.tracking_id = this.getId();
            brief_for_homer.uuid_ids.add(getHardware().getId());

            Swagger_UpdatePlan_brief_for_homer_BinaryComponent binary = new Swagger_UpdatePlan_brief_for_homer_BinaryComponent();
            binary.firmware_type = firmware_type;

            brief_for_homer.binary = binary;

            // TODO only if developer analytics
            brief_for_homer.progress_subscribe = true;

            if (firmware_type == FirmwareType.FIRMWARE || firmware_type == FirmwareType.BACKUP) {

                if (c_program_version_for_update != null) {

                    binary.download_id              = c_program_version_for_update.getCompilation().id;
                    binary.build_id                 = c_program_version_for_update.getCompilation().firmware_build_id;
                    binary.program_name             = c_program_version_for_update.getProgram().name.length() > 32 ? c_program_version_for_update.getProgram().name.substring(0, 32) : c_program_version_for_update.getProgram().name;
                    binary.program_version_name     = c_program_version_for_update.name.length() > 32 ? c_program_version_for_update.name.substring(0, 32) : c_program_version_for_update.name;
                    binary.compilation_lib_version  = c_program_version_for_update.getCompilation().firmware_version_lib;
                    binary.time_stamp               = c_program_version_for_update.getCompilation().firmware_build_datetime;

                    // Update přímo z kompilace souboru bez archivace verze
                } else if (binary_file  != null && binary_file.c_compilations_binary_file != null) {

                    binary.download_id              = binary_file.c_compilations_binary_file.id;
                    binary.build_id                 = binary_file.c_compilations_binary_file.firmware_build_id;
                    binary.program_name             = "Manual Update";
                    binary.program_version_name     = "Manual Update";
                    binary.compilation_lib_version  = binary_file.c_compilations_binary_file.firmware_version_lib;
                    binary.time_stamp               = binary_file.c_compilations_binary_file.firmware_build_datetime;

                    // Update manuálně nahraným souborem bez jakkékoliv vazby
                    // nutné vyseparovat id z binárky
                } else if (binary_file  != null) {

                    throw new IllegalAccessException("get_brief_for_update_homer_server:: Firmware is FIRMWARE or BACKUP but there is no binary_file or file!");
                    /*
                    binary.download_id              = binary_file.id;
                    binary.build_id                 = binary_file.c_compilations_binary_file.firmware_build_id;
                    binary.program_name             = "Manual Update";
                    binary.program_version_name     = "Manual Update";
                    binary.compilation_lib_version  = "Unknown";
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
        count_of_tries = 0;

        if (this.state == null) this.state = HardwareUpdateState.PENDING;
        super.save();
    }

    @JsonIgnore @Override
    public boolean delete() {
        this.state = HardwareUpdateState.CANCELED;
        this.finished = new Date();
        super.update();

        return true;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    public Model_Notification notificationUpdateStart() {
        return new Model_Notification()
                .setChainType(NotificationType.CHAIN_START)
                .setNotificationId(this.getId())
                .setImportance(NotificationImportance.LOW)
                .setLevel(NotificationLevel.INFO)
                .setText(new Notification_Text().setText("Update "))
                .setObject(this)
                .setText(new Notification_Text().setText(" of hardware "))
                .setObject(this.getHardware())
                .setText(new Notification_Text().setText(" has started."));
    }

    public Model_Notification notificationUpdateEnd() {
        return new Model_Notification()
                .setChainType(NotificationType.CHAIN_END)
                .setNotificationId(this.getId())
                .setImportance(NotificationImportance.LOW)
                .setLevel(NotificationLevel.INFO)
                .setText(new Notification_Text().setText("Update "))
                .setObject(this)
                .setText(new Notification_Text().setText(" of hardware "))
                .setObject(this.getHardware())
                .setText(new Notification_Text().setText(" was successful."));
    }

    public Model_Notification notificationUploading(Integer percents) {
        return new Model_Notification()
                .setChainType(NotificationType.CHAIN_UPDATE)
                .setNotificationId(this.getId())
                .setImportance(NotificationImportance.LOW)
                .setLevel(NotificationLevel.INFO)
                .setText(new Notification_Text().setText("Update "))
                .setObject(this)
                .setText(new Notification_Text().setText(" of hardware "))
                .setObject(this.getHardware())
                .setText(new Notification_Text().setText(" - progress: " + percents + "%."));
    }

    public Model_Notification notificationUploadDone() {
        return new Model_Notification()
                .setChainType(NotificationType.CHAIN_UPDATE)
                .setNotificationId(this.getId())
                .setImportance(NotificationImportance.LOW)
                .setLevel(NotificationLevel.INFO)
                .setText(new Notification_Text().setText("Update "))
                .setObject(this)
                .setText(new Notification_Text().setText(" of hardware "))
                .setObject(this.getHardware())
                .setText(new Notification_Text().setText(" was uploaded into hardware."));
    }

    public Model_Notification notificationBufferErasing() {
        return new Model_Notification()
                .setChainType(NotificationType.CHAIN_UPDATE)
                .setNotificationId(this.getId())
                .setImportance(NotificationImportance.LOW)
                .setLevel(NotificationLevel.INFO)
                .setText(new Notification_Text().setText("Update "))
                .setObject(this)
                .setText(new Notification_Text().setText(" of hardware "))
                .setObject(this.getHardware())
                .setText(new Notification_Text().setText(" - buffer is erasing."));
    }

    public Model_Notification notificationBufferErased() {
        return new Model_Notification()
                .setChainType(NotificationType.CHAIN_UPDATE)
                .setNotificationId(this.getId())
                .setImportance(NotificationImportance.LOW)
                .setLevel(NotificationLevel.INFO)
                .setText(new Notification_Text().setText("Update "))
                .setObject(this)
                .setText(new Notification_Text().setText(" of hardware "))
                .setObject(this.getHardware())
                .setText(new Notification_Text().setText(" - buffer is erased, transfer will start."));
    }

    public Model_Notification notificationRestarting() {
        return new Model_Notification()
                .setChainType(NotificationType.CHAIN_UPDATE)
                .setNotificationId(this.getId())
                .setImportance(NotificationImportance.LOW)
                .setLevel(NotificationLevel.INFO)
                .setText(new Notification_Text().setText("Update "))
                .setObject(this)
                .setText(new Notification_Text().setText(" of hardware "))
                .setObject(this.getHardware())
                .setText(new Notification_Text().setText(" - hardware is restarting."));
    }

    public Model_Notification notificationAfterRestart() {
        return new Model_Notification()
                .setChainType(NotificationType.CHAIN_UPDATE)
                .setNotificationId(this.getId())
                .setImportance(NotificationImportance.LOW)
                .setLevel(NotificationLevel.INFO)
                .setText(new Notification_Text().setText("Update "))
                .setObject(this)
                .setText(new Notification_Text().setText(" of hardware "))
                .setObject(this.getHardware())
                .setText(new Notification_Text().setText(" - hardware has reconnected after restart."));
    }

    public Model_Notification notificationUpdateFailed(Integer errorCode) {
        return new Model_Notification()
                .setChainType(NotificationType.CHAIN_END)
                .setNotificationId(this.getId())
                .setImportance(NotificationImportance.LOW)
                .setLevel(NotificationLevel.ERROR)
                .setText(new Notification_Text().setText("Update "))
                .setObject(this)
                .setText(new Notification_Text().setText(" of hardware "))
                .setObject(this.getHardware())
                .setText(new Notification_Text().setText(" failed" + (errorCode != null ? " with code: " + errorCode + "." : ".")));
    }

    public Model_Notification notificationAlreadySame() {
        return new Model_Notification()
                .setChainType(NotificationType.INDIVIDUAL)
                .setNotificationId(this.getId())
                .setImportance(NotificationImportance.LOW)
                .setLevel(NotificationLevel.INFO)
                .setText(new Notification_Text().setText("Update "))
                .setObject(this)
                .setText(new Notification_Text().setText(" of hardware "))
                .setObject(this.getHardware())
                .setText(new Notification_Text().setText(" - " + (this.firmware_type.equals(FirmwareType.FIRMWARE) ? "firmware" : this.firmware_type.equals(FirmwareType.BOOTLOADER) ? "bootloader" : "backup") + " has failed. "));
    }

    public Model_Notification notificationRestoreFromBackup() {
        return new Model_Notification()
                .setChainType(NotificationType.CHAIN_END)
                .setNotificationId(this.getId())
                .setImportance(NotificationImportance.NORMAL)
                .setLevel(NotificationLevel.ERROR)
                .setText(new Notification_Text().setText("Update "))
                .setObject(this)
                .setText(new Notification_Text().setText(" of hardware "))
                .setObject(this.getHardware())
                .setText(new Notification_Text().setText(" - " + (this.firmware_type.equals(FirmwareType.FIRMWARE) ? "firmware" : this.firmware_type.equals(FirmwareType.BOOTLOADER) ? "bootloader" : "backup") + " is already on the hardware."))
                .setNewLine()
                .setText(new Notification_Text().setText("New Firmware is critically broken and the WatchDog restarted device and restore it from backup. Please check it!"))
                ;

    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

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
