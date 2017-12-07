package web_socket.message_objects.homer_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

public class WS_Message_Homer_Token_validation_request extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore
    public static final String message_type = "hardware_verification";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required public String client_token;

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    public ObjectNode get_result(boolean token_approve){

        ObjectNode result = Json.newObject();
        result.put("message_type", message_type);
        result.put("message_channel", message_channel);
        result.put("token_approve", token_approve);
        result.put("message_id", message_id);
        result.put("token", client_token);
        result.put("status" , "success");
        return result;
    }

}
