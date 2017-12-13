package web_socket.message_objects.tyrion_with_becki;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;
import web_socket.services.WS_Becki_Website;

import java.util.ArrayList;
import java.util.List;

public class WS_Message_RefreshTuch extends WS_AbstractMessage {

    @JsonProperty public static final String messageType = WS_Message_Update_model_echo.messageType;

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @JsonProperty public static final String message_channel = WS_Message_Update_model_echo.message_channel;


//------------------------------------------------------------------

    public String command;
    public List<String> person_ids = new ArrayList<>();

// -------------------------------------------------------------------------------------------------------------------

   public WS_Message_RefreshTuch(String command, String person_id){
        this.command = command;
        this.person_ids.add(person_id);
   }

   public WS_Message_RefreshTuch(String command, List<String> person_ids){
       this.command = command;
       this.person_ids.addAll(person_ids);
   }

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    public ObjectNode make_request(){
        return Json.newObject()
                .put("message_type", messageType)
                .put("message_channel", message_channel)
                .put("model", command)
                .put("model_id","");
    }
}