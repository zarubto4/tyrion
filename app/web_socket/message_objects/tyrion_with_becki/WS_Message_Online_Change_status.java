package web_socket.message_objects.tyrion_with_becki;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.codehaus.jackson.map.ObjectMapper;
import play.libs.Json;
import utilities.enums.Enum_Online_status;
import web_socket.services.WS_Becki_Website;

public class WS_Message_Online_Change_status {

    @JsonProperty public static final String messageType = "online_status_change";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @JsonProperty public static final String message_channel =  WS_Becki_Website.CHANNEL;

    //------------------------------------------------------------------

     @JsonIgnore public String project_id;

    //------------------------------------------------------------------

    @JsonProperty public String model;
    @JsonProperty public String model_id;
    @JsonProperty public Enum_Online_status online_status;

    //------------------------------------------------------------------


// -------------------------------------------------------------------------------------------------------------------

    public WS_Message_Online_Change_status(Class<?> cls, String project_id, String model_id, Enum_Online_status online_status){

        this.model_id = model_id;
        this.model = cls.getSimpleName();
        this.project_id = project_id;
        this.online_status = online_status;

    }

    public WS_Message_Online_Change_status(Class<?> cls, String project_id, Long model_id, Enum_Online_status online_status){

        this.model_id = model_id.toString();
        this.model = cls.getSimpleName();
        this.project_id = project_id;
        this.online_status = online_status;
    }


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(){
        return Json.newObject()
                .put("message_type", messageType)
                .put("message_channel", message_channel)
                .put("model", model)
                .put("model_id", model_id)
                .put("online_status", online_status.name());
    }


}
