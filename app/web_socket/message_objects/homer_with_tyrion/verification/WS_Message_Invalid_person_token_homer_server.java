package web_socket.message_objects.homer_with_tyrion.verification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import play.data.validation.Constraints;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

public class WS_Message_Invalid_person_token_homer_server extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore
    public static final String messageType = "removePersonLoginToken";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required  public String token;



/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request_unsuccess() {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type", messageType);
        request.put("message_channel", Model_HomerServer.CHANNEL);
        request.put("status", "error");
        request.put("message", "Token is not valid");
        request.put("message_id", message_id);
        return request;
    }

    @JsonIgnore
    public ObjectNode make_request_success() {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type", messageType);
        request.put("message_channel", Model_HomerServer.CHANNEL);
        request.put("status", "success");
        request.put("message", "Token is not valid");
        request.put("message_id", message_id);

        return request;
    }



}
