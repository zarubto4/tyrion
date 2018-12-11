package websocket.messages.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import websocket.messages.common.abstract_class.WS_AbstractMessage;

import java.util.UUID;

public class WS_Message_Hardware_uuid_converter extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String message_type = "hardware_translate_id";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    public String full_id;
    public UUID uuid;

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    public ObjectNode get_result(UUID uuid, String full_id) {

        ObjectNode result = Json.newObject();
        result.put("message_type", message_type);
        result.put("message_channel", message_channel);
        result.put("message_id", message_id);
        result.put("uuid_id", uuid.toString());
        result.put("full_id", full_id);
        result.put("status" , "success");
        return result;

    }

    public ObjectNode get_result_error() {
        ObjectNode result = Json.newObject();
        result.put("message_type", message_type);
        result.put("message_channel", message_channel);
        result.put("message_id", message_id);
        result.put("full_id", full_id);
        result.put("status" , "error");
        result.put("error_message" , "Device not found or without dominance entity");
        return result;

    }


}
