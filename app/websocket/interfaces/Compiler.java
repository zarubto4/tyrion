package websocket.interfaces;

import akka.stream.Materializer;
import com.google.inject.Inject;
import controllers._BaseFormFactory;
import models.Model_CompilationServer;
import play.libs.concurrent.HttpExecutionContext;
import utilities.compiler.CompilerService;
import utilities.enums.NetworkStatus;
import utilities.logger.Logger;
import utilities.network.NetworkStatusService;
import websocket.Interface;
import websocket.Message;
import websocket.Request;
import websocket.TimeOut;
import websocket.messages.compilator_with_tyrion.WS_Message_Make_compilation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class Compiler extends Interface {

    private static final Logger logger = new Logger(Compiler.class);

    public static final String CHANNEL = "compilation_server";

    private final NetworkStatusService networkStatusService;
    private final CompilerService compilerService;

    private Map<UUID, CompletableFuture<WS_Message_Make_compilation>> runningCompilations = new HashMap<>();

    @Inject
    public Compiler(NetworkStatusService networkStatusService, HttpExecutionContext httpExecutionContext,
                    Materializer materializer, _BaseFormFactory formFactory, CompilerService compilerService, TimeOut timeOut) {
        super(httpExecutionContext, materializer, formFactory, timeOut);
        this.networkStatusService = networkStatusService;
        this.compilerService = compilerService;
    }

    public CompletionStage<WS_Message_Make_compilation> compile(Request request) {
        return this.ask(request)
                .thenComposeAsync(message -> {
                    WS_Message_Make_compilation result = message.as(WS_Message_Make_compilation.class);
                    CompletableFuture<WS_Message_Make_compilation> future = new CompletableFuture<>();
                    this.runningCompilations.put(result.build_id, future);
                    return future.applyToEither(this.timeOut.after(2, TimeUnit.MINUTES), done -> {
                        done.interface_code = message.getMessage().get("interface_code").toString(); // Interface data comes only in the first response TODO: find out how to bind the interface_code via form
                        return done;
                    });
                }, this.httpExecutionContext.current());
    }

    @Override
    public void onMessage(Message message) {
        switch (message.getType()) {
            case "buildSuccess": this.onBuildComplete(message.as(WS_Message_Make_compilation.class)); break; // TODO remove after code server is compatible
            case WS_Message_Make_compilation.message_type: this.onBuildComplete(message.as(WS_Message_Make_compilation.class)); break;
        }
    }

    @Override
    public String getDefaultChannel() {
        return CHANNEL;
    }

    private void onBuildComplete(WS_Message_Make_compilation message) {
        if (this.runningCompilations.containsKey(message.build_id)) {
            this.runningCompilations.remove(message.build_id).complete(message);
        } else {
            logger.warn("onBuildComplete - no compilation with this build id: {} is running", message.build_id);
        }
    }

    @Override
    protected void onClose() {
        super.onClose();
        this.networkStatusService.setStatus(Model_CompilationServer.find.byId(this.id), NetworkStatus.OFFLINE);
        this.compilerService.checkAvailability();
    }
}
