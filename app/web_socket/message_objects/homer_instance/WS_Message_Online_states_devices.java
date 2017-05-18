package web_socket.message_objects.homer_instance;

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

public class WS_Message_Online_states_devices extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String messageType = "deviceOnlineState";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Valid public List<DeviceStatus> deviceList = new ArrayList<>();


    public static class DeviceStatus{

        public DeviceStatus(){}

        @Constraints.Required  public String deviceId;
        public boolean online_status;
        public String error;
        public String errorCode;

    }


    /**
     * The map was created for large fields. To avoid having to search the list of objects, the list will be remapped
     * on hashmap. So you can call the device directly by ID.
     *
     * Slower for a small number of elements - significantly faster for a large number of elements.
     */
    @JsonIgnore HashMap<String,DeviceStatus> map = new HashMap<>();
    public boolean is_device_online(String device_id){

        if(map.isEmpty() && deviceList.isEmpty()){
            return false;
        }else if(map.isEmpty()) {
            for(DeviceStatus status : deviceList){
                map.put(status.deviceId, status);
            }
        }

       return map.get(device_id).online_status;
    }





/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(Model_HomerInstance instance, List<String> devicesId) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerInstance.CHANNEL);
        request.put("instanceId", instance.blocko_instance_name);
        request.set("devicesIds", Json.toJson(devicesId) );

        return request;
    }




}
