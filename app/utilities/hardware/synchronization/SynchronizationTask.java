package utilities.hardware.synchronization;

import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.NotFoundException;
import io.ebean.Expr;
import models.*;
import utilities.document_mongo_db.document_objects.DM_Board_Bootloader_DefaultConfig;
import utilities.enums.*;
import utilities.hardware.HardwareInterface;
import utilities.hardware.update.UpdateService;
import utilities.logger.Logger;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_overview_Board;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_set_settings;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Help_Hardware_Pair;

import java.lang.reflect.Field;
import java.util.*;

public class SynchronizationTask {

    private static final Logger logger = new Logger(SynchronizationTask.class);

    private final UpdateService updateService;

    private final Model_Hardware hardware;

    private final HardwareInterface hardwareInterface;

    private WS_Message_Hardware_overview_Board overview;

    public SynchronizationTask(Model_Hardware hardware, HardwareInterface hardwareInterface, UpdateService updateService) {
        this.hardware = hardware;
        this.hardwareInterface = hardwareInterface;
        this.updateService = updateService;
    }

    public void start() {

        logger.info("start - synchronization task begins");

        this.overview = this.hardwareInterface.getOverview();


    }

    public void cancel() {

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

        // Uložení do Cache paměti // PORT je synchronizován v následujícím for cyklu
        if (cache_latest_know_ip_address == null || !cache_latest_know_ip_address.equals(overview.ip)) {
            logger.warn("check_settings nastavuju jí do cache ");
            cache_latest_know_ip_address = this.overview.ip;
        }


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

    private void synchronizeFirmware(WS_Message_Hardware_overview_Board overview) {
        try {

            logger.info("synchronizeFirmware - checking firmware for hardware, id: {}, full_id: {}", this.hardware.id, this.hardware.full_id);

            Model_CProgramVersion firmware = this.hardware.getCurrentFirmware();

            if (firmware == null || !firmware.compilation.firmware_build_id.equals(this.overview.binaries.firmware.build_id)) {
                this.updateService.update();
            }

            // Vylistuji seznam úkolů k updatu
            List<Model_HardwareUpdate> firmware_plans = Model_HardwareUpdate.find.query().where().eq("hardware.id", this.id)
                    .disjunction()
                    .add(Expr.eq("state", HardwareUpdateState.NOT_YET_STARTED))
                    .add(Expr.eq("state", HardwareUpdateState.IN_PROGRESS))
                    .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_IS_OFFLINE))
                    .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_NEVER_CONNECTED))
                    .add(Expr.eq("state", HardwareUpdateState.CRITICAL_ERROR))
                    .endJunction()
                    .eq("firmware_type", FirmwareType.FIRMWARE.name())
                    .lt("actualization_procedure.date_of_planing", new Date())
                    .order().desc("actualization_procedure.date_of_planing")
                    .findList();

            // Kontrola Firmwaru a přepsání starých
            // Je žádoucí přepsat všechny předhozí update plány - ale je nutné se podívat jestli nejsou rozdílné!
            // To jest pokud mám 2 updaty firmwaru pak ten starší zahodím
            // Ale jestli mám udpate firmwaru a backupu pak k tomu dojít nesmí!
            // Poměrně krkolomné řešení a HNUS kod - ale chyba je výjmečná a stává se jen sporadicky těsně před nebo po restartu serveru
            if (firmware_plans.size() > 1) {
                logger.warn("check_firmware: Device id: {} : there is more than one active firmware_plans. Its time to override it!", this.id);
                for (int i = 1; i < firmware_plans.size(); i++) {
                    firmware_plans.get(i).state = HardwareUpdateState.OBSOLETE;
                    firmware_plans.get(i).update();
                }
            }

            if (!firmware_plans.isEmpty()) {

                logger.warn("check_firmware: Device id: {} : existují nedokončené procedury", this.id);

                Model_HardwareUpdate plan = firmware_plans.get(0);

                logger.warn("Plan:: {} status: {} ", plan.id, plan.state);

                // Mám shodu firmwaru oproti očekávánemů
                if (get_actual_c_program_version() != null) {
                    logger.warn("Firmware:: Device id: {} :   --------------------------------------------------------------------", this.id);
                    logger.warn("Firmware:: Device id: {} :  Co aktuálně je na HW podle Tyriona??:: CProgram Name {} Version Name {} Build Id {} ", this.id, get_actual_c_program_version().get_c_program().name, get_actual_c_program_version().name,  get_actual_c_program_version().compilation.firmware_build_id);
                    logger.warn("Firmware:: Device id: {} :  Co aktuálně je na HW podle Homera??:: {}", this.id, overview.binaries.firmware.build_id);
                    logger.warn("Firmware:: Device id: {} :  Co očekává nedokončená procedura??:: CProgram Name {} Version Name {} Build Id {} ", this.id, plan.c_program_version_for_update.get_c_program().name, plan.c_program_version_for_update.name, plan.c_program_version_for_update.compilation.firmware_build_id);

                    // Verze se rovnají
                    if (overview.binaries.firmware.build_id.equals(plan.c_program_version_for_update.compilation.firmware_build_id)) {

                        logger.debug("check_firmware:: Device id: {} : verze se shodují - tím pádem je procedura dokončená a uzavírám", this.id);
                        this.actual_c_program_version = plan.c_program_version_for_update;

                        this.idCache().add(Model_CProgram.class,  this.actual_c_program_version.get_c_program().id);
                        this.idCache().add(Model_CProgramVersion.class,  this.actual_c_program_version.id);

                        this.update();

                        logger.debug("check_firmware:: Device id: {} : - up to date, procedure is done", this.id);
                        plan.state = HardwareUpdateState.COMPLETE;
                        plan.date_of_finish = new Date();
                        plan.update();
                        return;

                    } else {
                        logger.debug("check_firmware verze se neshodují");
                        logger.debug("check_firmware - need update, system starts a update again, number of tries {}", plan.count_of_tries);
                        plan.state = HardwareUpdateState.NOT_YET_STARTED;
                        plan.count_of_tries++;
                        plan.update();
                        execute_update_plan(plan);
                    }

                } else {

                    logger.debug("check_firmware - no firmware, system starts a new update");
                    plan.state = HardwareUpdateState.NOT_YET_STARTED;
                    plan.count_of_tries++;
                    plan.update();

                    execute_update_plan(plan);
                }

                return;
            } else {
                logger.debug("check_firmware:: Device id: {}  Neexistují nedokončené procedury", this.id);
            }

            logger.debug("check_firmware:: Device id: {} Totální přehled v Json: \n {} \n", this.id, this.prettyPrint());

            // Nemám Updaty - ale verze se neshodují
            if (get_actual_c_program_version() != null && firmware_plans.isEmpty()) {

                logger.debug("check_firmware - current firmware according to Tyrion: C_Program Name {} Version {} build_id {} ", get_actual_c_program_version().get_c_program().name, get_actual_c_program_version().name, get_actual_c_program_version().compilation.firmware_build_id);
                logger.debug("check_firmware - current firmware according to Homer: C_Program Name {} Version {} build_id {} ", overview.binaries.firmware.usr_name, overview.binaries.firmware.usr_version, overview.binaries.firmware.build_id);

                if (!get_actual_c_program_version().compilation.firmware_build_id.equals(overview.binaries.firmware.build_id)) {
                    // Na HW není to co by na něm mělo být.
                    logger.debug("check_firmware - no update procedures found, but versions are not equal");
                    logger.debug("check_firmware - Different firmware versions versus database");

                    if (get_backup_c_program_version() != null && get_backup_c_program_version().compilation.firmware_build_id.equals(overview.binaries.bootloader.build_id)) {

                        logger.warn("check_firmware - We have problem with firmware version. Backup is now running");

                        // Notifikace uživatelovi
                        this.notification_board_unstable_actual_firmware_version(get_actual_c_program_version());

                        // Označit firmare za nestabilní
                        get_actual_c_program_version().compilation.status = CompilationStatus.UNSTABLE;
                        get_actual_c_program_version().compilation.update();

                        // Přemapovat hardware
                        actual_c_program_version = get_backup_c_program_version();

                        this.idCache().add(Model_CProgram.class, actual_c_program_version.id);                // C Program
                        this.idCache().add(Model_CProgramVersion.class, actual_c_program_version.id);

                        this.idCache().add(Model_Hardware.Model_CProgramFakeBackup.class, actual_c_program_version.id);      // Backup
                        this.idCache().add(Model_Hardware.Model_CProgramVersionFakeBackup.class, actual_c_program_version.id);

                        this.update();

                        return;
                    }

                    // Backup to není
                    else {

                        logger.warn("check_firmware - Wrong version on hardware - or null version on hardware");
                        logger.warn("check_firmware - Now System set Default Firmware or Firmware by Database!!!");

                        // Nastavuji nový systémový update
                        List<WS_Help_Hardware_Pair> b_pairs = new ArrayList<>();

                        WS_Help_Hardware_Pair b_pair = new WS_Help_Hardware_Pair();
                        b_pair.hardware = this;
                        b_pair.c_program_version = get_actual_c_program_version();

                        b_pairs.add(b_pair);

                        Model_UpdateProcedure procedure = create_update_procedure(FirmwareType.FIRMWARE, UpdateType.AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE, b_pairs);
                        procedure.execute_update_procedure();

                        return;
                    }

                } else {
                    // Na HW je to co by na něm podle databáze mělo být.
                    logger.debug("check_firmware - hardware is up to date");
                    return;
                }
            }

            // Set Defualt Program protože žádný teď nemáš
            if (get_actual_c_program_version() == null && firmware_plans.isEmpty()) {

                logger.debug("check_firmware:: Device id: {} Actual firmware_id is not recognized by the DB - Device has not firmware required by Tyrion", this.id);
                logger.debug("check_firmware:: Device id: {} Tyrion will try to find Default C Program Main Version for this type of hardware. If is it set, Tyrion will update device to starting state", this.id);

                // Ověřím - jestli nemám nově nahraný firmware na Hardwaru (to je ten co je teď výcohí firmware pro aktuální typ hardwaru)
                if (overview.binaries != null && overview.binaries.firmware != null
                        && overview.binaries.firmware.build_id != null
                        && getHardwareType().get_main_c_program().default_main_version != null
                        && getHardwareType().get_main_c_program().default_main_version.compilation.firmware_build_id != null
                        && overview.binaries.firmware.build_id.equals(getHardwareType().get_main_c_program().default_main_version.compilation.firmware_build_id)
                ) {

                    logger.debug("check_firmware:: Device id: {}  hardware is brand new, but already has required default hardware type firmware", this.id);

                    // SET MAIN
                    this.actual_c_program_version = getHardwareType().get_main_c_program().default_main_version;

                    // Clean Cache
                    this.idCache().removeAll(Model_CProgram.class);
                    this.idCache().removeAll(Model_CProgramVersion.class);

                    this.idCache().add(Model_CProgram.class, getHardwareType().get_main_c_program().id);
                    this.idCache().add(Model_CProgramVersion.class, getHardwareType().get_main_c_program().default_main_version.id);

                    // SET BACKUP
                    this.actual_backup_c_program_version = getHardwareType().get_main_c_program().default_main_version; // Udělám rovnou zálohu, protože taková by tam měla být

                    // Clean Cache
                    this.idCache().removeAll(Model_Hardware.Model_CProgramFakeBackup.class);
                    this.idCache().removeAll(Model_Hardware.Model_CProgramVersionFakeBackup.class);

                    this.idCache().add(Model_Hardware.Model_CProgramFakeBackup.class, getHardwareType().get_main_c_program().id);
                    this.idCache().add(Model_Hardware.Model_CProgramVersionFakeBackup.class, getHardwareType().get_main_c_program().default_main_version.id);

                    this.update();
                    return;
                }

                // Nastavím default firmware podle schématu Tyriona!
                // Defaultní firmware je v v backandu určený výchozí program k typu desky.
                if (getHardwareType().get_main_c_program() != null && getHardwareType().get_main_c_program().default_main_version != null) {

                    logger.debug("check_firmware:: Device id: {} Yes, Default Version for Type Of Device {} is set", this.id , getHardwareType().name);

                    List<WS_Help_Hardware_Pair> b_pairs = new ArrayList<>();

                    WS_Help_Hardware_Pair b_pair = new WS_Help_Hardware_Pair();
                    b_pair.hardware = this;
                    b_pair.c_program_version = getHardwareType().get_main_c_program().default_main_version;

                    b_pairs.add(b_pair);

                    this.notification_board_not_databased_version();

                    Model_UpdateProcedure procedure = create_update_procedure(FirmwareType.FIRMWARE, UpdateType.AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE, b_pairs);
                    procedure.execute_update_procedure();

                } else {
                    logger.error("check_firmware:: Device id: {} Attention please! This is not a critical bug - Tyrion server is not just set for this type of device! Set main C_Program and version!", this.id  );
                    logger.error("check_firmware:: Device id: {} Default main code version is not set for Type Of Board {} please set that!", this.id , getHardwareType().name);
                }
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    private void synchronizeBackup(WS_Message_Hardware_overview_Board overview) {
        try {
            // Pokud uživatel nechce DB synchronizaci ingoruji
            if (!this.database_synchronize) {
                logger.trace("check_backup:: Device id: {} - database_synchronize is forbidden - change parameters not allowed!", this.id );
                return;
            }

            // když je autobackup tak sere pes - změna autobacku je rovnou z devicu
            if (backup_mode) {
                logger.trace("check_backup:: Device id: {}- autobackup is true change parameters not allowed!", this.id );

                // Ale mohl bych udělat záznam o tom co tam je - kdyby to nebylo stejné s tím co si myslí tyrion že tam je:

                if (overview.binaries.backup != null && overview.binaries.backup.build_id != null && !overview.binaries.backup.build_id.equals("")) {

                    if (this.actual_backup_c_program_version != null && this.actual_backup_c_program_version.compilation.firmware_build_id.equals(overview.binaries.backup.build_id)) {
                        logger.debug("check_backup:: Device id: {} - verze se shodují");
                    } else {

                        Model_CProgramVersion version = Model_CProgramVersion.find.query().nullable().where().eq("compilation.firmware_build_id", overview.binaries.backup.build_id).findOne();
                        if (version != null) {

                            logger.debug("check_backup:: Device id: {} Ještě nebyla přiřazena žádná Backup verze k HW v Tyrionovi - ale program se podařilo najít", this.id);
                            logger.debug("check_backup:: Device id: {} Actual Version ID of backup: {} build_id: {} ", this.id, this.actual_backup_c_program_version != null ? this.actual_backup_c_program_version.id : "'neni uloženo'", this.actual_backup_c_program_version != null ? this.actual_backup_c_program_version.compilation.firmware_build_id : " není uloženo ");
                            logger.debug("check_backup:: Device id: {} Actual Version ID of backup: {} build_id: {} ", this.id, version.id, version.compilation.firmware_build_id);


                            if (this.actual_backup_c_program_version != null && version.id.equals(this.actual_backup_c_program_version.id)) {
                                logger.debug("check_backup:: Version is same!");
                            }

                            this.actual_backup_c_program_version = version;
                            this.idCache().add(Model_Hardware.Model_CProgramVersionFakeBackup.class, version.get_c_program().id);
                            this.update();
                        }
                    }
                }

                return;
            }

            logger.debug("check_backup:: third check backup! - Backup is static!! ");

            // Backup je takový jaký očekává tyrion
            if (get_backup_c_program_version() != null && get_backup_c_program_version().compilation.firmware_build_id.equals(overview.binaries.backup.build_id)) {
                logger.debug("check_backup:: Backup is same as on hardware as on Tyrion");
                return;
            }

            // Backup není - to by se stát nemělo - ale šup sem sním
            if (get_backup_c_program_version() == null) {
                logger.warn("check_backup:: Static Backup is required - but tyrion not set any backup!");

                if (overview.binaries.backup != null && (overview.binaries.backup.build_id == null || !overview.binaries.backup.build_id.equals(""))) {

                    try {
                        Model_CProgramVersion version = Model_CProgramVersion.find.query().where().eq("compilation.firmware_build_id", overview.binaries.backup.build_id).findOne();

                        logger.debug("check_backup:: Ještě nebyla přiřazena žádná Backup verze k HW v Tyrionovi - ale program se podařilo najít");

                        this.actual_backup_c_program_version = version;
                        this.idCache().add(Model_Hardware.Model_CProgramFakeBackup.class, version.get_c_program().id);
                        this.idCache().add(Model_Hardware.Model_CProgramVersionFakeBackup.class, version.id);
                        this.update();
                    } catch (NotFoundException e) {
                        logger.warn("check_backup:: Nastal stav, kdy mám statický backup, Tyrion v databázi nic nemá a ani se mi nepodařilo najít program (build_id)"
                                + "který by byl kompatibilní. Což je trochu problém. Uvidíme co nabízí update procedury. \n" +
                                "Možnosti jsou 1.) Nahrát první určený plan na hardware a tato metoda začne v podmínce najdi fungovat \n" +
                                "nebo 2) přepnu do autobacku");
                    }
                }
            }

            List<Model_HardwareUpdate> firmware_plans = Model_HardwareUpdate.find.query().where().eq("hardware.id", this.id)
                    .disjunction()
                    .add(Expr.eq("state", HardwareUpdateState.NOT_YET_STARTED))
                    .add(Expr.eq("state", HardwareUpdateState.IN_PROGRESS))
                    .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_IS_OFFLINE))
                    .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_NEVER_CONNECTED))
                    .endJunction()
                    .eq("firmware_type", FirmwareType.BACKUP.name())
                    .lt("actualization_procedure.date_of_planing", new Date())
                    .order().desc("actualization_procedure.date_of_planing")
                    .findList();

            // Zaloha kdyby byly stále platné aktualizace na backup
            for (int i = 1; i < firmware_plans.size(); i++) {
                firmware_plans.get(i).state = HardwareUpdateState.OBSOLETE;
                firmware_plans.get(i).update();
            }

            // Kontrola Firmwaru a přepsání starých
            // Je žádoucí přepsat všechny předhozí update plány - ale je nutné se podívat jestli nejsou rozdílné!
            // To jest pokud mám 2 updaty firmwaru pak ten starší zahodím
            // Ale jestli mám udpate firmwaru a backupu pak k tomu dojít nesmí!
            // Poměrně krkolomné řešení a HNUS kod - ale chyba je výjmečná a stává se jen sporadicky těsně před nebo po restartu serveru
            if (firmware_plans.size() > 1) {
                for (int i = 1; i < firmware_plans.size(); i++) {
                    firmware_plans.get(i).state = HardwareUpdateState.OBSOLETE;
                    firmware_plans.get(i).update();
                }
            }

            // Mám updaty a tak kontroluji
            if (!firmware_plans.isEmpty()) {

                Model_HardwareUpdate plan = firmware_plans.get(0);

                logger.debug("check_backup - Actual backup according to Tyrion: {}", get_backup_c_program_version().compilation.firmware_build_id);
                logger.debug("check_backup - Actual backup according to Homer: {}", overview.binaries.backup.build_id);
                logger.debug("check_backup - incomplete procedure expects: {}", plan.c_program_version_for_update.compilation.firmware_build_id);

                if (plan.getHardware().get_backup_c_program_version() != null) {

                    // Verze se rovnají
                    if (get_backup_c_program_version().compilation.firmware_build_id.equals(plan.c_program_version_for_update.compilation.firmware_build_id)) {

                        logger.debug("check_backup - up to date, procedure is done");
                        plan.state = HardwareUpdateState.COMPLETE;
                        plan.date_of_finish = new Date();
                        plan.update();

                    } else {

                        logger.debug("check_backup - need update, system starts a new update, number of tries {}", plan.count_of_tries);
                        plan.state = HardwareUpdateState.NOT_YET_STARTED;
                        plan.count_of_tries++;
                        plan.update();
                        execute_update_procedure(plan.getActualizationProcedure());
                    }

                } else {

                    logger.debug("check_backup - no backup, system starts a new update");
                    plan.state = HardwareUpdateState.NOT_YET_STARTED;
                    plan.count_of_tries++;
                    plan.update();

                    execute_update_procedure(plan.getActualizationProcedure());
                }
                return;
            }

            // Nemám updaty ale verze se neshodují
            if (get_backup_c_program_version() == null && firmware_plans.isEmpty()) {
                // TODO set_auto_backup();
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    private void synchronizeBootloader(WS_Message_Hardware_overview_Board overview) {

        // Nastavím bootloader který na hardwaru je

        if (get_actual_bootloader() == null || overview.binaries.bootloader.build_id.equals(this.actual_bootloader().version_identifier)) {

            actual_boot_loader = Model_BootLoader.find.query().where().eq("hardware_type.id", this.hardware_type().id).eq("version_identifier", overview.binaries.bootloader.build_id).findOne();
            update();

            logger.trace("check_bootloader -: Actual bootloader_id by DB not recognized and its updated :: ", overview.binaries.bootloader.build_id);
        }


        // Pokud uživatel nechce DB synchronizaci ingoruji
        if (!this.database_synchronize) {
            logger.trace("check_bootloader - database_synchronize is forbidden - change parameters not allowed!");
            return;
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

            if (plan.getHardware().get_actual_bootloader() != null) {

                // Verze se rovnají
                if (plan.getHardware().get_actual_bootloader().version_identifier.equals(plan.getBootloader().version_identifier)) {

                    logger.debug("check_bootloader - up to date, procedure is done");
                    plan.state = HardwareUpdateState.COMPLETE;
                    plan.date_of_finish = new Date();
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
        }
    }
}
