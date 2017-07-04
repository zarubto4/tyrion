package web_socket.message_objects.homer_with_tyrion.verification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import play.data.validation.Constraints;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

public class WS_Message_Check_homer_server_permission extends WS_AbstractMessage {

    @JsonIgnore
    public static final String messageType = "homer_get_verification_token";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


    @Constraints.Required public String hash_token;

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request() {

        ObjectNode request= Json.newObject();
        request.put("message_type", messageType);
        request.put("message_channel", Model_HomerServer.CHANNEL);

        return request;
    }

}
