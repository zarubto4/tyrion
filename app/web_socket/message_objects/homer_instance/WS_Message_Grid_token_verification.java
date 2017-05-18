package web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

public class WS_Message_Grid_token_verification extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String messageType = "token_grid_verification";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required public String token;


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    public ObjectNode get_result( boolean token_approve){

        ObjectNode result = Json.newObject();
        result.put("messageType", messageType);
        result.put("messageChannel", messageChannel);
        result.put("token_approve", token_approve);
        result.put("messageId", messageId);
        result.put("instanceId", instanceId);
        result.put("status" , "success");
        return result;

    }

}
