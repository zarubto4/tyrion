package websocket.messages.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Hardware;
import play.data.validation.Constraints;
import play.libs.Json;
import websocket.messages.common.abstract_class.WS_AbstractMessage;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class WS_Message_Hardware_online_status extends WS_AbstractMessage  {

    // MessageType
    @JsonIgnore public static final String message_type = "hardware_online_status";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Valid public List<DeviceStatus> hardware_list = new ArrayList<>();


    /**
     * The map was created for large fields. To avoid having to search the list of objects, the list will be remapped
     * on hashmap. So you can call the device directly by ID.
     *
     * Slower for a small number of elements - significantly faster for a large number of elements.
     */
    @JsonIgnore HashMap<UUID,DeviceStatus> map = new HashMap<>();
    public boolean is_device_online(UUID device_id) {

        if (map.isEmpty() && hardware_list.isEmpty()) {
            return false;
        } else if (map.isEmpty()) {
            for (DeviceStatus status : hardware_list) {
                map.put(status.hardware_id, status);
            }
        }

       return map.get(device_id).online_status;
    }




/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(List<String> full_ids) {

        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_Hardware.CHANNEL);
        request.set("full_ids", Json.toJson(full_ids) );

        return request;
    }


/* HELP CLASS  -------------------------------------------------------------------------------------------------------*/

    public static class DeviceStatus{

        public DeviceStatus() {}

        @Constraints.Required  public UUID hardware_id;
        public boolean online_status;

    }



}
