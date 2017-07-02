package web_socket.message_objects.homer_instance_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

import java.util.List;

public class WS_Message_Instance_destroy extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String messageType = "destroyInstance";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(List<String> instance_ids) {

        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerServer.CHANNEL);
        request.set("instance_ids", Json.toJson(instance_ids));

        return request;
    }
}