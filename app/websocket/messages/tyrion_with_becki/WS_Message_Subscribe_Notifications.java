package websocket.messages.tyrion_with_becki;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;
import websocket.interfaces.WS_Portal;
import websocket.messages.common.abstract_class.WS_AbstractMessage;

public class WS_Message_Subscribe_Notifications extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore
    public static final String messageType = "subscribe_notification";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required  public String single_connection_token;

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static ObjectNode approve_result() {

        ObjectNode request = Json.newObject();
        request.put("message_type", messageType);
        request.put("message_channel", WS_Portal.message_channel);
        request.put("status", "success");
        return request;

    }

}
