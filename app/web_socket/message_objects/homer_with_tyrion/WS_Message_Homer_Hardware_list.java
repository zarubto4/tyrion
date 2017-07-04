package web_socket.message_objects.homer_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import play.data.validation.Constraints;
import play.libs.Json;
import utilities.enums.Enum_MProgram_SnapShot_settings;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

import java.util.ArrayList;
import java.util.List;

public class WS_Message_Homer_Hardware_list extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String message_type = "hardware_list";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required  public List<Hardware_pair> device_pairs = new ArrayList<>();


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request() {

        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_HomerServer.CHANNEL);

        return request;
    }


    public class Hardware_pair{

        public String device_id;
        public boolean online_state;

    }
}
