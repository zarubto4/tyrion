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

    public Request(ObjectNode message) {
        this(message, 0, 7500, 3);
    }

    public Request(ObjectNode message, int delay, int timeout, int retries) {

        if (!message.has(Message.ID)) {
            this.id =  UUID.randomUUID();
            message.put(Message.ID, id.toString());
        } else {
            this.id = UUID.fromString(message.get(Message.ID).asText());
        }
        this.confirmationThread = new ResponseThread(message, delay, timeout, retries);
    }


    public Message send() {
        try {
            future = CompletableFuture.supplyAsync(this.confirmationThread);
            return future.get();
        } catch (Exception e) {
            return null;
        }
    }

    public void sendAsync(Consumer<Message> consumer) {
        try {
            future = CompletableFuture.supplyAsync(this.confirmationThread);
            future.thenAcceptAsync(consumer);
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void resolve(Message message) {
        future.complete(message);
        confirmationThread.stop();
    }

    public void setSender(Interface sender) {
        this.confirmationThread.setSender(sender);
    }

    public UUID getId() {
        return this.id;
    }

/* PRIVATE API ---------------------------------------------------------------------------------------------------------*/

    private ResponseThread confirmationThread;
    private CompletableFuture<Message> future;
}
