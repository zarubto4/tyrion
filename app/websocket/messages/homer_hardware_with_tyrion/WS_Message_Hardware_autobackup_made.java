package websocket.messages.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;
import websocket.messages.common.abstract_class.WS_AbstractMessage_Hardware;

public class WS_Message_Hardware_autobackup_made extends WS_AbstractMessage_Hardware {

    // MessageType
    @JsonIgnore public static final String message_type = "hardware_autobackup_made";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


    @Constraints.Required public String build_id;

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/


}
