package utilities.hardware.update;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import exceptions.FailedMessageException;
import exceptions.NeverConnectedException;
import exceptions.NotFoundException;
import exceptions.ServerOfflineException;
import io.ebean.Expr;
import io.ebean.ExpressionList;
import models.*;
import play.libs.Json;
import utilities.enums.*;
import utilities.errors.ErrorCode;
import utilities.hardware.HardwareInterface;
import utilities.hardware.HardwareService;
import utilities.homer.HomerInterface;
import utilities.homer.HomerService;
import utilities.logger.Logger;
import utilities.models_update_echo.EchoHandler;
import utilities.notifications.NotificationService;
import utilities.scheduler.SchedulerService;
import websocket.messages.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Progress;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import java.util.*;

@Singleton
public class UpdateService {

    private static final Logger logger = new Logger(UpdateService.class);

    private final HomerService homerService;
    private final HardwareService hardwareService;
    private final SchedulerService schedulerService;
    private final NotificationService notificationService;

    private Map<UUID, UpdateTask> tasks = new HashMap<>();

    @Inject
    public UpdateService(HomerService homerService, HardwareService hardwareService, SchedulerService schedulerService, NotificationService notificationService) {
        this.homerService = homerService;
        this.hardwareService = hardwareService;
        this.schedulerService = schedulerService;
        this.notificationService = notificationService;
    }

    /**
     * Executes a single update.
     * @param hardware for update
     * @param updatable firmware or bootloader
     * @param type of the updatable
     */
    public void update(Model_Hardware hardware, Updatable updatable, FirmwareType type) {
        Model_HardwareUpdate update = this.createUpdate(hardware, updatable, type);
        try {
            HardwareInterface hardwareInterface = this.hardwareService.getInterface(hardware);
            hardwareInterface.update(update);
        } catch (ServerOfflineException | NeverConnectedException e) {
            // nothing
        } catch (FailedMessageException e) {
            update.state = HardwareUpdateState.FAILED;
            update.error = e.getFailedMessage().getErrorMessage();
            update.error_code = e.getFailedMessage().getErrorCode();
            update.update();
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void bulkUpdate(List<Model_Hardware> hardwareList, Updatable updatable, FirmwareType type) {
        Map<Model_HomerServer, List<Model_HardwareUpdate>> updates = new HashMap<>();

        for (Model_Hardware hardware : hardwareList) {
            try {
                Model_HomerServer server = hardware.get_connected_server();
                if (server == null) {
                    logger.warn("bulkUpdate - ({}) device never connected", hardware.full_id);
                    continue;
                }

                if (!updates.containsKey(server)) {
                    updates.put(server, new ArrayList<>());
                }

                Model_HardwareUpdate update = this.createUpdate(hardware, updatable, type);
                if (update.state == HardwareUpdateState.PENDING) {
                    updates.get(server).add(update);
                }

            } catch (NotFoundException e) {
                // nothing - just continue
            }
        }

        updates.forEach((server, updateList) -> {
            try {
                HomerInterface homerInterface = this.homerService.getInterface(server);
                homerInterface.bulkUpdate(updateList);
            } catch (ServerOfflineException e) {
                // nothing - just continue
            } catch (FailedMessageException e) {
                updateList.forEach(update -> {
                    update.state = HardwareUpdateState.FAILED;
                    update.error = e.getFailedMessage().getErrorMessage();
                    update.error_code = e.getFailedMessage().getErrorCode();
                    update.update();
                });
            }
        });
    }

    public void groupUpdate(Model_HardwareGroup group, Updatable updatable, FirmwareType type) {
        this.bulkUpdate(group.getHardware(), updatable, type);
    }

    // TODO refactor
    public void onUpdateMessage(WS_Message_Hardware_UpdateProcedure_Progress message) {
        try {

            Enum_HardwareHomerUpdate_state phase = message.get_phase();

            Model_HardwareUpdate update = Model_HardwareUpdate.find.byId(message.tracking_id);

            Model_Project project = update.getProject();

            logger.debug("onUpdateMessage - update id: {}, progress: {}%", update.id, message.percentage_progress);

            // Pokud se vrátí fáze špatně - ukončuji celý update
            if (message.error_message != null || message.error_code != null) {
                logger.warn("update_procedure_progress  Update Fail! Device ID: {}, update procedure: {}", update.getHardware().id, update.id);

                update.date_of_finish = new Date();
                update.state = HardwareUpdateState.FAILED;
                update.error_code = message.error_code;
                update.error = message.error + message.error_message;
                update.update();

                if (project != null) {
                    this.notificationService.send(project, update.notificationUpdateFailed(message.error_code));
                }
                return;
            }

            logger.debug("update_procedure_progress :: Checking phase: Phase {} ", phase);
            // Fáze jsou volány jen tehdá, když má homer instrukce je zasílat
            switch (phase) {
                case PHASE_UPLOAD_START: if (project != null) this.notificationService.send(project, update.notificationUpdateStart()); break;
                case PHASE_UPLOADING: if (project != null) this.notificationService.send(project, update.notificationUploading(message.percentage_progress)); break;
                case PHASE_UPLOAD_DONE: if (project != null) this.notificationService.send(project, update.notificationUploadDone()); break;
                case PHASE_FLASH_ERASING: if (project != null) this.notificationService.send(project, update.notificationBufferErasing()); break;
                case PHASE_FLASH_ERASED: if (project != null) this.notificationService.send(project, update.notificationBufferErased()); break;
                case PHASE_RESTARTING: if (project != null) this.notificationService.send(project, update.notificationRestarting()); break;
                case PHASE_CONNECTED_AFTER_RESTART: if (project != null) this.notificationService.send(project, update.notificationAfterRestart()); break;
                case PHASE_UPDATE_DONE: {

                    if (project != null) {
                        this.notificationService.send(project, update.notificationUpdateEnd());
                    }

                    logger.debug("update_procedure_progress - procedure {} is UPDATE_DONE", update.id);

                    Model_Hardware hardware = update.getHardware();

                    logger.warn("update_procedure_progress :: UPDATE DONE :: update.firmware_type {} ", update.firmware_type);

                    if (update.firmware_type == FirmwareType.FIRMWARE) {

                        hardware.actual_c_program_version = update.c_program_version_for_update;
                        hardware.update();

                    } else if (update.firmware_type == FirmwareType.BOOTLOADER) {

                        hardware.actual_boot_loader = update.getBootloader();
                        hardware.update();

                    } else if (update.firmware_type == FirmwareType.BACKUP) {

                        hardware.actual_backup_c_program_version = update.c_program_version_for_update;
                        hardware.update();
                        hardware.make_log_backup_arrise_change();
                    }

                    update.state = HardwareUpdateState.COMPLETE;
                    update.date_of_finish = new Date();
                    update.update();

                    EchoHandler.addToQueue(new WSM_Echo(Model_Hardware.class, hardware.get_project_id(), hardware.id));

                    return;
                }

                case NEW_VERSION_DOESNT_MATCH: {

                    update.state = HardwareUpdateState.FAILED;
                    update.error_code = ErrorCode.NEW_VERSION_DOESNT_MATCH.error_code();
                    update.error = ErrorCode.NEW_VERSION_DOESNT_MATCH.error_message();
                    update.date_of_finish = new Date();
                    update.update();

                    if (project != null)  {
                        this.notificationService.send(project, update.notificationUpdateFailed(message.error_code));
                    }

                    break;
                }

                case ALREADY_SAME: {
                    try {

                        if (project != null) {
                            this.notificationService.send(project, update.notificationAlreadySame());
                        }

                        update.state = HardwareUpdateState.COMPLETE;
                        update.date_of_finish = new Date();
                        update.update();

                        Model_Hardware hardware = update.getHardware();

                        if (update.firmware_type == FirmwareType.FIRMWARE) {

                            if (hardware.getCurrentFirmware().id == null || !hardware.get_actual_c_program_version_id().equals(update.c_program_version_for_update.id)) {
                                hardware.actual_c_program_version = update.c_program_version_for_update;
                                hardware.update();
                            }

                        } else if (update.firmware_type == FirmwareType.BOOTLOADER) {

                            hardware.actual_boot_loader = update.getBootloader();
                            hardware.idCache().removeAll(Model_Hardware.Model_hardware_update_update_in_progress_bootloader.class);
                            hardware.update();

                        } else if (update.firmware_type == FirmwareType.BACKUP) {

                            if (hardware.getCurrentBackup().id == null || !hardware.getCurrentBackup().id.equals(update.c_program_version_for_update.id)) {

                                hardware.actual_backup_c_program_version = update.c_program_version_for_update;
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
                    throw new UnsupportedOperationException("Unknown update phase. Report: " + Json.toJson(message));
                }
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void cancel(UUID id) {

    }

    public void schedule() {

    }

    /**
     * This method will find all pending updates and marks them obsolete before it creates the new update.
     * If some update is already in progress, the method will not create new update but it will return the running one.
     * @param hardware for update
     * @param updatable firmware or bootloader
     * @param type of the updatable object
     * @return Model_HardwareUpdate
     */
    private Model_HardwareUpdate createUpdate(Model_Hardware hardware, Updatable updatable, FirmwareType type) {
        ExpressionList<Model_HardwareUpdate> expressions = Model_HardwareUpdate.find.query().where()
                .eq("hardware.id", hardware.id)
                .or(Expr.eq("state", HardwareUpdateState.PENDING), Expr.eq("state", HardwareUpdateState.RUNNING))
                .lt("actualization_procedure.date_of_planing", new Date());

        if (type.equals(FirmwareType.FIRMWARE)) {
            expressions.eq("firmware_type", FirmwareType.FIRMWARE);
        } else if (type.equals(FirmwareType.BACKUP)) {
            expressions.eq("firmware_type", FirmwareType.BACKUP);
        } else if (type.equals(FirmwareType.BOOTLOADER)) {
            expressions.eq("firmware_type", FirmwareType.BOOTLOADER);
        } else {
            throw new RuntimeException("Unknown component type: " + type + ", allowable values are: FIRMWARE, BACKUP or BOOTLOADER");
        }

        List<Model_HardwareUpdate> updates = expressions
                .order().desc("actualization_procedure.date_of_planing")
                .findList();

        if (updates.size() > 0) {

            for (Model_HardwareUpdate update : updates) {
                if (update.state.equals(HardwareUpdateState.RUNNING)) {
                    logger.info("update - ({}) - some update is already being executed on the hardware", hardware.full_id);
                    if (update.getComponentId() == updatable.getId()) {
                        logger.info("update - ({}) - attempt to create same update again - skipping", hardware.full_id);
                        return update; // TODO is this a good idea?
                    }
                } else if (update.state.equals(HardwareUpdateState.PENDING)) {
                    logger.info("update - ({}) - some update is pending for this hardware, marking obsolete", hardware.full_id);
                    update.state = HardwareUpdateState.OBSOLETE;
                    update.update();
                }
            }
        }

        Model_HardwareUpdate update = new Model_HardwareUpdate();
        update.hardware = hardware;
        update.state = HardwareUpdateState.PENDING;
        update.firmware_type = type;
        update.count_of_tries = 0;

        if (updatable instanceof Model_CProgramVersion) {
            update.c_program_version_for_update = (Model_CProgramVersion) updatable;
        } else if (updatable instanceof Model_BootLoader) {
            update.bootloader = (Model_BootLoader) updatable;
        } else if (updatable instanceof Model_Compilation) {
            update.binary_file = ((Model_Compilation) updatable).getBlob();
        } else {
            throw new RuntimeException("updatable must be Model_CProgramVersion, Model_BootLoader or Model_Compilation");
        }

        update.save();

        return update;
    }
}
