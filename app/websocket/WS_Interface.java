package websocket;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import utilities.logger.Logger;
import websocket.interfaces.WS_Portal;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class WS_Interface extends AbstractActor {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(WS_Interface.class);

/* PUBLIC API ----------------------------------------------------------------------------------------------------------*/

    public static Props props(ActorRef out) {
        return Props.create(WS_Interface.class, out);
    }

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
        logger.trace("send:: {}", message.toString());
        this.out.tell(Json.toJson(message), self());
    }

    /**
     * Sends WebSocket message synchronously.
     * This operation is blocking until the response is received.
     * @param message {@link WS_Message} to send
     * @return response
     */
    public ObjectNode sendWithResponse(WS_Message message) {
        logger.trace("sendWithResponse:: Set Sender {} ", message.toString());
        message.setSender(this);
        logger.trace("sendWithResponse:: Message Buffer ID: {} ", message.getId());
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


    /**
     * Odesílání zpráv: Zprávy lze odesílat s vyžadovanou odpovědí, nebo bez ní. Pokud vyžaduji odpověď (jako potvrzení
     * že se akce povedla, nebo co se událo v reakci na zprávu), spustí se vlákno v metodě write_with_confirmation. Odeslaná
     * zpráva má unikátní číslo, které se uloží do zásobníku odeslaných odpovědí.
     * Vlákno se na chvíli uspí..  metoda onMessage, kam chodí odpovědi zjistí-li, že bylo uloženo do zásobníku odeslaných
     * zpráv nějaké ID, zprávu dále nepřeposílá a pouze danou zprávu uloží do zásobníku příchozích zpráv,
     * kde jí vlákno v intervalech hledá. Tam si jí vlákno taktéž vyzvedne. Pokud
     * nedojde k během určitého intervalu k odovědi, vláknu vyprší životnost a zavolá vyjímku TimeoutException.
     */
    public Map<UUID, WS_Message> messageBuffer = new HashMap<>();
    private final ActorRef out;

    private void onMessage(JsonNode message) {
        try {

            logger.trace("onMessage - incoming message: {}", message);
            ObjectNode json = (ObjectNode) message;

            logger.trace("onMessage - Object node: {}", json.toString());

            if(!json.has("websocket_identificator")){
                json.put("websocket_identificator", this.id.toString());
            }

            if (json.has("message_id")) {
                logger.trace("onMessage - message contains message ID: {}", json.get("message_id"));

                UUID id = UUID.fromString(json.get("message_id").asText());

                logger.trace("onMessage - message UUID: {}", id.toString());

                if (messageBuffer.containsKey(id)) {
                    logger.trace("onMessage - its message from buffer");
                    messageBuffer.get(id).resolve(json);
                }else {

                    logger.trace("onMessage - its not message from Buffer - set to onMessage in some WS_Interface");
                    this.onMessage(json);
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
