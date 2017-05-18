package web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

public class WS_Message_Yoda_disconnected extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore
    public static final String messageType = "yodaDisconnected";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required public String deviceId;

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

}
