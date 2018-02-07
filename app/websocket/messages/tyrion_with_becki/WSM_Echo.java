package websocket.messages.tyrion_with_becki;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import utilities.model.Echo;
import utilities.model.BaseModel;
import play.libs.Json;
import websocket.interfaces.WS_Portal;
import websocket.messages.common.abstract_class.WS_AbstractMessage;

import java.util.UUID;

public class WSM_Echo extends WS_AbstractMessage {

    @JsonProperty public static final String messageType = "becki_object_update";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @JsonProperty public static final String message_channel =  WS_Portal.message_channel;

    //------------------------------------------------------------------

    @JsonIgnore  public Class cls;
    @JsonIgnore  public UUID project_id;

    //------------------------------------------------------------------

    @JsonProperty public String model;
    @JsonProperty public UUID model_id;

    //------------------------------------------------------------------

// -------------------------------------------------------------------------------------------------------------------

   public WSM_Echo(Class<?> cls, UUID project_id, UUID model_id) {
        this.cls = cls;
        this.model_id = model_id;
        this.model = cls.getSimpleName().replace("Model_", "");
        this.project_id = project_id;
   }

    public WSM_Echo(Echo echo) {
        this.cls = echo.getClass();
        this.model_id = ((BaseModel) echo).id;
        this.model = cls.getSimpleName().replace("Model_", "");
        this.project_id = echo.getProjectId();
    }

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    public ObjectNode make_request() {
        return Json.newObject()
                .put("message_type", messageType)
                .put("message_channel", message_channel)
                .put("project_id", project_id.toString())
                .put("model", model)
                .put("model_id", model_id.toString());
    }
}