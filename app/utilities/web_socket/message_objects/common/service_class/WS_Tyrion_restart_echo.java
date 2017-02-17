package utilities.web_socket.message_objects.common.service_class;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import utilities.web_socket.message_objects.common.abstract_class.WS_AbstractMessageInstance;

public class WS_Tyrion_restart_echo extends WS_AbstractMessageInstance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "tyrionRestartEcho";


    @JsonIgnore
    public ObjectNode make_request() {

        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", "Tyrion");

        return request;
    }
}