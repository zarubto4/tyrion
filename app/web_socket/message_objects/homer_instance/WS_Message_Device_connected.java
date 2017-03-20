package web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Board;

public class WS_Message_Device_connected extends WS_AbstractMessage_Board {

    // MessageType
    @JsonIgnore
    public static final String messageType = "deviceConnected";

    @ApiModelProperty(required = true) public String deviceId;
    @ApiModelProperty(required = true) public boolean online;


}
