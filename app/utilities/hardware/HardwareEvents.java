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

        hardware.make_log_connect(); // TODO injection

        this.networkStatusService.setStatus(hardware, NetworkStatus.ONLINE);

        HardwareSynchronizationTask task = this.injector.getInstance(HardwareSynchronizationTask.class);
        task.setHardware(hardware);

        this.synchronizationService.submit(task);

        // Notifikce
        if (hardware.developer_kit) {
            hardware.notification_board_connect(); // TODO injection this.notificationService.send();
        }
    }

    public void disconnected(Model_Hardware hardware) {

        // Záznam do DM databáze
        hardware.make_log_disconnect(); // TODO injection

        this.networkStatusService.setStatus(hardware, NetworkStatus.OFFLINE);

        // Standartní synchronizace
        if (hardware.project().id != null) {
            // TODO injection WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Hardware.class, hardware.id, false, hardware.project().id);
        }

        if (hardware.developer_kit) {
            // Notifikace
            hardware.notification_board_disconnect(); // TODO injection this.notificationService.send();
        }
    }

    public void activated(Model_Hardware hardware) {

    }

    public void deactivated(Model_Hardware hardware) {

    }
}
