package web_socket.message_objects.homer_with_tyrion.verification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import play.libs.Json;
import utilities.errors.ErrorCode;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

public class WS_Message_Homer_Verification_result extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String messageType = "homer_verification_result";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/



/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(boolean verify, String token) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();

        if(verify) {

            request.put("message_type", "homer_verification_result");
            request.put("status", "success");
            request.put("token", token);
            return request;

        }else {
            request.put("message_type", "homer_verification_result");
            request.put("status", "error");
            request.put("error_message", ErrorCode.UNAUTHORIZED_CONNECTION.error_message());
            request.put("error_code", ErrorCode.UNAUTHORIZED_CONNECTION.error_code());
            return request;
        }

    }
}
