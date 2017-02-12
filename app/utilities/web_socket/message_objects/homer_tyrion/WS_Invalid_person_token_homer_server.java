package utilities.web_socket.message_objects.homer_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiModelProperty;
import models.project.b_program.servers.Model_HomerServer;
import play.data.validation.Constraints;
import play.libs.Json;
import utilities.web_socket.message_objects.common.WS_AbstractMessage;

public class WS_Invalid_person_token_homer_server extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore
    public static final String messageType = "removePersonLoginToken";

    @ApiModelProperty(required = true) @Constraints.Required  public String token;



    @JsonIgnore
    public ObjectNode make_request_unsuccess() {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerServer.CHANNEL);
        request.put("status", "error");
        request.put("message", "Token is not valid");
        request.put("messageId", messageId);
        return request;
    }

    @JsonIgnore
    public ObjectNode make_request_success() {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerServer.CHANNEL);
        request.put("status", "success");
        request.put("message", "Token is not valid");
        request.put("messageId", messageId);

        return request;
    }



}
