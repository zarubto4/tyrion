package web_socket.message_objects.common.service_class;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

public class WS_Message_Tyrion_restart_echo extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String messageType = "tyrionRestartEcho";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/



/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request() {

        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", "Tyrion");

        return request;
    }
}