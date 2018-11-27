package utilities.compiler;

import com.google.inject.Inject;
import exceptions.ServerOfflineException;
import models.Model_CompilationServer;
import websocket.WebSocketService;
import websocket.interfaces.Compiler;

public class CompilerService {

    private final WebSocketService webSocketService;

    @Inject
    public CompilerService(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    public CompilerInterface getInterface(Model_CompilationServer server) {
        Compiler compiler = this.webSocketService.getInterface(server.id);
        if (compiler != null) {
            return new CompilerInterface(server, compiler);
        } else {
            throw new ServerOfflineException();
        }
    }
}
