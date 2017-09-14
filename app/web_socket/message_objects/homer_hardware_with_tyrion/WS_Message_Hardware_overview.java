package web_socket.message_objects.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Board;
import play.data.validation.Constraints;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class WS_Message_Hardware_overview extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore
    public static final String message_type = "hardware_info";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


    @Valid
    public List<WS_Help_Hardware_board_overview> hardware_list = new ArrayList<>();


    /**
     * The map was created for large fields. To avoid having to search the list of objects, the list will be remapped
     * on hashmap. So you can call the device directly by ID.
     * <p>
     * Slower for a small number of elements - significantly faster for a large number of elements.
     */
    @JsonIgnore
    HashMap<String, WS_Help_Hardware_board_overview> map = new HashMap<>();
    public WS_Help_Hardware_board_overview get_device_from_list(String device_id) {

        if (map.isEmpty() && hardware_list.isEmpty()) {
            return null;
        } else if (map.isEmpty()) {
            for (WS_Help_Hardware_board_overview status : hardware_list) {
                map.put(status.hardware_id, status);
            }
        }

        return map.get(device_id);
    }

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(List<String> devices_ids) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_Board.CHANNEL);
        request.set("hardware_ids", Json.toJson(devices_ids));

        request.set("info_keys", Json.toJson(Arrays.asList(

                "target",                       // for example: Yoda_G3E
                "alias",                        // for example: WashingMAchine_31234
                "normal_mqtt_connection",       // for example: url:port
                "backup_mqtt_connection",       // for example: url:port
                "console",                      // Boolean - If device sending logs to Homer
                "ip",                           // Ip in local network
                "mac",                          // mac adress of device
                "binaries",                     // Binaries info of firwmare, bootloader and backup
                "webport",                      // Port, where you can get webserver with device information
                "webview"                       // Boolean if device support webview

        )));

        return request;

    }

/* HELP CLASS  -------------------------------------------------------------------------------------------------------*/

    public static class WS_Help_Hardware_board_overview {

        @Constraints.Required public String hardware_id;
        @Constraints.Required public boolean online_state;
        public String mac;

        public String target;                       // třeba Yoda G3
        public String alias;                        // "pepa"

        @Valid public WS_Help_Hardware_board_binaries binaries;

        public String ip;
        public boolean console;
        public boolean webview;
        public Integer webport;

        public String normal_mqtt_connection;       // ip addressa:port
        public String backup_mqtt_connection;       // ip addressa:port

        public boolean autobackup;


    }

    public static class WS_Help_Hardware_board_binaries {

        public WS_Help_Hardware_board_binaries(){}

        @Constraints.Required @Valid public WS_Help_Hardware_board_IBinaryInfo firmware;
        @Constraints.Required @Valid public WS_Help_Hardware_board_IBinaryInfo bootloader;
        @Constraints.Required @Valid public WS_Help_Hardware_board_IBinaryInfo backup;
        @Constraints.Required @Valid public WS_Help_Hardware_board_IBinaryInfo buffer;

    }

    // Zastupný objekt pro binary
    public static class WS_Help_Hardware_board_IBinaryInfo {

        public String version;
        public Integer size;
        public Long timestamp;
        public String build_id;
        public String name;
        public Integer memsize;

    }


}