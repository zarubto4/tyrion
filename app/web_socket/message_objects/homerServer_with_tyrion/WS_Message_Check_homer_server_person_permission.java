package web_socket.message_objects.homerServer_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiModelProperty;
import models.Model_FloatingPersonToken;
import models.Model_Person;
import models.Model_HomerServer;
import play.data.validation.Constraints;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

public class WS_Message_Check_homer_server_person_permission extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore
    public static final String messageType = "checkUserPermission";

    @ApiModelProperty(required = true) @Constraints.Required  public String email;
    @ApiModelProperty(required = true) @Constraints.Required  public String password;
    @ApiModelProperty(required = true) @Constraints.Required  public String user_agent;

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
    public ObjectNode make_request_success(Model_HomerServer server, Model_Person person, Model_FloatingPersonToken token) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerServer.CHANNEL);
        request.put("status", "success");
        request.put("message", "Token is not valid");
        request.put("messageId", messageId);
        request.put("read_permission",   server.read_permission(person));
        request.put("edit_permission",   server.edit_permission(person));
        request.put("delete_permission", server.delete_permission(person));
        request.put("create_permission", server.create_permission(person));
        request.put("authToken", token.authToken);
        return request;
    }

    @JsonIgnore
    public ObjectNode make_request_permission_required() {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerServer.CHANNEL);
        request.put("status", "error");
        request.put("message", "Permission Required");

        return request;
    }

}
