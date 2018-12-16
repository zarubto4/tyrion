package utilities.hardware;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import io.ebean.Expr;
import models.*;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import utilities.document_mongo_db.document_objects.DM_Board_Bootloader_DefaultConfig;
import utilities.enums.*;
import utilities.hardware.update.UpdateService;
import utilities.logger.Logger;
import utilities.synchronization.Task;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_overview_Board;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_set_settings;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class HardwareSynchronizationTask implements Task {

    private static final Logger logger = new Logger(HardwareSynchronizationTask.class);

    private final HttpExecutionContext httpExecutionContext;
    private final HardwareService hardwareService;
    private final UpdateService updateService;

    private CompletableFuture<Void> future;

    private Model_Hardware hardware;

    private HardwareInterface hardwareInterface;

    private WS_Message_Hardware_overview_Board overview;

    @Inject
    public HardwareSynchronizationTask(HardwareService hardwareService, UpdateService updateService, HttpExecutionContext httpExecutionContext) {
        this.httpExecutionContext = httpExecutionContext;
        this.hardwareService = hardwareService;
        this.updateService = updateService;
    }

    @Override
    public UUID getId() {
        return this.hardware.getId();
    }

    @Override
    public CompletionStage<Void> start() {

        logger.info("start - synchronization task begins");
        return future = CompletableFuture.runAsync(() -> {
            if (this.hardware == null || this.hardwareInterface == null) {
                throw new RuntimeException("You must set hardware before the task start.");
            }

            this.overview = this.hardwareInterface.getOverview();

            System.out.println("Overview for synchronization: \n" + this.hardwareInterface.getOverview().prettyPrint());

            if(!this.overview.online_status) {
                logger.info("start - Device is offline, there is no Overview");
                return;
            }

            this.synchronizeHardwareGroup();

            this.synchronizeHardwareAlias();
            this.synchronizeMacAddress();
            this.synchronizeMIPAddress();

            this.synchronizeSettings();
            this.synchronizeFirmware();
            this.synchronizeBackup();
            this.synchronizeBootloader();

            // Find open updates and call update
            this.findUpdatesForFirmware();
            this.findUpdatesForBackup();
            this.findUpdatesForBootloader();

        }, this.httpExecutionContext.current());
    }

    @Override
    public void stop() {
        this.future.cancel(true);
    }

    public void setHardware(Model_Hardware hardware) {
        if (this.hardware == null) {
            this.hardware = hardware;
            this.hardwareInterface = this.hardwareService.getInterface(this.hardware);
        } else {
            throw new RuntimeException("Cannot set hardware twice.");
        }
    }

    /**
     * Synchronize List of Hardware Groups on Devices
     * @return
     */
    private void synchronizeHardwareGroup() {
        try {

            for (UUID hardware_group_id : this.hardware.get_hardware_group_ids()) {
                // Pokud neobsahuje přidám - ale abych si ušetřil čas - nastavím rovnou celý seznam - Homer si s tím poradí
                if (overview.hardware_group_ids == null || overview.hardware_group_ids.isEmpty() || !overview.hardware_group_ids.contains(hardware_group_id)) {
                    this.hardwareInterface.setHardwareGroups(this.hardware.get_hardware_group_ids(), Enum_type_of_command.SET);
                    break;
                }
            }


        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    private void synchronizeHardwareAlias() {
        if (!this.hardware.name.equals( this.overview.alias)) {
            logger.warn("check_settings - inconsistent state: alias");
            this.hardwareInterface.setAlias(this.hardware.name);
        }
    }

    private void synchronizeMacAddress() {
        if (this.hardware.mac_address == null && overview.mac != null) {
            this.hardware.mac_address = overview.mac;
            this.hardware.update();
        }
    }

    private void synchronizeMIPAddress() {
        if (this.hardware.cache_latest_know_ip_address == null || !hardware.cache_latest_know_ip_address.equals(overview.ip)) {
            logger.warn("check_settings nastavuju jí do cache ");
            hardware.cache_latest_know_ip_address = this.overview.ip;
        }
    }

    /**
     * Zde se kontroluje jestli je na HW to co na něm reálně být má.
     * Pokud některý parametr je jiný než se očekává, metoda ho změní (for cyklus), jenže je nutné zařízení restartovat,
     * protože může teoreticky dojít k tomu, že se register změní, ale na zařízení se to neprojeví.
     * Například změna portu www stránky pro vývojáře. Proto se vrací TRUE pokud vše sedí a připojovací procedura muže pokračovat nebo false, protože device byl restartován.
     */
    private boolean synchronizeSettings() {

        // ---- ZDE ještě nedělám žádné změny na HW!!! -----

        /*
            Tato smyčka prochází všechny položky v objektu DM_Board_Bootloader_DefaultConfig jeden po druhém a pak hledá
            ty samé config parametry v příchozím objektu - pokud je nazelzne následě porovná očekávanou ohnotu od té
            reálné a popřípadě upraví na hardwaru.

            Pokud se něco změnilo - nastaví se change register na true

         */
        DM_Board_Bootloader_DefaultConfig configuration = this.hardware.bootloader_core_configuration();
        boolean changeSettings = false; // Pokud došlo ke změně
        boolean changeConfig = false;

        try {

            for (Field configField : configuration.getClass().getFields()) {

                String configFieldName = configField.getName();

                try {
                    Field reportedField = overview.getClass().getField(configFieldName);

                    Object configValue = configField.get(configuration);
                    Object reportedValue = reportedField.get(overview);

                    // If values are same do nothing
                    if (configValue != reportedValue) {

                        // If change was requested (is pending) update the hw setting, otherwise update database info
                        if (configuration.pending.contains(configFieldName)) {
                            Class type = reportedField.getType();

                            ObjectNode message = null;

                            if (type.equals(Boolean.class)) {

                                message = new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this.hardware), configFieldName, (Boolean) configField.get(configuration));

                            } else if (type.equals(String.class)) {

                                message = new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this.hardware), configFieldName, (String) configField.get(configuration));

                            } else if (type.equals(Integer.class)) {

                                message = new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this.hardware), configFieldName, (Integer) configField.get(configuration));

                            } else {
                                throw new NoSuchFieldException();
                            }

                            changeSettings = true;
                            changeConfig = true;
                            // TODO this.hardwareInterface.send ???
                            configuration.pending.remove(configFieldName);

                        } else {
                            configField.set(configuration, reportedValue);
                            changeConfig = true;
                        }
                    } else if (configuration.pending.contains(configFieldName)) {
                        configuration.pending.remove(configFieldName);
                        changeConfig = true;
                    }

                } catch (NoSuchFieldException e) {
                    // Nothing
                }
            }

        } catch (Exception e) {
            logger.internalServerError(e);
        }

        if (changeConfig) {
            this.hardware.update_bootloader_configuration(configuration);
        }

        return changeSettings;
    }

    /* Firmware */
    private void synchronizeFirmware() {

        logger.info("synchronizeFirmware - checking firmware for hardware, id: {}, full_id: {}", this.hardware.id, this.hardware.full_id);

        // Nastavíme co na Hardware je pokud to známe

        if (this.hardware.getCurrentFirmware() == null || !this.hardware.getCurrentFirmware().getCompilation().firmware_build_id.equals(this.overview.binaries.firmware.build_id)) {

            Model_CProgramVersion version = Model_CProgramVersion.find.query().where().eq("compilation.firmware_build_id", this.overview.binaries.firmware.build_id).findOne();

            if(version != null && version.get_c_program().getProjectId().equals(hardware.getProjectId())) {
                    this.hardware.actual_c_program_version = version;
                    this.hardware.update();
            } else {
                // Program is from different project
                this.hardware.notification_board_no_firmware();
            }
        }


    }

    private void findUpdatesForFirmware() {

       Model_HardwareUpdate firmware_update = Model_HardwareUpdate.find.query().nullable().where()
                .eq("hardware.id", this.hardware.getId())
                .eq("firmware_type", FirmwareType.FIRMWARE)
                .or(Expr.eq("state", HardwareUpdateState.PENDING), Expr.eq("state", HardwareUpdateState.RUNNING))
                .orderBy("")
                .findOne();

       if(firmware_update == null) return;

       if(this.hardware.getCurrentFirmware() != null && this.hardware.getCurrentFirmware().getCompilation().firmware_build_id.equals(  firmware_update.c_program_version_for_update.getCompilation().firmware_build_id )) {
           firmware_update.state = HardwareUpdateState.COMPLETE;
           firmware_update.update();
        } else {
           this.updateService.update(firmware_update);
       }
    }


    /* Backup */
    private void synchronizeBackup() {

        logger.info("synchronizeBackup - checking backup for hardware, id: {}, full_id: {}", this.hardware.id, this.hardware.full_id);

        if (this.hardware.getCurrentBackup() != null && !this.hardware.getCurrentFirmware().getCompilation().firmware_build_id.equals(this.overview.binaries.firmware.build_id)) {
            Model_CProgramVersion version = Model_CProgramVersion.find.query().where().eq("compilation.firmware_build_id", this.overview.binaries.firmware.build_id).findOne();
            if(version != null && version.get_c_program().getProjectId().equals(hardware.getProjectId())) {
                this.hardware.actual_backup_c_program_version = version;
                this.hardware.update();
            }
        }
    }

    private void findUpdatesForBackup() {

        Model_HardwareUpdate firmware_update = Model_HardwareUpdate.find.query().nullable().where()
                .eq("hardware.id", this.hardware.getId())
                .eq("firmware_type", FirmwareType.BACKUP)
                .or(Expr.eq("state", HardwareUpdateState.PENDING), Expr.eq("state", HardwareUpdateState.RUNNING))
                .orderBy("")
                .findOne();

        if(firmware_update == null) return;

        if(this.hardware.getCurrentBackup() != null && this.hardware.getCurrentBackup().getCompilation().firmware_build_id.equals(  firmware_update.c_program_version_for_update.getCompilation().firmware_build_id )) {
            firmware_update.state = HardwareUpdateState.COMPLETE;
            firmware_update.update();
        } else {
            this.updateService.update(firmware_update);
        }
    }


    /* Boot Loader */
    private void synchronizeBootloader() {

        logger.info("synchronizeBootloader - checking bootloader for hardware, id: {}, full_id: {}", this.hardware.id, this.hardware.full_id);

        if (hardware.getCurrentBootloader() == null || !overview.binaries.bootloader.build_id.equals(hardware.getCurrentBootloader().version_identifier)) {
            Model_BootLoader actual_boot_loader = Model_BootLoader.find.query().nullable().where().eq("hardware_type.id", hardware.getHardwareType().getId()).eq("version_identifier", overview.binaries.bootloader.build_id).findOne();

            if(actual_boot_loader != null) {
                hardware.actual_boot_loader = actual_boot_loader;
                hardware.update();
            }
        }

    }

    private void findUpdatesForBootloader() {

        Model_HardwareUpdate firmware_update = Model_HardwareUpdate.find.query().nullable().where()
                .eq("hardware.id", this.hardware.getId())
                .eq("firmware_type", FirmwareType.BOOTLOADER)
                .or(Expr.eq("state", HardwareUpdateState.PENDING), Expr.eq("state", HardwareUpdateState.RUNNING))
                .orderBy("")
                .findOne();

        if(firmware_update == null) return;

        if(this.hardware.getCurrentBootloader() != null && this.hardware.getCurrentBootloader().version_identifier .equals(  firmware_update.bootloader.version_identifier)) {
            firmware_update.state = HardwareUpdateState.COMPLETE;
            firmware_update.update();
        } else {
            this.updateService.update(firmware_update);
        }
    }
}
