package utilities.hardware.synchronization;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import models.*;
import utilities.hardware.HardwareService;
import utilities.hardware.update.UpdateService;

import java.util.*;

@Singleton
public class SynchronizationService {

    private final HardwareService hardwareService;
    private final UpdateService updateService;

    private final Map<UUID, SynchronizationTask> tasks = new HashMap<>();

    @Inject
    public SynchronizationService(HardwareService hardwareService, UpdateService updateService) {
        this.hardwareService = hardwareService;
        this.updateService = updateService;
    }

    public void synchronize(Model_Hardware hardware) {

        if (this.tasks.containsKey(hardware.getId())) {
            throw new RuntimeException("Synchronization is already in progress for this device.");
        }

        SynchronizationTask task = new SynchronizationTask(hardware, this.hardwareService.getInterface(hardware), this.updateService);

        this.tasks.put(hardware.getId(), task);

        task.start();
    }

    public void cancel(Model_Hardware hardware) {
        if (this.tasks.containsKey(hardware.getId())) {
            this.tasks.get(hardware.getId()).cancel();
        } else {
            throw new RuntimeException("No synchronization task for this device.");
        }
    }
}
