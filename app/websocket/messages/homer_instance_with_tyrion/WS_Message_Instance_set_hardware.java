package websocket.messages.homer_instance_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Instance;
import play.libs.Json;
import websocket.messages.common.abstract_class.WS_AbstractMessage_Instance;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Message_Homer_Hardware_ID_UUID_Pair;

import java.util.List;
import java.util.UUID;

public class WS_Message_Instance_set_hardware extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String message_type = "instance_set_hardware";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request( List<WS_Message_Homer_Hardware_ID_UUID_Pair> hardware_ids) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_Instance.CHANNEL);
        request.set("hardware", Json.toJson(hardware_ids));

        return request;
    }
}
