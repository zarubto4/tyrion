package utilities.webSocket.messageObjects;

import io.swagger.annotations.ApiModelProperty;

public class WS_DeviceConnected {

    @ApiModelProperty(required = true) public String deviceId;
    @ApiModelProperty(required = true) public boolean online;
    @ApiModelProperty(required = true) public String firmware_version_core;
    @ApiModelProperty(required = true) public String firmware_version_mbed;
    @ApiModelProperty(required = true) public String firmware_version_lib;
    @ApiModelProperty(required = true) public String firmware_build_id;

    @ApiModelProperty(required = true) public String bootloader_version_core;
    @ApiModelProperty(required = true) public String bootloader_version_mbed;
    @ApiModelProperty(required = true) public String bootloader_build_id;
    @ApiModelProperty(required = true) public String bootloader_build_datetime;


}
