package web_socket.message_objects.common.service_class;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import utilities.errors.ErrorCode;

/**
 * Created by zaruba on 05.07.17.
 */
public class WS_Message_Invalid_Message {

    // MessageType
    //  @JsonIgnore public static final String message_type = "tyrionRestartEcho";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/



/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static ObjectNode make_request(String message_type, String errors_log) {

        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("error_code", ErrorCode.INVALID_MESSAGE.error_code());
        request.put("error_message", ErrorCode.INVALID_MESSAGE.error_message());

        if(errors_log!=null) request.put("error_log", errors_log);

        return request;
    }

}
