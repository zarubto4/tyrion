package web_socket.message_objects.homerServer_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

public class WS_Message_Unregistred_device_connected extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String messageType = "yoda_unauthorized_logging";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @Constraints.Required  public String deviceId;


}

