package websocket.messages.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Hardware;
import play.libs.Json;
import websocket.messages.common.abstract_class.WS_AbstractMessage;

import javax.validation.Valid;
import java.util.*;

public class WS_Message_Hardware_overview extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore
    public static final String message_type = "hardware_info";

    public WS_Message_Hardware_overview() {}

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


    @Valid
    public List<WS_Message_Hardware_overview_Board> hardware_list = new ArrayList<>();


    /**
     * The map was created for large fields. To avoid having to search the list of objects, the list will be remapped
     * on hashmap. So you can call the device directly by ID.
     * <p>
     * Slower for a small number of elements - significantly faster for a large number of elements.
     */
    @JsonIgnore
    HashMap<UUID, WS_Message_Hardware_overview_Board> map = new HashMap<>();
    public WS_Message_Hardware_overview_Board get_device_from_list(UUID id) {

        // System.out.println("WS_Message_Hardware_overview get_device_from_list " +  hardware_id);

        if (map.isEmpty() && hardware_list.isEmpty()) {
             System.out.println("WS_Message_Hardware_overview: Seznam je prázdný :( ");
            return null;
        } else if (map.isEmpty()) {

            for (WS_Message_Hardware_overview_Board status : hardware_list) {
                status.status = super.status;
                status.message_channel = super.message_channel;
                status.message_id = super.message_id;
                status.message_type = super.message_type;
                status.websocket_identificator = websocket_identificator;
                map.put(status.uuid, status);
            }
        }

        if (map.containsKey(id)) {
            return map.get(id);
        } else {
            // System.out.println("WS_Message_Hardware_overview: Seznam neobsahuje dané ID :( " +  hardware_id);
            WS_Message_Hardware_overview_Board overview_board = new WS_Message_Hardware_overview_Board();
            overview_board.status = "error";
            overview_board.error_message = "Hardware is not in List";
            overview_board.error_code = 1231;
            return overview_board;
        }

    }

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(List<UUID> ids) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_Hardware.CHANNEL);
        request.set("uuid_ids", Json.toJson(ids));

        request.set("info_keys", Json.toJson(Arrays.asList(

                "target",                  // for example: Yoda_G3E
                "alias",                        // for example: WashingMAchine_31234
                "normal_mqtt_connection",       // for example: url:port
                "backup_mqtt_connection",       // for example: url:port
                "console",                      // Boolean - If device sending logs to Homer
                "ip",                           // Ip in local network
                "mac",                          // mac address of device
                "autobackup",                   // get backup
                "binaries",                     // Binaries info of firwmare, bootloader and backup
                "webport",                      // Port, where you can get webserver with device information
                "webview",                      // Boolean if device support webview
                "blreport",                     // Boolean if device support webview
                "wdenable",                     // Boolean if device support webview
                "timeoffset",                   // Boolean if device support webview
                "timesync",                     // Boolean if device support webview
                "lowpanbr",                     // Boolean if device support webview
                "autojump",                     // Boolean if device support webview
                "wdtime",                       // Boolean if device support webview
                "netsource"                     // Boolean if device support webview
        )));

        return request;

    }


}