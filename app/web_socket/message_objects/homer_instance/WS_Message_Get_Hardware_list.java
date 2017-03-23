package web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerInstance;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;
import web_socket.message_objects.homer_instance.helps_objects.WS_Message_Help_Yoda_only_hardware_Id_list;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class WS_Message_Get_Hardware_list extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "hardwareList";

    @Valid
    public List<WS_Message_Help_Yoda_only_hardware_Id_list> hardwareIdList = new ArrayList<>();


    @JsonIgnore
    public WS_Message_Help_Yoda_only_hardware_Id_list getListWithYoda(String yodaId){

        for(WS_Message_Help_Yoda_only_hardware_Id_list hardwareId: hardwareIdList){
            if(hardwareId.deviceId.equals(yodaId)) return hardwareId;
        }

        return null;
    }



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