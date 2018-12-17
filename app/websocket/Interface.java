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
import exceptions.FailedMessageException;
import exceptions.InvalidBodyException;
import org.reactivestreams.Publisher;
import play.libs.Json;
import scala.concurrent.duration.FiniteDuration;
import utilities.logger.Logger;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class Interface implements WebSocketInterface {

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

    protected final Materializer materializer;
    protected final _BaseFormFactory formFactory;

    public Interface(Materializer materializer, _BaseFormFactory formFactory) {
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
        return Flow.fromSinkAndSourceCoupled(Sink.foreach(this::onReceived), Source.fromPublisher(both.second())) // TODO probably foreachParallel
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
                this.onMessage(message);
            }

        } catch (InvalidBodyException e) {
            // Response is, there is a change that it will response to something, that dont recognize message and also response to this server with error message...
            // Just ignore it
        } catch (Exception e) {
            logger.internalServerError(e);

            ObjectNode error = Json.newObject();

            if (msg.has(Message.ID)) {
                error.set(Message.ID, msg.get(Message.ID));
            } else {
                error.put(Message.ID, UUID.randomUUID().toString());
            }

            if (msg.has(Message.CHANNEL)) {
                error.set(Message.CHANNEL, msg.get(Message.CHANNEL));
            } else {
                error.put(Message.CHANNEL, this.getDefaultChannel());
            }

            if (msg.has(Message.TYPE)) {
                error.set(Message.TYPE, msg.get(Message.TYPE));
            } else {
                error.put(Message.TYPE, "unknown");
            }

            error.put(Message.STATUS, "error");
            error.put("error", "invalid message");
            error.put("error_log", e.getMessage());
            this.send(error);
        }
    }

    /**
     * Checks if message contains required fields and then wraps it in the Message object for easier manipulation.
     * @param message to parse
     * @return Message object
     */
    private Message parseMessage(JsonNode message) {
        if (message.has(Message.ID) && message.has(Message.TYPE) && message.has(Message.CHANNEL)) {
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
                .put(Message.ID, UUID.randomUUID().toString())
                .put(Message.TYPE, "keepalive")
                .put(Message.CHANNEL, this.getDefaultChannel());
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
        request.setSender(this);
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
        request.setSender(this);
        messageBuffer.put(request.getId(), request);
        request.sendAsync(consumer);
    }

    public void removeMessage(UUID id) {
        this.messageBuffer.remove(id);
    }

    @Override
    public void onMessage(Message message) {

    }

    @Override
    public boolean isOnline() {
        return !this.out.isTerminated();
    }

    public Long ping() {
        long start = System.currentTimeMillis();
        try {
            Message response = this.sendWithResponse(
                    new Request(Json.newObject()
                            .put(Message.ID, UUID.randomUUID().toString())
                            .put(Message.CHANNEL, this.getDefaultChannel())
                            .put(Message.TYPE, "ping")
                    )
            );
        } catch (FailedMessageException e) {
            logger.warn("ping - got error response for ping, but still the server responded");
        }
        return System.currentTimeMillis() - start;
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

    public abstract String getDefaultChannel();
}
