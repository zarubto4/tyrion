package web_socket.message_objects.homerServer_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

public class WS_Message_Notice_homer_server_verification_required extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String messageType = "serverValidation";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/


    @JsonIgnore
    public ObjectNode make_request() {

        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerServer.CHANNEL);
        request.put("message", "Unique server identifier is not recognize!");

        return request;
    }

}
