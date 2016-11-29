package utilities.webSocket.messageObjects;

import io.swagger.annotations.ApiModelProperty;

public class WS_DeviceConnected extends WS_BoardStats_AbstractClass {

    @ApiModelProperty(required = true) public String deviceId;
    @ApiModelProperty(required = true) public boolean online;


}
