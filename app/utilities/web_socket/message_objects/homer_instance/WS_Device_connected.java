package utilities.web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import utilities.web_socket.message_objects.common.WS_AbstractMessage;

public class WS_Device_connected extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore
    public static final String messageType = "deviceConnected";

    @ApiModelProperty(required = true) public String deviceId;
    @ApiModelProperty(required = true) public boolean online;


}
