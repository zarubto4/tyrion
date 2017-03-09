package utilities.web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.project.b_program.instnace.Model_HomerInstance;
import play.libs.Json;
import utilities.web_socket.message_objects.common.abstract_class.WS_AbstractMessageBoard;
import utilities.web_socket.message_objects.common.abstract_class.WS_AbstractMessageInstance;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class WS_Get_summary_information  extends WS_AbstractMessageInstance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "instanceSummary";

    @Valid
    public List<WS_Yoda_connected> masterDeviceList = new ArrayList<>();


    // Pomocné metody
    @JsonIgnore
    public boolean deviceIsOnline(String device_id){

        WS_AbstractMessageBoard device = getDeviceStats(device_id);

        if (device == null ) {
            return false;
        }

        return device.online_status;
    }

    @JsonIgnore
    public WS_AbstractMessageBoard getDeviceStats(String device_id){

        for(WS_Yoda_connected yoda : masterDeviceList){

            if(yoda.deviceId.equals(device_id)) return yoda;

            for(WS_Device_connected device : yoda.deviceList){
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
