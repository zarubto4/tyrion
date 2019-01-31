package utilities.hardware;

import com.google.inject.Inject;
import com.google.inject.Injector;
import exceptions.NeverConnectedException;
import exceptions.ServerOfflineException;
import models.Model_Hardware;
import models.Model_HomerServer;
import play.libs.concurrent.HttpExecutionContext;
import utilities.logger.Logger;
import utilities.synchronization.SynchronizationService;
import websocket.WebSocketService;
import websocket.interfaces.Homer;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Model_Hardware_Temporary_NotDominant_record;

public class HardwareService {

    private static final Logger logger = new Logger(HardwareService.class);

    private final SynchronizationService synchronizationService;
    private final HttpExecutionContext httpExecutionContext;
    private final WebSocketService webSocketService;
    private final DominanceService dominanceService;
    private final Injector injector;

    @Inject
    public HardwareService(Injector injector, SynchronizationService synchronizationService, WebSocketService webSocketService,
                           DominanceService dominanceService, HttpExecutionContext httpExecutionContext) {
        this.synchronizationService = synchronizationService;
        this.httpExecutionContext = httpExecutionContext;
        this.webSocketService = webSocketService;
        this.dominanceService = dominanceService;
        this.injector = injector;
    }

    public HardwareInterface getInterface(Model_Hardware hardware) {
        Model_HomerServer server = hardware.get_connected_server();
        if (server != null) {
            Homer homer = this.webSocketService.getInterface(server.id);
            if (homer != null) {
                return new HardwareInterface(hardware, homer, this.httpExecutionContext);
            } else {
                throw new ServerOfflineException();
            }
        } else {
            throw new NeverConnectedException();
        }
    }

    public HardwareConfigurator getConfigurator(Model_Hardware hardware) {

        HardwareInterface hardwareInterface = null;

        try {
            hardwareInterface = this.getInterface(hardware);
        } catch (ServerOfflineException e) {
            // nothing - hardware is unreachable at this moment
        }

        return new HardwareConfigurator(hardware, hardwareInterface);
    }

    public void activate(Model_Hardware hardware) {
        WS_Model_Hardware_Temporary_NotDominant_record record = this.dominanceService.getNondominant(hardware.full_id);
        if (this.dominanceService.setDominant(hardware)) {
            if (record != null) {
                try {
                    this.getInterface(hardware).changeUUIDOnServer(record.random_temporary_hardware_id)
                            .whenComplete((message, exception) -> {
                                if (exception != null) {
                                    logger.internalServerError(exception);
                                }
                            });

                } catch (NeverConnectedException|ServerOfflineException e) {
                    // nothing
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

    /**
     * Call for set default firmware on Hardware (probably aprove from notification from users)
     * @param hardware
     */
    public void setDefaultFirmware(Model_Hardware hardware) {
        // TODO vykonat update defaultního updatu
    }

    /**
     * Reject request to set to default firmware
     */
    public void rejectDefaultFirmware(Model_Hardware hardware) {
        // TODO zaznamenat zamítnutí
    }

    public void deactivate(Model_Hardware hardware) {
        this.dominanceService.setNondominant(hardware);

        HardwareInterface hardwareInterface = this.getInterface(hardware);
        hardwareInterface.removeUUIDOnServer().whenComplete((message, exception) -> {
            if (exception != null) {
                logger.internalServerError(exception);
            } else {
                logger.info("deactivate - id removed from homer server");
            }
        });
        // TODO send echo
        // TODO remove from network status service
    }
}
