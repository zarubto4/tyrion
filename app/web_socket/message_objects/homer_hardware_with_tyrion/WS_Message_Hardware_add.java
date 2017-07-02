package web_socket.message_objects.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Board;
import models.Model_HomerInstance;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

import java.util.List;

public class WS_Message_Hardware_add extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String messageType = "add_hardware";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(List<String> devicesId) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_Board.CHANNEL);
        request.set("device_ids", Json.toJson(devicesId) );

        return request;
    }
}
