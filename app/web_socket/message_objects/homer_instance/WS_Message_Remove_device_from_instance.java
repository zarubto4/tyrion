package web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerInstance;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

import java.util.List;

public class WS_Message_Remove_device_from_instance extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String messageType = "removeDeviceFromInstance";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(Model_HomerInstance instance, String yoda_id, List<String> devices_id) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerInstance.CHANNEL);
        request.put("instanceId", instance.blocko_instance_name);
        request.put("yodaId", yoda_id);
        request.set("devicesId", Json.toJson(devices_id));

        return request;
    }


}