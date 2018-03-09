package websocket.messages.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Hardware;
import play.libs.Json;
import utilities.enums.Enum_type_of_command;
import websocket.messages.common.abstract_class.WS_AbstractMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class WS_Message_Hardware_set_hardware_groups extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String message_type = "hardware_hardware_group_setting";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/


    @JsonIgnore
    public  ObjectNode make_request(List<Model_Hardware> devices, List<UUID> group_ids, Enum_type_of_command command_type) {

        List<String> hardware_ids = devices.stream().map(Model_Hardware::get_full_id).collect(Collectors.toList());

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_Hardware.CHANNEL);
        request.set("full_ids", Json.toJson(hardware_ids) );
        request.put("hardware_group_id", Json.toJson(group_ids));
        request.put("command_type", command_type.name() );

        return request;
    }


/* HELP CLASS  -------------------------------------------------------------------------------------------------------*/

    /**
     * Každý objekt zastupuje nastavení konkrétní hodnoty na hardwaru,
     * rozdělujeme je, aby nikdy nedošlo k záměně a aktualizaci hodnot postupně - nikoliv masivní dávnou.
     */

    class Pair{
        @JsonProperty public List<String> hardware_ids = new ArrayList<>();
        @JsonProperty public List<String> hardware_group_ids = new ArrayList<>();
    }

}
