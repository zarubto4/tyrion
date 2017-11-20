package web_socket.message_objects.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Board;
import models.Model_BoardGroup;
import play.libs.Json;
import utilities.enums.Enum_type_of_command;
import utilities.swagger.documentationClass.Swagger_B_Program_Version_New;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WS_Message_Hardware_set_hardware_groups extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String message_type = "hardware_hardware_group_setting";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/


    @JsonIgnore
    public  ObjectNode make_request(List<Model_Board> devices, List<String> groups_id, Enum_type_of_command command_type) {

        List<String> hardware_ids        = devices.stream().map(Model_Board::get_id).collect(Collectors.toList());

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_Board.CHANNEL);
        request.set("hardware_ids", Json.toJson(hardware_ids) );
        request.set("hardware_group_ids", Json.toJson(groups_id));
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
