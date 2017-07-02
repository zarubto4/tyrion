package web_socket.message_objects.homer_instance_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerInstance;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

import java.util.List;

public class WS_Message_Instance_device_remove extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String messageType = "remove_device_from_instance";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request( List<String> devices_id) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerInstance.CHANNEL);
        request.set("devices_ids", Json.toJson(devices_id));

        return request;
    }


}