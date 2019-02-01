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
import utilities.notifications.NotificationService;
import utilities.scheduler.JobDefinition;
import utilities.scheduler.SchedulerService;
import websocket.messages.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Progress;

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
     * @param firmwareType of the updatable
     */
    public void update(Model_Hardware hardware, Updatable updatable, FirmwareType firmwareType, UpdateType updateType, UUID trackingId) {
        Model_HardwareUpdate update = this.createUpdate(hardware, updatable, firmwareType, updateType, trackingId);
        try {
            HardwareInterface hardwareInterface = this.hardwareService.getInterface(hardware);
            hardwareInterface.update(update)
                    .whenComplete((message, exception) -> {
                        if (exception != null) {
                            if (exception instanceof FailedMessageException) {
                                update.error = ((FailedMessageException) exception).getFailedMessage().getErrorMessage();
                                update.error_code = ((FailedMessageException) exception).getFailedMessage().getErrorCode();
                            } else {
                                logger.internalServerError(exception);
                                update.error = exception.getMessage();
                            }

                            update.state = HardwareUpdateState.FAILED;
                            update.update();
                        }
                    });
        } catch (ServerOfflineException | NeverConnectedException e) {
            // nothing
            logger.warn("update - server is offline");
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void update(Model_Hardware hardware, Updatable updatable, FirmwareType firmwareType, UpdateType updateType) {
        this.update(hardware, updatable, firmwareType, updateType, null);
    }

    public void bulkUpdate(List<Model_Hardware> hardwareList, Updatable updatable, FirmwareType type, UpdateType updateType, UUID trackingId) {
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

                Model_HardwareUpdate update = this.createUpdate(hardware, updatable, type, updateType, trackingId);
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
                homerInterface.bulkUpdate(updateList)
                        .whenComplete((message, exception) -> {
                            if (exception != null) {
                                if (exception instanceof FailedMessageException) {
                                    updateList.forEach(update -> {
                                        update.state = HardwareUpdateState.FAILED;
                                        update.error = ((FailedMessageException)exception).getFailedMessage().getErrorMessage();
                                        update.error_code = ((FailedMessageException)exception).getFailedMessage().getErrorCode();
                                        update.update();
                                    });
                                } else {
                                    logger.internalServerError(exception);
                                    updateList.forEach(update -> {
                                        update.state = HardwareUpdateState.FAILED;
                                        update.error = exception.getMessage();
                                        update.update();
                                    });
                                }
                            }
                        });
            } catch (ServerOfflineException e) {
                // nothing - just continue
            }
        });
    }

    public void onUpdateMessage(WS_Message_Hardware_UpdateProcedure_Progress message) {
        try {

            Enum_HardwareHomerUpdate_state phase = message.get_phase();

            Model_HardwareUpdate update = Model_HardwareUpdate.find.byId(message.tracking_id);
            Model_Project project = update.getProject();

            logger.debug("onUpdateMessage - update id: {}, progress: {}%", update.id, message.percentage_progress);

            // Pokud se vrátí fáze špatně - ukončuji celý update
            if (message.error_message != null || message.error_code != null) {
                logger.warn("onUpdateMessage - update failed, Device ID: {}, update procedure: {}", update.getHardware().id, update.id);

                update.finished = new Date();
                update.state = HardwareUpdateState.FAILED;
                update.error_code = message.error_code;
                update.error = message.error + message.error_message;
                update.update();

                if (project != null) {
                    this.notificationService.send(project, update.notificationUpdateFailed(message.error_code));

                    // Critical Faild - Device was restored from Backup!
                    if (message.error.equals(Enum_HardwareHomerUpdate_state.ERROR_BACKUP_RESTORE.name())) {
                        this.notificationService.send(project, update.notificationRestoreFromBackup());
                    }
                }
                return;
            }

            logger.debug("onUpdateMessage - phase {}", phase);
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

                    logger.debug("onUpdateMessage - procedure {} is UPDATE_DONE", update.id);

                    Model_Hardware hardware = update.getHardware();

                    logger.warn("onUpdateMessage - update done, update.firmware_type {}", update.firmware_type);

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
                    update.finished = new Date();
                    update.update();

                    // TODO EchoHandler.addToQueue(new WSM_Echo(Model_Hardware.class, hardware.getProjectId(), hardware.id));

                    return;
                }

                case NEW_VERSION_DOESNT_MATCH: {

                    update.state = HardwareUpdateState.FAILED;
                    update.error_code = ErrorCode.NEW_VERSION_DOESNT_MATCH.error_code();
                    update.error = ErrorCode.NEW_VERSION_DOESNT_MATCH.error_message();
                    update.finished = new Date();
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
                        update.finished = new Date();
                        update.update();

                        Model_Hardware hardware = update.getHardware();

                        if (update.firmware_type == FirmwareType.FIRMWARE) {

                            if (hardware.getCurrentFirmware().id == null || !hardware.getActualCProgramVersionId().equals(update.c_program_version_for_update.id)) {
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
                            logger.debug("onUpdateMessage - nebylo třeba vůbec nic měnit.");
                        }

                        this.notificationService.modelUpdated(Model_Hardware.class, hardware.id, hardware.getProjectId());

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
        // TODO
    }

    public void schedule(Date planned, Model_Hardware hardware, Updatable updatable, FirmwareType firmwareType, UpdateType updateType, UUID trackingId) {
        Model_HardwareUpdate update = this.createUpdate(hardware, updatable, firmwareType, updateType, trackingId);
        update.planned = planned;
        update.update();

        this.schedulerService.schedule(new JobDefinition("hardware-update-" + update.getId(), UpdateJob.class).setDate(planned).setData(UpdateJob.UPDATE_ID, update.getId().toString()));
    }

    /**
     * This method will find all pending updates and marks them obsolete before it creates the new update.
     * @param hardware for update
     * @param updatable firmware or bootloader
     * @param firmwareType of the updatable object
     * @param updateType of the updatable object
     * @return Model_HardwareUpdate
     */
    private Model_HardwareUpdate createUpdate(Model_Hardware hardware, Updatable updatable, FirmwareType firmwareType, UpdateType updateType,  UUID trackingId) {

        if (trackingId == null) {
            trackingId = UUID.randomUUID();
        }

        ExpressionList<Model_HardwareUpdate> expressions = Model_HardwareUpdate.find.query().where()
                .eq("hardware.id", hardware.id)
                .or(Expr.eq("state", HardwareUpdateState.PENDING), Expr.eq("state", HardwareUpdateState.RUNNING));

        if (firmwareType.equals(FirmwareType.FIRMWARE)) {
            expressions.eq("firmware_type", FirmwareType.FIRMWARE);
        } else if (firmwareType.equals(FirmwareType.BACKUP)) {
            expressions.eq("firmware_type", FirmwareType.BACKUP);
        } else if (firmwareType.equals(FirmwareType.BOOTLOADER)) {
            expressions.eq("firmware_type", FirmwareType.BOOTLOADER);
        } else {
            throw new RuntimeException("Unknown component type: " + firmwareType + ", allowable values are: FIRMWARE, BACKUP or BOOTLOADER");
        }

        List<Model_HardwareUpdate> updates = expressions
                .order().desc("created")
                .findList();

        for (Model_HardwareUpdate update : updates) {
            logger.info("update - ({}) - some update is pending for this hardware, marking obsolete", hardware.full_id);
            update.state = HardwareUpdateState.OBSOLETE;
            update.update();
        }

        Model_HardwareUpdate update = new Model_HardwareUpdate();
        update.hardware = hardware;
        update.state = HardwareUpdateState.PENDING;
        update.firmware_type = firmwareType;
        update.type = updateType;
        update.count_of_tries = 0;
        update.tracking_id = trackingId;

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

    private Model_HardwareUpdate createUpdate(Model_Hardware hardware, Updatable updatable, FirmwareType type, UpdateType updateType) {
        return this.createUpdate(hardware, updatable, type, updateType, UUID.randomUUID());
    }
}
