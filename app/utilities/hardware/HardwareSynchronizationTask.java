package utilities.hardware;

import com.google.inject.Inject;
import models.*;
import play.libs.concurrent.HttpExecutionContext;
import utilities.enums.*;
import utilities.logger.Logger;
import utilities.notifications.NotificationService;
import utilities.synchronization.Task;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_overview_Board;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class HardwareSynchronizationTask implements Task {

    private static final Logger logger = new Logger(HardwareSynchronizationTask.class);

    private final HttpExecutionContext httpExecutionContext;
    private final HardwareService hardwareService;
    private final NotificationService notificationService;
    private final HardwareOverviewService hardwareOverviewService;

    private CompletableFuture<Void> future;

    private Model_Hardware hardware;

    private HardwareInterface hardwareInterface;

    private WS_Message_Hardware_overview_Board overview;

    @Inject
    public HardwareSynchronizationTask(HardwareService hardwareService, HttpExecutionContext httpExecutionContext,
                                       NotificationService notificationService, HardwareOverviewService hardwareOverviewService) {
        this.httpExecutionContext = httpExecutionContext;
        this.hardwareService = hardwareService;
        this.notificationService = notificationService;
        this.hardwareOverviewService = hardwareOverviewService;
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

            this.synchronizeSettings();
            this.synchronizeFirmware();
            this.synchronizeBackup();
            this.synchronizeBootloader();

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
     * Zde se kontroluje jestli je na HW to co na něm reálně být má.
     * Pokud některý parametr je jiný než se očekává, metoda ho změní (for cyklus), jenže je nutné zařízení restartovat,
     * protože může teoreticky dojít k tomu, že se register změní, ale na zařízení se to neprojeví.
     * Například změna portu www stránky pro vývojáře. Proto se vrací TRUE pokud vše sedí a připojovací procedura muže pokračovat nebo false, protože device byl restartován.
     */
    // TODO rework to something better
    private void synchronizeSettings() {

        logger.info("synchronizeSettings - checking settings for hardware, id: {}, full_id: {}", this.hardware.id, this.hardware.full_id);

        // Kontrola Skupin Hardware Groups - To není synchronizace s HW ale s Instancí HW na Homerovi
        for (UUID hardware_group_id : this.hardware.get_hardware_group_ids()) {
            // Pokud neobsahuje přidám - ale abych si ušetřil čas - nastavím rovnou celý seznam - Homer si s tím poradí
            if (overview.hardware_group_ids == null || overview.hardware_group_ids.isEmpty() || !overview.hardware_group_ids.contains(hardware_group_id)) {
                this.hardwareInterface.setHardwareGroups(this.hardware.get_hardware_group_ids(), Enum_type_of_command.SET);
                break;
            }
        }

        if (this.hardware.mac_address == null && overview.mac != null) {
            this.hardware.mac_address = overview.mac;
            this.hardware.update();
        }

        this.hardwareService.getConfigurator(this.hardware).configure(this.overview);

        logger.info("synchronizeSettings - ({}) settings for hardware synchronized", this.hardware.full_id);

        // TODO make it work somehow
        /*// Uložení do Cache paměti // PORT je synchronizován v následujícím for cyklu
        if (cache_latest_know_ip_address == null || !cache_latest_know_ip_address.equals(overview.ip)) {
            logger.warn("check_settings nastavuju jí do cache ");
            cache_latest_know_ip_address = this.overview.ip;
        }*/
    }

    private void synchronizeFirmware() {

        logger.info("synchronizeFirmware - checking firmware for hardware, id: {}, full_id: {}", this.hardware.id, this.hardware.full_id);

        if (this.hardware.getCurrentFirmware() != null && !this.hardware.getCurrentFirmware().getCompilation().firmware_build_id.equals(this.overview.binaries.firmware.build_id)) {

            Model_CProgramVersion version = Model_CProgramVersion.find.query().nullable().where().eq("compilation.firmware_build_id", this.overview.binaries.firmware.build_id).findOne();


            if (version != null && version.getProject().getId().equals(this.hardware.getProjectId())) { // TODO better permission check than same project

                this.hardware.actual_c_program_version = version;
                this.hardware.update();

            } else if (this.hardware.getProject() != null && this.hardware.bootloader_core_configuration().decision_for_default_firmware == null) {
                this.notificationService.send(this.hardware.getProject(), this.hardware.notificationUnknownFirmware());
            }
        }
    }

    private void synchronizeBackup() {

        logger.info("synchronizeBackup - checking backup for hardware, id: {}, full_id: {}", this.hardware.id, this.hardware.full_id);

        if (this.hardware.getCurrentBackup() != null && !this.hardware.getCurrentBackup().getCompilation().firmware_build_id.equals(overview.binaries.backup.build_id)) {
            Model_CProgramVersion version = Model_CProgramVersion.find.query().nullable().where().eq("compilation.firmware_build_id", overview.binaries.backup.build_id).findOne();
            if (version != null && version.getProject().getId().equals(hardware.getProjectId())) { // TODO better permission check than same project
                this.hardware.actual_backup_c_program_version = version;
                this.hardware.update();
            }
        }
    }

    private void synchronizeBootloader() {

        logger.info("synchronizeBootloader - checking bootloader for hardware, id: {}, full_id: {}", this.hardware.id, this.hardware.full_id);

        if (this.hardware.getCurrentBootloader() == null || !overview.binaries.bootloader.build_id.equals(this.hardware.getCurrentBootloader().version_identifier)) {
            Model_BootLoader bootLoader = Model_BootLoader.find.query().nullable().where().eq("hardware_type.id", this.hardware.getHardwareType().getId()).eq("version_identifier", overview.binaries.bootloader.build_id).findOne();
            if (bootLoader != null) {
                this.hardware.actual_boot_loader = bootLoader;
                this.hardware.update();
            }
        }
    }
}
