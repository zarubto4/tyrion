package web_socket.message_objects.homer_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import play.data.validation.Constraints;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

public class WS_Message_Homer_Instancie_number extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String message_type = "homer_instance_size";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required public int value;

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request() {

        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_HomerServer.CHANNEL);

        return request;
    }

}
