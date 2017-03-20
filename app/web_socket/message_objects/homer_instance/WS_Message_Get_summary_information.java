package web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerInstance;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Board;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class WS_Message_Get_summary_information extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "instanceSummary";

    @Valid
    public List<WS_Message_Yoda_connected> masterDeviceList = new ArrayList<>();


    // Pomocné metody
    @JsonIgnore
    public boolean deviceIsOnline(String device_id){

        WS_AbstractMessage_Board device = getDeviceStats(device_id);

        if (device == null ) {
            return false;
        }

        return device.online_status;
    }

    @JsonIgnore
    public WS_AbstractMessage_Board getDeviceStats(String device_id){

        for(WS_Message_Yoda_connected yoda : masterDeviceList){

            if(yoda.deviceId.equals(device_id)) return yoda;

            for(WS_Message_Device_connected device : yoda.deviceList){
                if(device.deviceId.equals(device_id)) return device;
            }
        }

        return null;
    }


 //-----------------------------------------------------------------------------------


    @JsonIgnore
    public ObjectNode make_request(Model_HomerInstance instance) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerInstance.CHANNEL);
        request.put("instanceId", instance.blocko_instance_name);

        return request;
    }

}
