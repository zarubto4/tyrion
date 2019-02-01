package websocket;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.UUID;

public class Request {

/* PUBLIC API ----------------------------------------------------------------------------------------------------------*/

    private UUID id;
    private String type;
    private ObjectNode message;

    public Request(ObjectNode message) {

        if (!message.has(Message.ID)) {
            this.id =  UUID.randomUUID();
            this.type = message.get(Message.TYPE).asText();
            message.put(Message.ID, id.toString());
        } else {
            this.id = UUID.fromString(message.get(Message.ID).asText());
        }

        this.message = message;
    }

    public UUID getId() {
        return this.id;
    }

    public String getType() {
        return this.type;
    }

    public ObjectNode getMessage() {
        return this.message;
    }

}
