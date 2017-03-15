package utilities.web_socket.message_objects.compilator_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_CompilationServer;
import play.libs.Json;
import utilities.web_socket.message_objects.common.abstract_class.WS_AbstractMessageInstance;

public class WS_Ping_compilation_server extends WS_AbstractMessageInstance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "ping";

    @JsonIgnore
    public ObjectNode make_request() {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_CompilationServer.CHANNEL);

        return request;
    }


}