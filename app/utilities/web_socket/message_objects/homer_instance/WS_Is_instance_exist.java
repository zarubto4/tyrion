package utilities.web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import play.libs.Json;
import utilities.web_socket.message_objects.common.abstract_class.WS_AbstractMessageInstance;

public class WS_Is_instance_exist extends WS_AbstractMessageInstance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "instanceExist";

    public boolean exist;


    @JsonIgnore
    public ObjectNode make_request(String instance_name) {

        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerServer.CHANNEL);
        request.put("instanceId", instance_name);

        return request;
    }
}