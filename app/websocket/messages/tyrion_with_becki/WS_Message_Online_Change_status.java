package websocket.messages.tyrion_with_becki;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import utilities.enums.NetworkStatus;
import websocket.interfaces.Portal;

import java.util.*;

public class WS_Message_Online_Change_status {

    @JsonProperty public static final String messageType = "online_status_change";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @JsonProperty public static final String message_channel =  Portal.CHANNEL;

    @JsonIgnore public UUID project_id; // Not required!

    @JsonProperty public String model;
    @JsonProperty public UUID model_id;
    @JsonProperty public NetworkStatus online_state;

// -------------------------------------------------------------------------------------------------------------------

    public WS_Message_Online_Change_status(Class<?> cls, UUID model_id, NetworkStatus status) {
        this.model_id = model_id;
        this.model = cls.getSimpleName().replace("Model_", "");
        this.online_state = status;
    }

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request() {
        return Json.newObject()
                .put("message_type", messageType)
                .put("message_channel", message_channel)
                .put("model", model)
                .put("model_id", model_id.toString())
                .put("online_state", online_state.name());
    }
}