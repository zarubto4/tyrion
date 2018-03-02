package websocket.messages.homer_hardware_with_tyrion.updates;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Hardware;
import play.libs.Json;
import utilities.swagger.output.Swagger_UpdatePlan_brief_for_homer;
import websocket.messages.common.abstract_class.WS_AbstractMessage_Instance;

import java.util.List;

public class WS_Message_Hardware_UpdateProcedure_Command extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String messageType = "hardware_update";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(List<Swagger_UpdatePlan_brief_for_homer> tasks) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type", messageType);
        request.put("message_channel", Model_Hardware.CHANNEL);
        request.set("update_tasks", Json.toJson(tasks));

        return request;
    }
}
