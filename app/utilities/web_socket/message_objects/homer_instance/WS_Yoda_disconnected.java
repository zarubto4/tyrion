package utilities.web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;
import utilities.web_socket.message_objects.common.WS_AbstractMessage;

public class WS_Yoda_disconnected extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore
    public static final String messageType = "yodaDisconnected";

    @Constraints.Required public String deviceId;
}
