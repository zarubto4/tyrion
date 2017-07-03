package web_socket.message_objects.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Board;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

import java.util.ArrayList;
import java.util.List;

public class WS_Message_Hardware_set_alias extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String messageType = "hardware_set_alias";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/


    @JsonIgnore
    public  ObjectNode make_request(Model_Board device) {

        List<Model_Board> devices = new ArrayList<>();
        devices.add(device);

        return make_request(devices);

    }

    @JsonIgnore
    public  ObjectNode make_request(List<Model_Board> devices) {

        List<Alias_Pair> device_pair = new ArrayList<>();
        for(Model_Board device : devices) {

            Alias_Pair pair = new Alias_Pair();
            pair.hardware_alias = device.name;
            pair.device_id = device.id;
            device_pair.add(pair);
        }

        return make_request_only_pair(device_pair);

    }

    @JsonIgnore
    private  ObjectNode make_request_only_pair(List<Alias_Pair> pairs) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_Board.CHANNEL);
        request.set("device_pairs", Json.toJson(pairs));

        return request;
    }


/* HELP CLASS  -------------------------------------------------------------------------------------------------------*/

    class Alias_Pair{
        public String device_id;
        public String hardware_alias;
    }

}
