package utilities.hardware.update;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import exceptions.NeverConnectedException;
import exceptions.ServerOfflineException;
import io.ebean.Expr;
import io.ebean.ExpressionList;
import models.Model_BootLoader;
import models.Model_CProgramVersion;
import models.Model_Hardware;
import models.Model_HardwareUpdate;
import utilities.enums.FirmwareType;
import utilities.enums.HardwareUpdateState;
import utilities.hardware.HardwareInterface;
import utilities.hardware.HardwareService;
import utilities.logger.Logger;
import utilities.scheduler.SchedulerService;

import java.util.*;

@Singleton
public class UpdateService {

    private static final Logger logger = new Logger(UpdateService.class);

    private final HardwareService hardwareService;
    private final SchedulerService schedulerService;

    private Map<UUID, UpdateTask> tasks = new HashMap<>();

    @Inject
    public UpdateService(HardwareService hardwareService, SchedulerService schedulerService) {
        this.hardwareService = hardwareService;
        this.schedulerService = schedulerService;
    }

    public void update(Model_Hardware hardware, Updatable updatable) {

        ExpressionList<Model_HardwareUpdate> expressions = Model_HardwareUpdate.find.query().where()
                .eq("hardware.id", hardware.id)
                .or(Expr.eq("state", HardwareUpdateState.PENDING), Expr.eq("state", HardwareUpdateState.RUNNING))
                .lt("actualization_procedure.date_of_planing", new Date());

        if (updatable.getComponentType().equals(FirmwareType.FIRMWARE)) {
            expressions.eq("firmware_type", FirmwareType.FIRMWARE);
        } else if (updatable.getComponentType().equals(FirmwareType.BACKUP)) {
            expressions.eq("firmware_type", FirmwareType.BACKUP);
        } else if (updatable.getComponentType().equals(FirmwareType.BOOTLOADER)) {
            expressions.eq("firmware_type", FirmwareType.BOOTLOADER);
        } else {
            throw new RuntimeException("Unknown component type: " + updatable.getComponentType() + ", allowable values are: FIRMWARE, BACKUP or BOOTLOADER");
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
                        return;
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
        update.firmware_type = updatable.getComponentType();
        update.count_of_tries = 0;

        if (updatable instanceof Model_CProgramVersion) {
            update.c_program_version_for_update = (Model_CProgramVersion) updatable;
        } else if (updatable instanceof Model_BootLoader) {
            update.bootloader = (Model_BootLoader) updatable;
        } else {
            throw new RuntimeException("updatable must be Model_CProgramVersion or Model_BootLoader");
        }

        update.save();

        try {
            HardwareInterface hardwareInterface = this.hardwareService.getInterface(hardware);
            hardwareInterface.update();

        } catch (ServerOfflineException | NeverConnectedException e) {
            // nothing
        } catch (Exception e) {
            logger.internalServerError(e);
        }

        UpdateTask task = new UpdateTask();
    }

    public void cancel(UUID id) {

    }

    public void schedule() {

    }
}
