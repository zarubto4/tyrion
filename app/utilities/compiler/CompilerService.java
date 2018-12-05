package utilities.compiler;

import com.google.inject.Inject;
import exceptions.ServerOfflineException;
import models.Model_CompilationServer;
import websocket.Request;
import websocket.WebSocketService;
import websocket.interfaces.Compiler;
import websocket.messages.compilator_with_tyrion.WS_Message_Make_compilation;

import java.util.List;
import java.util.Random;
import java.util.UUID;

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

    public WS_Message_Make_compilation compile(Request request) throws Exception {
        List<UUID> ids = this.webSocketService.getIdsOf(iface -> iface instanceof Compiler);
        if (ids.isEmpty()) {
            throw new ServerOfflineException();
        }

        UUID id = ids.get(new Random().nextInt(ids.size()));

        CompilerInterface compilerInterface = this.getInterface(Model_CompilationServer.find.byId(id));
        return compilerInterface.compile(request);
    }
}
