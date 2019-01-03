package websocket.messages.tyrion_with_becki;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import websocket.interfaces.Portal;
import websocket.messages.common.abstract_class.WS_AbstractMessage;

import java.util.UUID;

public class WSM_Echo extends WS_AbstractMessage {

    @JsonProperty public static final String messageType = "becki_object_update";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @JsonProperty public static final String message_channel =  Portal.CHANNEL;

    //------------------------------------------------------------------

    @JsonIgnore  public Class cls;

    @JsonProperty public String model;
    @JsonProperty public UUID model_id;

// -------------------------------------------------------------------------------------------------------------------
    public WSM_Echo(Class<?> cls, UUID id) {
        this.cls = cls;
        this.model_id = id;
        this.model = cls.getSimpleName().replace("Model_", "");
    }

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    public ObjectNode make_request() {
        return Json.newObject()
                .put("message_type", messageType)
                .put("message_channel", message_channel)
                .put("model", model)
                .put("model_id", model_id.toString());
    }
}