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
import utilities.enums.FirmwareType;
import utilities.enums.HardwareUpdateState;
import utilities.hardware.HardwareInterface;
import utilities.hardware.HardwareService;
import utilities.homer.HomerInterface;
import utilities.homer.HomerService;
import utilities.logger.Logger;
import utilities.scheduler.SchedulerService;

import java.util.*;

@Singleton
public class UpdateService {

    private static final Logger logger = new Logger(UpdateService.class);

    private final HomerService homerService;
    private final HardwareService hardwareService;
    private final SchedulerService schedulerService;

    private Map<UUID, UpdateTask> tasks = new HashMap<>();

    @Inject
    public UpdateService(HomerService homerService, HardwareService hardwareService, SchedulerService schedulerService) {
        this.homerService = homerService;
        this.hardwareService = hardwareService;
        this.schedulerService = schedulerService;
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
        update.state = HardwareUpdateState.PENDING;
        update.firmware_type = type;
        update.count_of_tries = 0;

        if (updatable instanceof Model_CProgramVersion) {
            update.c_program_version_for_update = (Model_CProgramVersion) updatable;
        } else if (updatable instanceof Model_BootLoader) {
            update.bootloader = (Model_BootLoader) updatable;
        } else {
            throw new RuntimeException("updatable must be Model_CProgramVersion or Model_BootLoader");
        }

        update.save();

        return update;
    }
}
