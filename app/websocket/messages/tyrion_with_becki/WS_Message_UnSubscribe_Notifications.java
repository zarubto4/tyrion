package websocket.messages.tyrion_with_becki;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import websocket.interfaces.WS_Portal;
import websocket.messages.common.abstract_class.WS_AbstractMessage;

import java.util.UUID;

public class WS_Message_UnSubscribe_Notifications extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore
    public static final String message_type = "unsubscribe_notification";

    public UUID single_connection_token;

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static ObjectNode approve_result(String message_id) {

        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", WS_Portal.CHANNEL);
        request.put("status", "success");
        request.put("message_id", message_id);
        return request;

    }

}
