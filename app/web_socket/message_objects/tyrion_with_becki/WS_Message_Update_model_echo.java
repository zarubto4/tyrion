package web_socket.message_objects.tyrion_with_becki;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.codehaus.jackson.map.ObjectMapper;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;
import web_socket.services.WS_Becki_Website;

public class WS_Message_Update_model_echo extends WS_AbstractMessage {

    @JsonProperty public static final String messageType = "object_update";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @JsonProperty public static final String messageChannel =  WS_Becki_Website.CHANNEL;

    //------------------------------------------------------------------

    @JsonIgnore  public Class cls;
    @JsonIgnore  public String project_id;

    //------------------------------------------------------------------

    @JsonProperty public String model;
    @JsonProperty public String model_id;

    //------------------------------------------------------------------

// -------------------------------------------------------------------------------------------------------------------

   public WS_Message_Update_model_echo(Class<?> cls, String project_id, String model_id){
        this.cls = cls;
        this.model_id = model_id;
        this.model = cls.getSimpleName();
        this.project_id = project_id;

   }

   public WS_Message_Update_model_echo(Class<?> cls, String project_id, Long model_id){
       this.cls = cls;
       this.model_id = model_id.toString();
       this.model = cls.getSimpleName();
       this.project_id = project_id;
   }

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    public ObjectNode get_request(){
        return new ObjectMapper().convertValue(this, ObjectNode.class);
    }



}
