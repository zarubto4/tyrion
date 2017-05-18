package web_socket.message_objects.tyrion_with_becki;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;
import web_socket.services.WS_Becki_Website;

public class WS_Message_UnSubscribe_Notifications extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore
    public static final String messageType = "unsubscribe_notification";

    public String single_connection_token;

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static ObjectNode approve_result() {

        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", WS_Becki_Website.CHANNEL);
        request.put("status", "success");
        return request;

    }

}
