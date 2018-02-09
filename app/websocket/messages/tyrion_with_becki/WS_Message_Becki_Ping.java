package websocket.messages.tyrion_with_becki;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import websocket.interfaces.WS_Portal;
import websocket.messages.common.abstract_class.WS_AbstractMessage_Instance;

public class WS_Message_Becki_Ping extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String messageType = "becki_ping";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/





/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request() {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type", messageType);
        request.put("message_channel", WS_Portal.CHANNEL);

        return request;
    }
}