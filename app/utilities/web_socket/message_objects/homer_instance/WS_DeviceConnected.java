package utilities.web_socket.message_objects.homer_instance;

import io.swagger.annotations.ApiModelProperty;
import utilities.web_socket.message_objects.WS_BoardStats_AbstractClass;

public class WS_DeviceConnected extends WS_BoardStats_AbstractClass {

    @ApiModelProperty(required = true) public String deviceId;
    @ApiModelProperty(required = true) public boolean online;


}
