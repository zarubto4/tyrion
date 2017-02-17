package utilities.web_socket.message_objects.homer_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.project.b_program.instnace.Model_HomerInstance;
import play.libs.Json;
import utilities.web_socket.message_objects.common.abstract_class.WS_AbstractMessageInstance;

public class WS_Is_device_connected  extends WS_AbstractMessageInstance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "checkDeviceConnection";


    @JsonIgnore
    public ObjectNode make_request(String device_id) {

        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerInstance.CHANNEL);
        request.put("deviceId", device_id);

        return request;
    }
}