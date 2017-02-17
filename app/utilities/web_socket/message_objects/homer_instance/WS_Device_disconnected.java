package utilities.web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;
import utilities.web_socket.message_objects.common.abstract_class.WS_AbstractMessageInstance;

public class WS_Device_disconnected extends WS_AbstractMessageInstance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "deviceDisconnected";

    @Constraints.Required public String deviceId;

}
