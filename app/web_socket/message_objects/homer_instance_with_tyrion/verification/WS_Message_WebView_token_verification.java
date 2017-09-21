package web_socket.message_objects.homer_instance_with_tyrion.verification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

public class WS_Message_WebView_token_verification extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String messageType = "token_web_view_verification";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required public String token;



/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    public ObjectNode get_result(boolean token_approve){

        ObjectNode result = Json.newObject();
        result.put("message_type", messageType);
        result.put("message_channel", message_channel);
        result.put("token_approve", token_approve);
        result.put("message_id", message_id);
        result.put("instance_id", instance_id);
        result.put("status", "success");
        return result;

    }

}
