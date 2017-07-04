package web_socket.message_objects.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerInstance;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

public class WS_Message_Hardware_Restart extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String message_type = "hardware_restart";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    public Integer response_time;

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(String device_id) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_HomerInstance.CHANNEL);
        request.put("device_id", device_id);

        return request;
    }



/* HELP CLASS  -------------------------------------------------------------------------------------------------------*/

}
