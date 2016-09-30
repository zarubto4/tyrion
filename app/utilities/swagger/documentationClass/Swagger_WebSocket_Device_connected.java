package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModelProperty;

public class Swagger_WebSocket_Device_connected {

    public Swagger_WebSocket_Device_connected(){}

    @ApiModelProperty(required = true) public boolean online;
    public String deviceId;

    public String firmware_version_core;
    public String firmware_version_mbed;
    public String firmware_version_lib;
    public String firmware_build_id;
    public String firmware_build_datetime;

    public String bootloader_version_core;
    public String bootloader_version_mbed;
    public String bootloader_build_id;
    public String bootloader_build_datetime;
}
