package utilities.hardware;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.*;
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

            this.synchronizeSettings();
            this.synchronizeFirmware();
            this.synchronizeBackup();

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
    private boolean synchronizeSettings() {

        // ---- ZDE ještě nedělám žádné změny na HW!!! -----

        // Kontrola Skupin Hardware Groups - To není synchronizace s HW ale s Instancí HW na Homerovi
        for (UUID hardware_group_id : this.hardware.get_hardware_group_ids()) {
            // Pokud neobsahuje přidám - ale abych si ušetřil čas - nastavím rovnou celý seznam - Homer si s tím poradí
            if (overview.hardware_group_ids == null || overview.hardware_group_ids.isEmpty() || !overview.hardware_group_ids.contains(hardware_group_id)) {
                this.hardwareInterface.setHardwareGroups(this.hardware.get_hardware_group_ids(), Enum_type_of_command.SET);
                break;
            }
        }

        // TODO make it work somehow
        /*// Uložení do Cache paměti // PORT je synchronizován v následujícím for cyklu
        if (cache_latest_know_ip_address == null || !cache_latest_know_ip_address.equals(overview.ip)) {
            logger.warn("check_settings nastavuju jí do cache ");
            cache_latest_know_ip_address = this.overview.ip;
        }*/


        // ---- ZDE už dělám změny na HW!! -----

        // Kontrola Aliasu
        if (this.hardware.name != this.overview.alias) {
            logger.warn("check_settings - inconsistent state: alias");
            this.hardwareInterface.setAlias(this.hardware.name);
        }

        // Synchronizace mac_adressy pokud k tomu ještě nedošlo
        if (this.hardware.mac_address == null && overview.mac != null) {
            this.hardware.mac_address = overview.mac;
            this.hardware.update();
        }

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

    private void synchronizeFirmware() {

        logger.info("synchronizeFirmware - checking firmware for hardware, id: {}, full_id: {}", this.hardware.id, this.hardware.full_id);

        if (this.hardware.database_synchronize) {
            logger.info("synchronizeFirmware - ({}) synchronizing hardware with the database", this.hardware.full_id);

            Model_CProgramVersion firmware = this.hardware.getCurrentFirmware();

            // Set default firmware, if there is no firmware for this hardware
            if (firmware == null) {
                Model_CProgramVersion defaultVersion = this.hardware.getHardwareType().get_main_c_program().default_main_version;
                this.hardware.actual_c_program_version = defaultVersion;
                this.hardware.actual_backup_c_program_version = defaultVersion;
                this.hardware.update();

                firmware = defaultVersion;
            }

            if (!firmware.getCompilation().firmware_build_id.equals(this.overview.binaries.firmware.build_id)) {
                // TODO mark as unstable if device is running backup
                this.updateService.update(this.hardware, firmware, FirmwareType.FIRMWARE);
            }

        } else {
            logger.info("synchronizeFirmware - ({}) synchronizing database with the hardware", this.hardware.full_id);
            // TODO
        }
    }

    private void synchronizeBackup() {

        logger.info("synchronizeBackup - checking backup for hardware, id: {}, full_id: {}", this.hardware.id, this.hardware.full_id);

        if (this.hardware.database_synchronize && !this.hardware.backup_mode) {
            logger.info("synchronizeBackup - ({}) synchronizing hardware with the database", this.hardware.full_id);

            Model_CProgramVersion backup = this.hardware.getCurrentBackup();

            if (backup == null) {
                // TODO find some default backup
                logger.warn("synchronizeBackup - TODO backup is no set for the device");
                return;
            }

            if (backup.getCompilation().firmware_build_id.equals(overview.binaries.backup.build_id)) {
                this.updateService.update(this.hardware, backup, FirmwareType.BACKUP);
            }

        } else {
            logger.info("synchronizeBackup - ({}) synchronizing database with the hardware", this.hardware.full_id);

            if (this.overview.binaries.backup != null && overview.binaries.backup.build_id != null && !overview.binaries.backup.build_id.equals("")) {

                if (this.hardware.getCurrentBackup() != null && this.hardware.getCurrentBackup().getCompilation().firmware_build_id.equals(overview.binaries.backup.build_id)) {
                    logger.debug("synchronizeBackup - ({}) already synchronized");
                } else {

                    logger.debug("synchronizeBackup - ({}) synchronizing, finding backup reported by hardware");

                    Model_CProgramVersion version = Model_CProgramVersion.find.query().nullable().where().eq("compilation.firmware_build_id", overview.binaries.backup.build_id).findOne();
                    if (version != null) {
                        logger.debug("synchronizeBackup - ({}) synchronizing, backup found");

                        this.hardware.actual_backup_c_program_version = version;
                        this.hardware.update();

                        logger.debug("synchronizeBackup - ({}) synchronized");
                    }
                }
            }
        }
    }

    // TODO
    private void synchronizeBootloader() {

        logger.info("synchronizeBootloader - checking bootloader for hardware, id: {}, full_id: {}", this.hardware.id, this.hardware.full_id);

        if (this.hardware.database_synchronize) {

        } else {

        }

        // Nastavím bootloader který na hardwaru je

        /*if (get_actual_bootloader() == null || overview.binaries.bootloader.build_id.equals(this.actual_bootloader().version_identifier)) {

            actual_boot_loader = Model_BootLoader.find.query().where().eq("hardware_type.id", this.hardware_type().id).eq("version_identifier", overview.binaries.bootloader.build_id).findOne();
            update();

            logger.trace("check_bootloader -: Actual bootloader_id by DB not recognized and its updated :: ", overview.binaries.bootloader.build_id);
        }



        // Vylistuji seznam úkolů k updatu
        List<Model_HardwareUpdate> firmware_plans = Model_HardwareUpdate.find.query().where().eq("hardware.id", this.id)
                .disjunction()
                .add(Expr.eq("state", HardwareUpdateState.NOT_YET_STARTED))
                .add(Expr.eq("state", HardwareUpdateState.IN_PROGRESS))
                .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_IS_OFFLINE))
                .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_NEVER_CONNECTED))
                .endJunction()
                .eq("firmware_type", FirmwareType.BOOTLOADER)
                .le("actualization_procedure.date_of_planing", new Date())
                .order().desc("actualization_procedure.date_of_planing")
                .findList();

        // Kontrola Bootloader a přepsání starých
        // Je žádoucí přepsat všechny předhozí update plány - ale je nutné se podívat jestli nejsou rozdílné!
        // To jest pokud mám 2 updaty firmwaru pak ten starší zahodím
        // Ale jestli mám udpate firmwaru a backupu pak k tomu dojít nesmí!
        // Poměrně krkolomné řešení a HNUS kod - ale chyba je výjmečná a stává se jen sporadicky těsně před nebo po restartu serveru
        if (firmware_plans.size() > 1) {
            logger.trace("check_bootloader:: firmware_plans.size() > 1 ");
            for (int i = 1; i < firmware_plans.size(); i++) {
                logger.trace("check_bootloader:: OBSOLETE procedure ID {}", firmware_plans.get(i).id);
                firmware_plans.get(i).state = HardwareUpdateState.OBSOLETE;
                firmware_plans.get(i).update();
            }
        }

        if (!firmware_plans.isEmpty()) {

            Model_HardwareUpdate plan = firmware_plans.get(0);

            if (plan.getHardware().getCurrentBootloader() != null) {

                // Verze se rovnají
                if (plan.getHardware().getCurrentBootloader().version_identifier.equals(plan.getBootloader().version_identifier)) {

                    logger.debug("check_bootloader - up to date, procedure is done");
                    plan.state = HardwareUpdateState.COMPLETE;
                    plan.finished = new Date();
                    plan.update();

                    this.actual_boot_loader = plan.getBootloader();
                    this.idCache().add(Model_BootLoader.class, plan.getBootloader().id);
                    //this.cache_actual_boot_loader_id = plan.getBootloader().id;
                    update();

                } else {

                    logger.debug("check_bootloader - need update, system starts a new update, number of tries {}", plan.count_of_tries);
                    plan.state = HardwareUpdateState.NOT_YET_STARTED;
                    plan.count_of_tries++;
                    plan.update();
                    execute_update_procedure(plan.getActualizationProcedure());
                }

            } else {

                logger.debug("check_bootloader:: no bootloader, system starts a update again");
                plan.state = HardwareUpdateState.NOT_YET_STARTED;
                plan.count_of_tries++;
                plan.update();

                execute_update_procedure(plan.getActualizationProcedure());
            }

            return;
        }

        // Pokud na HW opravdu žádný bootloader není a není ani vytvořená update procedura
        if (get_actual_bootloader() == null && firmware_plans.isEmpty()) {

            logger.debug("check_bootloader:: noo default bootloader on hardware - required automatic update");

            // Zkontroluji jestli tam nějaká verze už je!
            Model_BootLoader bootloader = Model_BootLoader.find.query().nullable().where().eq("hardware_type.id", this.hardware_type().id).eq("version_identifier", overview.binaries.bootloader.build_id).findOne();

            if (bootloader != null ) {
                logger.debug("check_bootloader:: Bootloader identificator {} recognized and found in database", overview.binaries.bootloader.build_id);
                actual_boot_loader = bootloader;
                update();
                return;
            }

            if (getHardwareType().main_boot_loader() == null) {
                logger.error("check_bootloader::Main Bootloader for Type Of Board {} is not set for update device {}", this.getHardwareType().name, this.id);
                return;
            }

            List<WS_Help_Hardware_Pair> b_pairs = new ArrayList<>();

            WS_Help_Hardware_Pair b_pair = new WS_Help_Hardware_Pair();
            b_pair.hardware = this;

            if (this.get_actual_bootloader() == null) b_pair.bootLoader = getHardwareType().main_boot_loader();
            else b_pair.bootLoader = this.get_actual_bootloader();

            b_pairs.add(b_pair);

            logger.debug("check_bootloader:: - creating update procedure for bootloader");
            Model_UpdateProcedure procedure = create_update_procedure(FirmwareType.BOOTLOADER, UpdateType.AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE, b_pairs);
            procedure.execute_update_procedure();
        }*/
    }
}
