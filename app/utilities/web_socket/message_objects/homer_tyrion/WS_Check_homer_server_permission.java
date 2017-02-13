package utilities.web_socket.message_objects.homer_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.project.b_program.servers.Model_HomerServer;
import play.data.validation.Constraints;
import play.libs.Json;
import utilities.web_socket.message_objects.common.WS_AbstractMessage;

public class WS_Check_homer_server_permission extends WS_AbstractMessage {

    @JsonIgnore
    public static final String messageType = "getVerificationToken";


    @Constraints.Required public String hashToken;


    @JsonIgnore
    public ObjectNode make_request() {

        ObjectNode request= Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerServer.CHANNEL);

        return request;
    }

}
