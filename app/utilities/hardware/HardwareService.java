package utilities.hardware;

import com.google.inject.Inject;
import com.google.inject.Injector;
import exceptions.FailedMessageException;
import exceptions.NeverConnectedException;
import exceptions.ServerOfflineException;
import models.Model_Hardware;
import models.Model_HomerServer;
import utilities.logger.Logger;
import utilities.synchronization.SynchronizationService;
import websocket.WebSocketService;
import websocket.interfaces.Homer;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Model_Hardware_Temporary_NotDominant_record;

public class HardwareService {

    private static final Logger logger = new Logger(HardwareService.class);

    private final SynchronizationService synchronizationService;
    private final WebSocketService webSocketService;
    private final DominanceService dominanceService;
    private final Injector injector;

    @Inject
    public HardwareService(Injector injector, SynchronizationService synchronizationService, WebSocketService webSocketService, DominanceService dominanceService) {
        this.synchronizationService = synchronizationService;
        this.webSocketService = webSocketService;
        this.dominanceService = dominanceService;
        this.injector = injector;
    }

    public HardwareInterface getInterface(Model_Hardware hardware) {
        Model_HomerServer server = hardware.get_connected_server();
        if (server != null) {
            Homer homer = this.webSocketService.getInterface(server.id);
            if (homer != null) {
                return new HardwareInterface(hardware, homer);
            } else {
                throw new ServerOfflineException();
            }
        } else {
            throw new NeverConnectedException();
        }
    }

    public void activate(Model_Hardware hardware) {
        WS_Model_Hardware_Temporary_NotDominant_record record = this.dominanceService.getNondominant(hardware.full_id);
        if (this.dominanceService.setDominant(hardware)) {
            if (record != null) {
                try {
                    HardwareInterface hardwareInterface = this.getInterface(hardware);
                    hardwareInterface.changeUUIDOnServer(record.random_temporary_hardware_id);
                } catch (NeverConnectedException|ServerOfflineException e) {
                    // nothing
                } catch (FailedMessageException e) {
                    logger.warn("activate - server responded with error: {}", e.getFailedMessage().getErrorMessage());
                }
            }

            HardwareSynchronizationTask task = this.injector.getInstance(HardwareSynchronizationTask.class);
            task.setHardware(hardware);

            this.synchronizationService.submit(task);
            // TODO send echo

            hardware.make_log_activated(); // TODO injection
        } else {
            // TODO notification - cannot be activated
        }
    }

    public void deactivate(Model_Hardware hardware) {
        this.dominanceService.setNondominant(hardware);

        HardwareInterface hardwareInterface = this.getInterface(hardware);
        hardwareInterface.removeUUIDOnServer();
        // TODO send echo
        // TODO remove from network status service
    }
}
