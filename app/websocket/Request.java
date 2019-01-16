package websocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.FailedMessageException;
import exceptions.RequestTimeoutException;
import utilities.logger.Logger;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class Request {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Request.class);

/* PUBLIC API ----------------------------------------------------------------------------------------------------------*/

    private UUID id;
    private String message_type;

    public Request(ObjectNode message) {
        this(message, 0, 7500, 3);
    }

    public Request(ObjectNode message, int delay, int timeout, int retries) {

        if (!message.has(Message.ID)) {
            this.id =  UUID.randomUUID();
            this.message_type = message.get("message_type").asText();
            message.put(Message.ID, id.toString());
        } else {
            this.id = UUID.fromString(message.get(Message.ID).asText());
        }
        this.responseThread = new ResponseThread(message, delay, timeout, retries);
    }


    public Message send(Interface sender) throws RequestTimeoutException, FailedMessageException {
        try {
            this.responseThread.setSender(sender);
            future = CompletableFuture.supplyAsync(this.responseThread);
            return future.get();
        } catch (ExecutionException e) {
            if (e.getCause() != null) {
                if (e.getCause() instanceof FailedMessageException) {
                    throw (FailedMessageException) e.getCause();
                } else if (e.getCause() instanceof RequestTimeoutException) {
                    throw (RequestTimeoutException) e.getCause();
                }
            }
        } catch (InterruptedException e) {
            logger.internalServerError(e);
        }
        return null;
    }

    public CompletionStage<Message> sendAsync(Interface sender) {
        this.responseThread.setSender(sender);
        future = CompletableFuture.supplyAsync(this.responseThread);
        return future;
    }

    public void resolve(Message message) {

        responseThread.stop();

        if (message.isErroneous()) {
            future.completeExceptionally(new FailedMessageException(message));
        } else {
            future.complete(message);
        }
    }

    public UUID getId() {
        return this.id;
    }

    public String getMessageType() {
        return this.message_type;
    }
/* PRIVATE API ---------------------------------------------------------------------------------------------------------*/

    private ResponseThread responseThread;
    private CompletableFuture<Message> future;
}
