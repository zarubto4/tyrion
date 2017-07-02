package web_socket.message_objects.homer_instance_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerInstance;
import play.data.validation.Constraints;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WS_Message_Instance_status extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String messageType = "instanceStatus";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Valid public List<InstanceStatus> instance_list = new ArrayList<>();


    /**
     * The map was created for large fields. To avoid having to search the list of objects, the list will be remapped
     * on hashmap. So you can call the device directly by ID.
     *
     * Slower for a small number of elements - significantly faster for a large number of elements.
     */
    @JsonIgnore
    HashMap<String,InstanceStatus> map = new HashMap<>();
    public InstanceStatus get_status(String instnace_id){

        if(map.isEmpty() && instance_list.isEmpty()){
            return null;
        }else if(map.isEmpty()) {
            for(InstanceStatus status : instance_list){
                map.put(status.instnace_id, status);
            }
        }

        return map.get(instnace_id);
    }

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(List<String> instance_id) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerInstance.CHANNEL);
        request.set("instance_ids", Json.toJson(instance_id));

        return request;
    }



/* HELP CLASS  -------------------------------------------------------------------------------------------------------*/

    public static class InstanceStatus {

        public InstanceStatus(){}

        @Constraints.Required  public String instnace_id;
        public boolean online_status;
        public String error_code;

    }

}
