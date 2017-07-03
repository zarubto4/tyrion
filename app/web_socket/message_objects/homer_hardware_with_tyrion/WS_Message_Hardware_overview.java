package web_socket.message_objects.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Board;
import models.Model_HomerInstance;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WS_Message_Hardware_overview extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore
    public static final String messageType = "device_overview_list";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


    @Valid
    public List<WS_Help_Hardware_board_overview> device_list = new ArrayList<>();


    @JsonIgnore
    public WS_Help_Hardware_board_overview get_device_from_list(String device_id) {

        for (WS_Help_Hardware_board_overview device : device_list) {
            if (device.device_id.equals(device_id)) return device;
        }

        return null;
    }

    /**
     * The map was created for large fields. To avoid having to search the list of objects, the list will be remapped
     * on hashmap. So you can call the device directly by ID.
     * <p>
     * Slower for a small number of elements - significantly faster for a large number of elements.
     */
    @JsonIgnore
    HashMap<String, WS_Help_Hardware_board_overview> map = new HashMap<>();
    public WS_Help_Hardware_board_overview get_status(String device_id) {

        if (map.isEmpty() && device_list.isEmpty()) {
            return null;
        } else if (map.isEmpty()) {
            for (WS_Help_Hardware_board_overview status : device_list) {
                map.put(status.device_id, status);
            }
        }

        return map.get(device_id);
    }

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(List<String> devices_ids) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_Board.CHANNEL);
        request.set("device_ids", Json.toJson(devices_ids));

        return request;
    }

/* HELP CLASS  -------------------------------------------------------------------------------------------------------*/

    public static class WS_Help_Hardware_board_overview {

        public String device_id;
        public String instance_id;
        public String firmware_build_id;            // Číslo Buildu
        public String backup_build_id;              // Číslo Buildu
        public String bootloader_build_id;          // Version name Bootloader
        public String interface_name;
        public String state;
        public boolean online_status;
        public boolean autobackup;

    }

}