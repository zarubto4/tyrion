package websocket.messages.homer_with_tyrion.verification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import play.libs.Json;
import utilities.errors.ErrorCode;
import websocket.messages.common.abstract_class.WS_AbstractMessage;

public class WS_Message_Homer_Verification_result extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String messageType = "homer_verification_result";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/



/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(boolean verify, String token) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_channel", Model_HomerServer.CHANNEL);
        request.put("message_type", "homer_verification_result");

        if (verify) {
            request.put("status", "success");
            request.put("token", token);
            return request;

        } else {

            request.put("status", "error");
            request.put("error_message", ErrorCode.TOKEN_IS_INVALID.error_message());
            request.put("error_code", ErrorCode.TOKEN_IS_INVALID.error_code());
            return request;

        }

    }
}
