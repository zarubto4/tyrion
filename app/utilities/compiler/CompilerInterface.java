package utilities.compiler;

import models.Model_CompilationServer;
import websocket.Request;
import websocket.WebSocketInterface;
import websocket.interfaces.Compiler;
import websocket.messages.compilator_with_tyrion.WS_Message_Make_compilation;

public class CompilerInterface {

    private final Model_CompilationServer server;
    private final WebSocketInterface webSocketInterface;

    public CompilerInterface(Model_CompilationServer server, WebSocketInterface webSocketInterface) {
        this.server = server;
        this.webSocketInterface = webSocketInterface;
    }

    public WS_Message_Make_compilation compile(Request request) throws Exception {
        return ((Compiler) this.webSocketInterface).compile(request);
    }
}
