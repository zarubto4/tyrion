package utilities.hardware;

import com.google.inject.Inject;
import com.google.inject.Injector;
import models.Model_Hardware;
import utilities.enums.NetworkStatus;
import utilities.logger.Logger;
import utilities.network.NetworkStatusService;
import utilities.notifications.NotificationService;
import utilities.synchronization.SynchronizationService;

public class HardwareEvents {

    private static final Logger logger = new Logger(HardwareEvents.class);

    private final Injector injector;
    private final SynchronizationService synchronizationService;
    private final NetworkStatusService networkStatusService;
    private final NotificationService notificationService;

    @Inject
    public HardwareEvents(Injector injector, SynchronizationService synchronizationService, NetworkStatusService networkStatusService, NotificationService notificationService) {
        this.injector = injector;
        this.synchronizationService = synchronizationService;
        this.networkStatusService = networkStatusService;
        this.notificationService = notificationService;
    }

    public void connected(Model_Hardware hardware) {

        logger.info("connected - hardware: {} connected", hardware.full_id);

        this.networkStatusService.setStatus(hardware, NetworkStatus.ONLINE);

        HardwareSynchronizationTask task = this.injector.getInstance(HardwareSynchronizationTask.class);
        task.setHardware(hardware);

        this.synchronizationService.submit(task);

        if (hardware.getProject() != null) {
            this.notificationService.send(hardware.getProject(), hardware.notificationOnline());
        }
    }

    public void disconnected(Model_Hardware hardware) {

        logger.info("disconnected - hardware: {} disconnected", hardware.full_id);

        this.networkStatusService.setStatus(hardware, NetworkStatus.OFFLINE);

        if (hardware.getProject() != null) {
            this.notificationService.send(hardware.getProject(), hardware.notificationOffline());
        }
    }

    public void activated(Model_Hardware hardware) {

    }

    public void deactivated(Model_Hardware hardware) {

    }
}
