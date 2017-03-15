package utilities.web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerInstance;
import play.data.validation.Constraints;
import play.libs.Json;
import utilities.web_socket.message_objects.common.abstract_class.WS_AbstractMessageInstance;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WS_Online_states_devices extends WS_AbstractMessageInstance{

    // MessageType
    @JsonIgnore
    public static final String messageType = "deviceOnlineState";


    // Obsah příchozí zprávy

    @Valid public List<DeviceStatus> deviceList = new ArrayList<>();


    public static class DeviceStatus{

        public DeviceStatus(){}

        @Constraints.Required  public String deviceId;
        public boolean online_status;
        public String error;
        public String errorCode;

    }


    /**
     * Mapa byla vytvořena za účelem velkých polí. Aby nebylo nutné hledat v seznamu objektů, přemapuje se list
     * na hashmapu. A tak zle volat device napřímo podle ID.
     *
     * Pomalejší u malého množství prvků - výrazně rychlejší u velkého množství prvků.
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




    //------------------------------------------------------------------------------------------------------------------


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
