package websocket.messages.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Hardware;
import play.libs.Json;
import utilities.enums.BoardCommand;
import websocket.messages.common.abstract_class.WS_AbstractMessage_Instance;

import java.util.List;
import java.util.UUID;

public class WS_Message_Hardware_command_execute extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String message_type = "hardware_command";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(List<UUID> device_ids, BoardCommand command, boolean priority) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_Hardware.CHANNEL);
        request.put("command", command.name());
        request.set("uuid_ids", Json.toJson(device_ids));
        request.put("priority", priority);

        return request;
    }



/* HELP CLASS  -------------------------------------------------------------------------------------------------------*/

}
