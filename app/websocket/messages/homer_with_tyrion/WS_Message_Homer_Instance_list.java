package websocket.messages.homer_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import play.data.validation.Constraints;
import play.libs.Json;
import websocket.messages.common.abstract_class.WS_AbstractMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WS_Message_Homer_Instance_list extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String message_type = "homer_instance_list_ids";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    public List<UUID> instance_ids = new ArrayList<>();


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request() {

        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_HomerServer.CHANNEL);

        return request;
    }

}
