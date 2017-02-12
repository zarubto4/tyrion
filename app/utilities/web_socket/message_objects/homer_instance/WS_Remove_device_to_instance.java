package utilities.web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.project.b_program.instnace.Model_HomerInstance;
import play.libs.Json;
import utilities.web_socket.message_objects.common.WS_AbstractMessageInstance;

import java.util.List;

public class WS_Remove_device_to_instance extends WS_AbstractMessageInstance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "removeDeviceFromInstance";

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