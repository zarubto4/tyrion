package web_socket.message_objects.homer_instance_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import play.data.validation.Constraints;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

import java.util.ArrayList;
import java.util.List;

public class WS_Message_Instance_exist extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore
    public static final String message_type = "instanceExist";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


    @Constraints.Required public boolean exist;


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(String instance_id) {

        List<String> instance_ids = new ArrayList<>();
        instance_ids.add(instance_id);
        return make_request(instance_ids);

    }

    @JsonIgnore
    public ObjectNode make_request(List<String> instance_ids) {

        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_HomerServer.CHANNEL);
        request.set("instance_ids", Json.toJson(instance_ids));

        return request;
    }
}