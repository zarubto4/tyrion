package websocket.messages.homer_instance_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Instance;
import play.libs.Json;
import websocket.messages.common.abstract_class.WS_AbstractMessage_Instance;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class WS_Message_Instance_status extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String message_type = "instances_status";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Valid public List<InstanceStatus> instances = new ArrayList<>();


    /**
     * The map was created for large fields. To avoid having to search the list of objects, the list will be remapped
     * on hashmap. So you can call the device directly by ID.
     *
     * Slower for a small number of elements - significantly faster for a large number of elements.
     */
    @JsonIgnore
    HashMap<UUID,InstanceStatus> map = new HashMap<>();
    public InstanceStatus get_status(UUID id) {

        if (map.isEmpty() && instances.isEmpty()) {
            return null;
        } else if (map.isEmpty()) {
            for (InstanceStatus status : instances) {
                map.put(status.instance_id, status);
            }
        }

        return map.get(id);
    }

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(List<String> instance_id) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_Instance.CHANNEL);
        request.set("instance_ids", Json.toJson(instance_id));

        return request;
    }



/* HELP CLASS  -------------------------------------------------------------------------------------------------------*/

    public static class InstanceStatus {

        public InstanceStatus() {}

        public UUID instance_id;
        public List<String> hardware_ids = new ArrayList<>();
        public boolean status;
        public String error_code;

    }

}
