package utilities.compiler;

import models.Model_CompilationServer;
import websocket.WebSocketInterface;

public class CompilerInterface {

    private final Model_CompilationServer server;
    private final WebSocketInterface webSocketInterface;

    public CompilerInterface(Model_CompilationServer server, WebSocketInterface webSocketInterface) {
        this.server = server;
        this.webSocketInterface = webSocketInterface;
    }
}
