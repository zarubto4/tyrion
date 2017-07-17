package web_socket.message_objects.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

public class WS_Message_Hardware_disconnected extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String message_type = "hardware_disconnected";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required public String hardware_id;

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/


}
