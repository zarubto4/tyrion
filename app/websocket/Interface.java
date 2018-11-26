package websocket;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.japi.Pair;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers._BaseFormFactory;
import exceptions.InvalidBodyException;
import org.reactivestreams.Publisher;
import play.libs.Json;
import scala.concurrent.duration.FiniteDuration;
import utilities.logger.Logger;
import utilities.network.NetworkStatusService;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Interface implements WebSocketInterface {

    private static final Logger logger = new Logger(Interface.class);

    /**
     * Id of the interface.
     */
    protected UUID id;

    /**
     * Callback called when the socket was closed.
     */
    protected Consumer<Interface> onClose;

    /**
     * Reference to the owning WebSocket service.
     */
    protected WebSocketService webSocketService;

    /**
     * Reference for the actor representing the output of the interface.
     */
    private ActorRef out;

    /**
     * Buffer for messages which are waiting for the response.
     */
    private Map<UUID, Request> messageBuffer = new HashMap<>();

    protected final NetworkStatusService networkStatusService;
    protected final Materializer materializer;
    protected final _BaseFormFactory formFactory;

    public Interface(NetworkStatusService networkStatusService, Materializer materializer, _BaseFormFactory formFactory) {
        this.networkStatusService = networkStatusService;
        this.materializer = materializer;
        this.formFactory = formFactory;
    }

    public void setId(UUID id) {
        if (this.id == null) {
            this.id = id;
        } else {
            throw new RuntimeException("Cannot set id twice");
        }
    }

    public UUID getId() {
        return this.id;
    }

    public Flow<JsonNode, JsonNode, NotUsed> materialize(WebSocketService webSocketService) {

        this.webSocketService = webSocketService;

        Pair<ActorRef, Publisher<JsonNode>> both = Source.<JsonNode>actorRef(50, OverflowStrategy.dropNew()).toMat(Sink.asPublisher(AsPublisher.WITH_FANOUT), Keep.both()).run(this.materializer);

        Instant start = Instant.now();

        this.out = both.first();
        return Flow.fromSinkAndSourceCoupled(Sink.foreach(this::onReceived), Source.fromPublisher(both.second()))
                .keepAlive(new FiniteDuration(60L, TimeUnit.SECONDS), this::keepAlive)
                .watchTermination((notUsed, whenDone) -> {
                    whenDone.thenAccept(done -> {
                        logger.info("watchTermination$lambda - connection lasted for {} s", Instant.now().getEpochSecond() - start.getEpochSecond());
                        if (this.onClose != null) {
                            this.onClose.accept(this);
                        }
                        this.onClose();
                    });
                    return notUsed;
                });
    }

    private void onReceived(JsonNode msg) {
        try {

            logger.trace("onReceived - incoming message: {}", msg);

            Message message = this.parseMessage(msg);

            UUID messageId = message.getId();

            if (messageBuffer.containsKey(messageId)) {
                messageBuffer.get(messageId).resolve(message);
                messageBuffer.remove(messageId);
            } else {
                logger.trace("onReceived - message_id not found found in buffer");
                this.onMessage(message);
            }

        } catch (InvalidBodyException e) {
            // TODO send message back
        } catch (Exception e) {
            logger.internalServerError(e);

            ObjectNode json = Json.newObject();
            json.put("status", "error");
            json.put("error", "invalid message");
            json.put("error_log", e.getMessage());
            this.send(json);
        }
    }

    private Message parseMessage(JsonNode message) {
        if (message.has("message_id") && message.has("message_type") && message.has("message_channel")) {
            return new Message((ObjectNode) message, this.formFactory);
        } else {
            throw new RuntimeException("parseMessage - (" + this.getClass().getSimpleName() + ") - id: " + this.id + " received an invalid message: " + message.toString());
        }
    }

    /**
     * Creates a simple keep alive message.
     * @return keep alive message
     */
    private JsonNode keepAlive() {
        logger.trace("keepAlive - sending keepalive message");
        return Json.newObject()
                .put("message_id", UUID.randomUUID().toString())
                .put("message_type", "keepalive")
                .put("message_chanel", "TODO");
    }

    /**
     * Sends message immediately, does not wait for response.
     * @param message to send
     */
    @Override
    public void send(ObjectNode message) {
        logger.trace("send - sending: {} ", message.toString());
        this.out.tell(Json.toJson(message), this.out);
    }

    /**
     * Sends WebSocket request synchronously.
     * This operation is blocking until the response is received.
     * @param request {@link Request} to send
     * @return response
     */
    @Override
    public Message sendWithResponse(Request request) {
        logger.trace("sendWithResponse - sending request synchronously");
        // request.setSender(this);
        messageBuffer.put(request.getId(), request);
        return request.send();
    }

    /**
     * Sends WebSocket message asynchronously.
     * This operation is non-blocking, it executes the consumer callback
     * when the result is received.
     * @param request {@link Request} to send
     * @param consumer asynchronous callback
     */
    @Override
    public void sendWithResponseAsync(Request request, Consumer<Message> consumer) {
        logger.trace("sendWithResponseAsync - sending request asynchronously");
        // message.setSender(this);
        messageBuffer.put(request.getId(), request);
        request.sendAsync(consumer);
    }

    @Override
    public void onMessage(Message message) {

    }

    @Override
    public boolean isOnline() {
        return !this.out.isTerminated();
    }

    @Override
    public void close() {
        logger.trace("close - closing socket, id: {}", this.id);
        this.onClose();
        this.out.tell(PoisonPill.getInstance(), out);
    }

    protected void onClose() {
        logger.trace("onClosed - socket, id: {} was closed", this.id);
    }

    public void onClose(Consumer<Interface> onClose) {
        this.onClose = onClose;
    }
}
