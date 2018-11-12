package websocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers._BaseFormFactory;

import java.util.UUID;

/**
 * Universal representation of a WebSocket message.
 */
public class Message {

    private final _BaseFormFactory formFactory;

    private final ObjectNode message;

    public Message(ObjectNode message, _BaseFormFactory formFactory) {
        this.message = message;
        this.formFactory = formFactory;
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

    public String getErrorMessage() {
        if (this.message.has("error_message")) {
            return this.message.get("error_message").asText();
        }
        return null;
    }

    public ObjectNode getMessage() {
        return this.message;
    }

    public boolean isSuccessful() {
        return this.getStatus() != null && this.getStatus().equalsIgnoreCase("success");
    }
    public boolean isErroneous() {
        return this.getStatus() != null && this.getStatus().equalsIgnoreCase("error");
    }

    /**
     * Binds the json message to the given class.
     * @param cls to bind to
     * @param <T> type
     * @return bound object
     */
    public <T> T as(Class<T> cls) {
        return this.formFactory.formFromJsonWithValidation(cls, this.message);
    }
}
