package web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Board;

public class WS_Message_Device_connected extends WS_AbstractMessage_Board {

    // MessageType
    @JsonIgnore public static final String messageType = "deviceConnected";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required public String deviceId;
    @Constraints.Required public boolean online;

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/



}
