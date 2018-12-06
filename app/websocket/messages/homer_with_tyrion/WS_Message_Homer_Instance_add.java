package websocket.messages.homer_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import websocket.interfaces.Homer;
import websocket.messages.common.abstract_class.WS_AbstractMessage_Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WS_Message_Homer_Instance_add extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore
    public static final String message_type = "homer_instances_create";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(UUID instance_id) {

        List<UUID> instance_ids = new ArrayList<>();
        instance_ids.add(instance_id);
        return make_request(instance_ids);
    }

    @JsonIgnore
    public ObjectNode make_request(List<UUID> instance_ids) {

        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Homer.CHANNEL);
        request.set("instance_ids", Json.toJson(instance_ids));

        return request;
    }
}