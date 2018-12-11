package websocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers._BaseFormFactory;

import java.util.UUID;

/**
 * Universal representation of a WebSocket message.
 */
public class Message {

    public static final String ID = "message_id";
    public static final String CHANNEL = "message_channel";
    public static final String TYPE = "message_type";
    public static final String STATUS = "status";
    public static final String ERROR_MESSAGE = "error_message";
    public static final String ERROR_CODE = "error_code";


    private final _BaseFormFactory formFactory;

    private final ObjectNode message;

    public Message(ObjectNode message, _BaseFormFactory formFactory) {
        this.message = message;
        this.formFactory = formFactory;
    }

    public UUID getId() {
        if (this.message.has(ID)) {
            return UUID.fromString(this.message.get(ID).asText());
        }
        return null;
    }

    public String getType() {
        if (this.message.has(TYPE)) {
            return this.message.get(TYPE).asText();
        }
        return null;
    }

    public String getChannel() {
        if (this.message.has(CHANNEL)) {
            return this.message.get(CHANNEL).asText();
        }
        return null;
    }

    public String getStatus() {
        if (this.message.has(STATUS)) {
            return this.message.get(STATUS).asText();
        }
        return null;
    }

    public String getErrorMessage() {
        if (this.message.has(ERROR_MESSAGE)) {
            return this.message.get(ERROR_MESSAGE).asText();
        }
        return null;
    }

    public Integer getErrorCode() {
        if (this.message.has(ERROR_CODE)) {
            return this.message.get(ERROR_CODE).asInt();
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
