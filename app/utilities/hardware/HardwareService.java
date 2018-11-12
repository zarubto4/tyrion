package utilities.hardware;

import com.google.inject.Inject;
import exceptions.NeverConnectedException;
import exceptions.ServerOfflineException;
import models.Model_Hardware;
import models.Model_HomerServer;
import websocket.WebSocketService;
import websocket.interfaces.Homer;

public class HardwareService {

    private final WebSocketService webSocketService;
    private final DominanceService dominanceService;

    @Inject
    public HardwareService(WebSocketService webSocketService, DominanceService dominanceService) {
        this.webSocketService = webSocketService;
        this.dominanceService = dominanceService;
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

    public void activateHardware(Model_Hardware hardware) {
        this.dominanceService.setDominant(hardware);
        // TODO send echo
        // TODO remove from network status service
    }

    public void deactivateHardware(Model_Hardware hardware) {
        this.dominanceService.setNondominant(hardware);

        HardwareInterface hardwareInterface = this.getInterface(hardware);
        hardwareInterface.removeUUIDOnServer();
        // TODO send echo
    }
}
