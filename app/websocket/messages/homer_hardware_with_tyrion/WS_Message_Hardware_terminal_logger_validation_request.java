package websocket.messages.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;
import websocket.messages.common.abstract_class.WS_AbstractMessage;
import websocket.messages.common.abstract_class.WS_AbstractMessage_Hardware;

import java.util.List;

public class WS_Message_Hardware_terminal_logger_validation_request extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String message_type = "hardware_terminal_logger_verification";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required public List<String> full_ids;
    @Constraints.Required public String token;

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    public ObjectNode get_result(boolean token_approve) {

        ObjectNode result = Json.newObject();
        result.put("message_type", message_type);
        result.put("message_channel", message_channel);
        result.put("token_approve", token_approve);
        result.put("message_id", message_id);
        result.put("status" , "success");
        result.put("token" , token);
        return result;

    }

}
