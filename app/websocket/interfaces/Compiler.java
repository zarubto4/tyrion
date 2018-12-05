package websocket.interfaces;

import akka.stream.Materializer;
import com.google.inject.Inject;
import controllers._BaseFormFactory;
import exceptions.FailedMessageException;
import play.libs.Json;
import utilities.logger.Logger;
import utilities.network.NetworkStatusService;
import websocket.Interface;
import websocket.Message;
import websocket.Request;
import websocket.messages.compilator_with_tyrion.WS_Message_Make_compilation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class Compiler extends Interface {

    private static final Logger logger = new Logger(Compiler.class);

    private Map<UUID, CompletableFuture<WS_Message_Make_compilation>> runningCompilations = new HashMap<>();

    @Inject
    public Compiler(NetworkStatusService networkStatusService, Materializer materializer, _BaseFormFactory formFactory) {
        super(materializer, formFactory);
    }

    public WS_Message_Make_compilation compile(Request request) throws TimeoutException, InterruptedException, ExecutionException {
        Message message = this.sendWithResponse(request);
        if (message.isErroneous()) {
            throw new FailedMessageException(message);
        }

        WS_Message_Make_compilation result = message.as(WS_Message_Make_compilation.class);
        CompletableFuture<WS_Message_Make_compilation> future = new CompletableFuture<>();

        this.runningCompilations.put(result.build_id, future);

        WS_Message_Make_compilation completed = future.get(2L, TimeUnit.MINUTES);
        completed.interface_code = result.interface_code; // Interface data comes only in the first response
        return completed;
    }

    @Override
    public void onMessage(Message message) {
        switch (message.getType()) {
            case WS_Message_Make_compilation.message_type: this.onBuildComplete(message.as(WS_Message_Make_compilation.class));
        }
    }

    @Override
    public Long ping() {
        long start = System.currentTimeMillis();
        try {
            Message response = this.sendWithResponse(
                    new Request(Json.newObject()
                            .put("message_id", UUID.randomUUID().toString())
                            .put("message_channel", "compilation_server")
                            .put("message_type", "ping")
                    )
            );
        } catch (FailedMessageException e) {
            logger.warn("ping - got error response for ping, but still the server responded");
        }

        return System.currentTimeMillis() - start;
    }

    private void onBuildComplete(WS_Message_Make_compilation message) {
        if (this.runningCompilations.containsKey(message.build_id)) {
            this.runningCompilations.remove(message.build_id).complete(message);
        } else {
            logger.warn("onBuildComplete - no compilation with this build id: {} is running", message.build_id);
        }
    }
}
