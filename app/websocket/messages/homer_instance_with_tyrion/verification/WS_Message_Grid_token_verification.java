package websocket.messages.homer_instance_with_tyrion.verification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;
import websocket.messages.common.abstract_class.WS_AbstractMessage_Instance;

public class WS_Message_Grid_token_verification extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String message_type = "token_terminal_verification";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required public String token;
    @Constraints.Required public String snapshot_id;


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    public ObjectNode get_result( boolean token_approve) {

        ObjectNode result = Json.newObject();
        result.put("message_type", message_type);
        result.put("message_channel", message_channel);
        result.put("token_approve", token_approve);
        result.put("message_id", message_id);
        result.put("instance_id", instance_id);
        result.put("status" , "success");
        return result;

    }

}
