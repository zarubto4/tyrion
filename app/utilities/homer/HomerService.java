package utilities.homer;

import com.google.inject.Inject;
import exceptions.ServerOfflineException;
import models.Model_HomerServer;
import websocket.WebSocketService;
import websocket.interfaces.Homer;

public class HomerService {

    private final WebSocketService webSocketService;

    @Inject
    public HomerService(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    public HomerInterface getInterface(Model_HomerServer server) {
         Homer homer = this.webSocketService.getInterface(server.id);
         if (homer != null) {
             return new HomerInterface(server, homer);
         } else {
             throw new ServerOfflineException();
         }
    }
}
