package utilities.web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerInstance;
import play.libs.Json;
import utilities.web_socket.message_objects.common.abstract_class.WS_AbstractMessageInstance;

import java.util.List;

public class WS_Add_device_to_instance extends WS_AbstractMessageInstance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "addDeviceToInstance";

    @JsonIgnore
    public ObjectNode make_request(Model_HomerInstance instance, String yodaId, List<String> devicesId) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerInstance.CHANNEL);
        request.put("instanceId", instance.blocko_instance_name);
        request.put("yodaId", yodaId);
        request.set("devicesId", Json.toJson(devicesId) );

        return request;
    }
}
