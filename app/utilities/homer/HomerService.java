package utilities.homer;

import com.google.inject.Inject;
import exceptions.ServerOfflineException;
import models.Model_HomerServer;
import play.libs.concurrent.HttpExecutionContext;
import websocket.WebSocketService;
import websocket.interfaces.Homer;

public class HomerService {

    private final WebSocketService webSocketService;
    private final HttpExecutionContext httpExecutionContext;

    @Inject
    public HomerService(WebSocketService webSocketService, HttpExecutionContext httpExecutionContext) {
        this.webSocketService = webSocketService;
        this.httpExecutionContext = httpExecutionContext;
    }

    public HomerInterface getInterface(Model_HomerServer server) {
         Homer homer = this.webSocketService.getInterface(server.id);
         if (homer != null) {
             return new HomerInterface(server, homer, this.httpExecutionContext);
         } else {
             throw new ServerOfflineException();
         }
    }
}
