package web_socket.message_objects.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

public class WS_Message_Hardware_connected extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String messageType = "hardware_connected";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required public String device_id;


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/



}
