package utilities.web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;
import utilities.web_socket.message_objects.common.abstract_class.WS_AbstractMessageBoard;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;


public class WS_Yoda_connected extends WS_AbstractMessageBoard {

    // MessageType
    @JsonIgnore public static final String messageType = "yodaConnected";

    @Constraints.Required public String deviceId;

    @Valid
    public List<WS_Device_connected> deviceList  = new ArrayList<>();
}
