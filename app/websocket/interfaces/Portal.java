package websocket.interfaces;

import akka.NotUsed;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.NotSupportedException;
import models.Model_Garfield;
import utilities.logger.Logger;
import websocket.Interface;
import websocket.Message;
import websocket.Request;
import websocket.WebSocketInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class Portal extends Interface {

    private static final Logger logger = new Logger(Portal.class);

    private Materializer materializer;

    private Map<UUID, WebSocketInterface> children = new HashMap<>();

    public Portal(UUID id) {
        super(id);
    }

    @Override
    public Flow<JsonNode, JsonNode, NotUsed> materialize(Materializer materializer) {
        this.materializer = materializer;
        return null;
    }

    @Override
    public void onMessage(Message message) {
        switch (message.getChannel()) {
            case Model_Garfield.CHANNEL: { // TODO make interface for garfield

                // Není komu co zasílat - zahazuji - Je připojen jen tento kanál
                if (children.size() < 2) {
                    return;
                }

                for (UUID key : children.keySet()) {
                    if (key.equals(UUID.fromString(message.getMessage().get("single_connection_token").asText()))) continue;
                    children.get(key).send(message.getMessage());
                }

                break;
            }
            default: // TODO
        }
    }

    @Override
    public void send(ObjectNode message) {
        this.children.values().forEach(children -> children.send(message));
    }

    @Override
    public ObjectNode sendWithResponse(Request request) {
        throw new NotSupportedException("Messages with response are bot supported by this interface. (Portal)");
    }

    @Override
    public void sendWithResponseAsync(Request message, Consumer<ObjectNode> consumer) {
        throw new NotSupportedException("Messages with response are bot supported by this interface. (Portal)");
    }

    public Flow<JsonNode, JsonNode, NotUsed> register(WebSocketInterface iface) {

        this.children.put(iface.getId(), iface);

        iface.onClose(i -> {
            this.children.remove(i.getId());
            if (this.children.isEmpty() && this.onClose != null) {
                this.onClose.accept(this);
            }
        });

        return iface.materialize(this.materializer);
    }

    public boolean isRegistered(UUID id) {
        return this.children.containsKey(id);
    }

    @Override
    public void close() {
        new ArrayList<>(this.children.keySet()).forEach(key -> this.children.get(key).close());
    }
}
