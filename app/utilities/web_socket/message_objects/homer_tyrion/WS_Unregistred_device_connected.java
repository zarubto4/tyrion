package utilities.web_socket.message_objects.homer_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;
import utilities.web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

public class WS_Unregistred_device_connected extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore
    public static final String messageType = "yoda_unauthorized_logging";


    @Constraints.Required  public String deviceId;


}

