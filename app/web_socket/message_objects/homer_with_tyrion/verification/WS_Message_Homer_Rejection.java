package web_socket.message_objects.homer_with_tyrion.verification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

public class WS_Message_Homer_Rejection extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String messageType = "homer_verification_first_required";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/


    @JsonIgnore
    public ObjectNode make_request() {

        ObjectNode request = Json.newObject();
        request.put("message_type", messageType);
        request.put("message_channel", Model_HomerServer.CHANNEL);
        request.put("message", "Yor server is not verified yet!");

        return request;
    }

}
