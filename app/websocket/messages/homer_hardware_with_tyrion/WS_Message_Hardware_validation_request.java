package websocket.messages.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;
import utilities.logger.Logger;
import websocket.messages.common.abstract_class.WS_AbstractMessage_Hardware;

import java.util.UUID;

public class WS_Message_Hardware_validation_request {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(WS_AbstractMessage_Hardware.class);

/* VALUE --------------------------------------------------------------------------------------------------------------*/

    // MessageType
    @JsonIgnore public static final String message_type = "hardware_verification";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required public String user_name;
    @Constraints.Required public String password;
    @Constraints.Required public String full_id;

    @Constraints.Required public String message_id;
    @Constraints.Required public String message_channel;

    public String status = "error";
    public String error  = null;
    public Integer error_code = null;

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    public ObjectNode get_result(boolean token_approve, UUID uuid, String redirect_url, boolean dominant_entity) {

        ObjectNode result = Json.newObject();
        result.put("message_type", message_type);
        result.put("message_channel", message_channel);
        result.put("token_approve", token_approve);
        result.put("message_id", message_id);
        result.put("uuid_id", uuid != null ? uuid.toString() : null);
        result.put("status" , "success");
        result.put("password" , password);
        result.put("user_name" , user_name);
        result.put("redirect_url" , redirect_url);
        result.put("dominant_entity" , dominant_entity);
        return result;

    }
}
