package websocket.messages.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Instance;
import play.data.validation.Constraints;
import play.libs.Json;
import websocket.messages.common.abstract_class.WS_AbstractMessage_Instance;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WS_Message_Hardware_ping extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String message_type = "hardware_ping";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Valid public List<DevicePingStatus> hardware_list = new ArrayList<>();

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(List<UUID> ids) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_Instance.CHANNEL);
        request.set("uuid_ids", Json.toJson(ids));

        return request;
    }



/* HELP CLASS  -------------------------------------------------------------------------------------------------------*/

    public static class DevicePingStatus{

        public DevicePingStatus() {}

        @Constraints.Required  public UUID uuid;
        @Constraints.Required  public Integer response_time;       // timestamp in milis  Limit 30 000  (30 sekund)

    }

}
