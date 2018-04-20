package websocket.messages.tyrion_with_becki;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import websocket.messages.common.abstract_class.WS_AbstractMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WS_Message_RefreshTouch extends WS_AbstractMessage {

    @JsonProperty public static final String messageType = WSM_Echo.messageType;

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @JsonProperty public static final String message_channel = WSM_Echo.message_channel;


//------------------------------------------------------------------

    public String command;
    public List<UUID> person_ids = new ArrayList<>();

// -------------------------------------------------------------------------------------------------------------------

   public WS_Message_RefreshTouch(String command, UUID person_id) {
       System.out.println("WS_Message_RefreshTouch:: command " + command + " UUID: " + person_id);
        this.command = command;
        this.person_ids.add(person_id);
   }

   public WS_Message_RefreshTouch(String command, List<UUID> person_ids) {
       this.command = command;
       this.person_ids.addAll(person_ids);
   }

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    public ObjectNode make_request() {
        return Json.newObject()
                .put("message_type", messageType)
                .put("message_channel", message_channel)
                .put("model", command)
                .put("model_id","");
    }
}