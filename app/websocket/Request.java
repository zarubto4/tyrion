package websocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import utilities.logger.Logger;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class Request {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Request.class);

/* PUBLIC API ----------------------------------------------------------------------------------------------------------*/

    private UUID id;
    public String message_type; // Its Required public for special operation

    public Request(ObjectNode message, int delay, int timeout, int retries) {

        this.message_type = message.get("message_type").asText();

        if (!message.has("message_id")) {
            this.id =  UUID.randomUUID();
            message.put("message_id", id.toString());
        } else {
            this.id = UUID.fromString(message.get("message_id").asText());
        }
        this.confirmationThread = new ResponseThread(message, delay, timeout, retries, id);
    }


    public ObjectNode send() {
        try {
            logger.trace("send:: call ");
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
        logger.trace("resolve - {}", message.toString());
        future.complete(message);
        confirmationThread.stop();
    }

    public void setSender(WS_Interface sender) {
        logger.trace("setSender - sender ID: {} ", sender.id);
        this.confirmationThread.setSender(sender);
    }

    public UUID getId() {
        return this.id;
    }

/* PRIVATE API ---------------------------------------------------------------------------------------------------------*/

    private ResponseThread confirmationThread;
    private CompletableFuture<ObjectNode> future;
}
