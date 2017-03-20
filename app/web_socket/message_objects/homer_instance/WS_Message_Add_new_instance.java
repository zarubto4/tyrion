package web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerInstance;
import models.Model_HomerServer;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

public class WS_Message_Add_new_instance extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "createInstance";

    @JsonIgnore
    public ObjectNode make_request(Model_HomerInstance instance) {

        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerServer.CHANNEL);
        request.put("instanceId", instance.blocko_instance_name);

        return request;
    }
}