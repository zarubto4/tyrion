package utilities.web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;
import utilities.web_socket.message_objects.common.WS_AbstractMessageInstance;

public class WS_WebView_token_verification extends WS_AbstractMessageInstance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "token_webView_verification";

    @Constraints.Required public String token;



    public ObjectNode get_result(boolean token_approve){

        ObjectNode result = Json.newObject();
        result.put("messageType", messageType);
        result.put("messageChannel", messageChannel);
        result.put("token_approve", token_approve);
        result.put("messageId", messageId);
        result.put("instanceId", instanceId);
        return result;

    }

}
