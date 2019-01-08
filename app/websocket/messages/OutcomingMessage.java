package websocket.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import websocket.Message;

import java.util.UUID;

public class OutcomingMessage {

    @JsonProperty(Message.ID)
    private UUID id;

    @JsonProperty(Message.TYPE)
    private String type;

    @JsonProperty(Message.CHANNEL)
    private String channel;

    public OutcomingMessage(String type) {
        this.type = type;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public UUID getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getChannel() {
        return channel;
    }
}
