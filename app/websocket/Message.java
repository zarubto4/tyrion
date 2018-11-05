package websocket;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.UUID;

public class Message {

    private final ObjectNode message;

    public Message(ObjectNode message) {
        this.message = message;
    }

    public UUID getId() {
        if (this.message.has("message_id")) {
            return UUID.fromString(this.message.get("message_id").asText());
        }
        return null;
    }

    public String getType() {
        if (this.message.has("message_type")) {
            return this.message.get("message_type").asText();
        }
        return null;
    }

    public String getChannel() {
        if (this.message.has("message_channel")) {
            return this.message.get("message_channel").asText();
        }
        return null;
    }

    public String getStatus() {
        if (this.message.has("status")) {
            return this.message.get("status").asText();
        }
        return null;
    }

    public ObjectNode getMessage() {
        return this.message;
    }
}
