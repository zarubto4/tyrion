package websocket.messages.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;
import websocket.messages.common.abstract_class.WS_AbstractMessage;
import websocket.messages.common.abstract_class.WS_AbstractMessage_Instance;

import java.util.UUID;

public class WS_Message_Hardware_disconnected extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String message_type = "hardware_disconnected";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

                          public UUID uuid;
    @Constraints.Required public String full_id;

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/


}
