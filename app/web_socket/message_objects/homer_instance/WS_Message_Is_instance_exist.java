package web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

public class WS_Message_Is_instance_exist extends WS_AbstractMessage_Instance {

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