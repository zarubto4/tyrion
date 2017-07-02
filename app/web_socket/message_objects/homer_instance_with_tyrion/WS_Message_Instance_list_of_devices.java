package web_socket.message_objects.homer_instance_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerInstance;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class WS_Message_Instance_list_of_devices extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "instance_device_list";


    @Valid
    public List<String> device_ids = new ArrayList<>();


//-----------------------------------------------------------------------------------

    @JsonIgnore
    public ObjectNode make_request() {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerInstance.CHANNEL);

        return request;
    }

}
