package web_socket.message_objects.homerServer_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

public class WS_Message_Rejection_homer_server extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore
    public static final String messageType = "verificationFirstRequired";

    @JsonIgnore
    public ObjectNode make_request() {

        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerServer.CHANNEL);
        request.put("message", "Yor server is not verified yet!");

        return request;
    }

}
