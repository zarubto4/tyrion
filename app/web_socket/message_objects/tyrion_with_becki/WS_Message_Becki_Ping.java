package web_socket.message_objects.tyrion_with_becki;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;
import web_socket.services.WS_Becki_Website;

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
        request.put("message_channel", WS_Becki_Website.CHANNEL);

        return request;
    }
}