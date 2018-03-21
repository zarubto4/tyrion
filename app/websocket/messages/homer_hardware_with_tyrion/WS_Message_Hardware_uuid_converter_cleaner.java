package websocket.messages.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;
import websocket.messages.common.abstract_class.WS_AbstractMessage;

import java.util.UUID;

public class WS_Message_Hardware_uuid_converter_cleaner extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String message_type = "hardware_convert_id_clean";

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    public ObjectNode make_request(UUID new_uuid, UUID old_uuid, String full_id) {

        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", message_channel);
        request.put("old_uuid_id", old_uuid.toString());
        request.put("new_uuid_id", new_uuid != null ? new_uuid.toString() : null);
        request.put("full_id", full_id);
        return request;
    }

}
