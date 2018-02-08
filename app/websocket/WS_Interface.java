package websocket;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import utilities.logger.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class WS_Interface extends AbstractActor {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(WS_Interface.class);

/* PUBLIC API ----------------------------------------------------------------------------------------------------------*/

    public UUID id;

    public WS_Interface(ActorRef out) {
        this.out = out;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(JsonNode.class, this::onMessage).build();
    }

    /**
     * Sends message immediately, does not wait for response.
     * @param message to send
     */
    public void send(ObjectNode message) {
        if (!message.has("message_id")) {
            message.put("message_id", UUID.randomUUID().toString());
        }
        this.out.tell(message.toString(), self());
    }

    /**
     * Sends WebSocket message synchronously.
     * This operation is blocking until the response is received.
     * @param message {@link WS_Message} to send
     * @return response
     */
    public ObjectNode sendWithResponse(WS_Message message) {
        message.setSender(this);
        messageBuffer.put(message.getId(), message);
        return message.send();
    }

    /**
     * Sends WebSocket message asynchronously.
     * This operation is non-blocking, it executes the consumer callback
     * when the result is received.
     * @param message {@link WS_Message} to send
     * @param consumer asynchronous callback
     */
    public void sendWithResponseAsync(WS_Message message, Consumer<ObjectNode> consumer) {
        message.setSender(this);
        messageBuffer.put(message.getId(), message);
        message.sendAsync(consumer);
    }

    public void removeMessage(UUID id) {
        this.messageBuffer.remove(id);
    }

    @Override
    public void postStop() {
        this.onClose();
    }

    public void close() {
        onClose();
        self().tell(PoisonPill.getInstance(), self());
    }

/* PRIVATE API ---------------------------------------------------------------------------------------------------------*/

    private Map<UUID, WS_Message> messageBuffer = new HashMap<>();
    private final ActorRef out;

    private void onMessage(JsonNode message) {
        try {

            logger.debug("onMessage - incoming message: {}", message);

            ObjectNode json = (ObjectNode) message;

            if (json.has("message_id")) {
                UUID id = UUID.fromString(json.get("message_id").asText());
                if (messageBuffer.containsKey(id)) {
                    messageBuffer.get(id).resolve(json);
                }
            } else {
                this.onMessage(json);
            }

        } catch (Exception e) {
            logger.internalServerError(e);

            // TODO error response?
        }
    }

/* ABSTRACT API ----------------------------------------------------------------------------------------------------------*/

    public abstract boolean isOnline();
    public abstract void onMessage(ObjectNode json);
    public abstract void onClose();
}
