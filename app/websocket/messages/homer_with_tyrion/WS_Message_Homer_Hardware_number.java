package websocket.messages.homer_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import play.data.validation.Constraints;
import play.libs.Json;
import websocket.messages.common.abstract_class.WS_AbstractMessage;

import java.util.ArrayList;
import java.util.List;

public class WS_Message_Homer_Hardware_number extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String message_type = "homer_hardware_count";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required public int count;

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request() {

        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_HomerServer.CHANNEL);

        return request;
    }

}
