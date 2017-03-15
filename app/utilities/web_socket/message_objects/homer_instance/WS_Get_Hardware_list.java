package utilities.web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerInstance;
import play.libs.Json;
import utilities.web_socket.message_objects.common.abstract_class.WS_AbstractMessageInstance;
import utilities.web_socket.message_objects.homer_instance.Help_object.YodaOnlyHardwareIdList;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class WS_Get_Hardware_list extends WS_AbstractMessageInstance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "hardwareList";

    @Valid
    public List<YodaOnlyHardwareIdList> hardwareIdList = new ArrayList<>();


    @JsonIgnore
    public YodaOnlyHardwareIdList getListWithYoda(String yodaId){

        for(YodaOnlyHardwareIdList hardwareIdList: hardwareIdList){
            if(hardwareIdList.deviceId.equals(yodaId)) return hardwareIdList;
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