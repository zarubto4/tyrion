package web_socket.message_objects.homer_hardware_with_tyrion;

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

public class WS_Message_Hardware_Ping extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String message_type = "hardware_ping";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Valid public List<DevicePingStatus> hardware_list = new ArrayList<>();

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(List<String> device_ids) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_HomerInstance.CHANNEL);
        request.set("hardware_ids", Json.toJson(device_ids));

        return request;
    }



/* HELP CLASS  -------------------------------------------------------------------------------------------------------*/

    public static class DevicePingStatus{

        public DevicePingStatus(){}

        @Constraints.Required  public String hardware_id;
        @Constraints.Required  public Integer response_time;       // timestamp in milis  Limit 30 000  (30 sekund)

    }

}
