package websocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import utilities.logger.Logger;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class WS_Message {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(WS_Message.class);

/* PUBLIC API ----------------------------------------------------------------------------------------------------------*/

    private UUID id;

    public WS_Message(ObjectNode message, int delay, int timeout, int retries) {
        if (!message.has("message_id")) {
            UUID id = UUID.randomUUID();
            this.id = id;
            message.put("message_id", id.toString());
        } else {
            this.id = UUID.fromString(message.get("message_id").asText());
        }
        this.confirmationThread = new WS_ConfirmationThread(message, delay, timeout, retries);
    }

    public ObjectNode send() {
        try {
            future = CompletableFuture.supplyAsync(this.confirmationThread);
            return future.get();
        } catch (Exception e) {
            return null;
        }
    }

    public void sendAsync(Consumer<ObjectNode> consumer) {
        try {
            future = CompletableFuture.supplyAsync(this.confirmationThread);
            future.thenAcceptAsync(consumer);
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void resolve(ObjectNode message) {
        future.complete(message);
    }

    public void setSender(WS_Interface sender) {
        this.confirmationThread.setSender(sender);
    }

    public UUID getId() {
        return this.id;
    }

/* PRIVATE API ---------------------------------------------------------------------------------------------------------*/

    private WS_ConfirmationThread confirmationThread;
    private CompletableFuture<ObjectNode> future;
}
