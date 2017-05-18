package web_socket.message_objects.compilatorServer_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_CompilationServer;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

public class WS_Message_Ping_compilation_server extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String messageType = "ping";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/





/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request() {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_CompilationServer.CHANNEL);

        return request;
    }


}