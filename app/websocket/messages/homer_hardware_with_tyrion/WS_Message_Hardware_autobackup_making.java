package websocket.messages.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;
import websocket.messages.common.abstract_class.WS_AbstractMessage_Instance;

public class WS_Message_Hardware_autobackup_making extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String message_type = "hardware_autobackup_making";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required public String hardware_id;

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/


}
