package web_socket.message_objects.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Board;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

import java.util.ArrayList;
import java.util.List;

public class WS_Message_Hardware_set_settings extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String message_type = "hardware_settings";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public  ObjectNode make_request_alias(List<Model_Board> devices) {

        List<Pair> device_pair = new ArrayList<>();
        for(Model_Board device : devices) {

            Pair pair = new Pair();
            pair.hardware_id = device.id;

            Set_Alias_Pair settings = new Set_Alias_Pair();
            settings.alias = device.name;
            pair.settings = settings;

            device_pair.add(pair);
        }

        return make_request_only_pair(device_pair);
    }

    @JsonIgnore
    public  ObjectNode make_request_autobackup(List<Model_Board> devices) {

        List<Pair> device_pair = new ArrayList<>();
        for(Model_Board device : devices) {

            Pair pair = new Pair();
            pair.hardware_id = device.id;

            Set_AutoBackup_Pair settings = new Set_AutoBackup_Pair();
            pair.settings = settings;

            device_pair.add(pair);
        }

        return make_request_only_pair(device_pair);
    }


    @JsonIgnore
    public  ObjectNode make_request_synchronize_with_database(List<Model_Board> devices) {

        List<Pair> device_pair = new ArrayList<>();
        for(Model_Board device : devices) {

            Pair pair = new Pair();
            pair.hardware_id = device.id;

            Set_Database_synchronize_Pair settings = new Set_Database_synchronize_Pair();
            settings.database_synchronize = device.database_synchronize;
            pair.settings = settings;

            device_pair.add(pair);
        }

        return make_request_only_pair(device_pair);
    }

    @JsonIgnore
    public  ObjectNode make_request_synchronize_Web_port(List<Model_Board> devices) {

        List<Pair> device_pair = new ArrayList<>();
        for(Model_Board device : devices) {

            Pair pair = new Pair();
            pair.hardware_id = device.id;

            Set_Web_Port_Pair settings = new Set_Web_Port_Pair();
            settings.webport = device.web_port;
            pair.settings = settings;

            device_pair.add(pair);
        }

        return make_request_only_pair(device_pair);
    }

    @JsonIgnore
    public  ObjectNode make_request_synchronize_Web_view(List<Model_Board> devices) {

        List<Pair> device_pair = new ArrayList<>();
        for(Model_Board device : devices) {

            Pair pair = new Pair();
            pair.hardware_id = device.id;

            Set_Web_View_Pair settings = new Set_Web_View_Pair();
            settings.webview = device.web_view;
            pair.settings = settings;

            device_pair.add(pair);
        }

        return make_request_only_pair(device_pair);
    }



/* Create Final CLASS  -------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    private  ObjectNode make_request_only_pair(List<Pair> pairs) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_Board.CHANNEL);
        request.set("hardware_list", Json.toJson(pairs));

        return request;
    }


/* HELP CLASS  -------------------------------------------------------------------------------------------------------*/

    /**
     * Každý objekt zastupuje nastavení konkrétní hodnoty na hardwaru,
     * rozdělujeme je, aby nikdy nedošlo k záměně a aktualizaci hodnot postupně - nikoliv masivní dávnou.
     */

    
    class Pair{
        @JsonProperty public String hardware_id;
        @JsonProperty public Settings settings;
    }

    interface Settings{}

    class Set_Alias_Pair implements Settings{
       @JsonProperty public String alias;
    }

    class Set_AutoBackup_Pair implements Settings{
        @JsonProperty public boolean auto_backup = true;    // Autobackup is always set to true - only static firmware (backup) can override that
    }

    class Set_Database_synchronize_Pair implements Settings{
        @JsonProperty public boolean database_synchronize;
    }

    class Set_Web_Port_Pair implements Settings{
        @JsonProperty public Integer webport;
    }

    class Set_Web_View_Pair implements Settings{
        @JsonProperty public boolean webview;
    }



}
