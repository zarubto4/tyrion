package utilities.webSocket.messageObjects;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class WS_YodaConnected {

    @ApiModelProperty(required = true) public String instanceId;
    @ApiModelProperty(required = true) public String deviceId;
    @ApiModelProperty(required = true) public boolean autobackup;

    @ApiModelProperty(required = true) public String firmware_version_core;
    @ApiModelProperty(required = true) public String firmware_version_mbed;     //
    @ApiModelProperty(required = true) public String firmware_version_lib;
    @ApiModelProperty(required = true) public String firmware_build_id;         // Číslo Buildu
    @ApiModelProperty(required = true) public String firmware_build_datetime;   // Kdy bylo vybylděno

    @ApiModelProperty(required = true) public String bootloader_version_core;
    @ApiModelProperty(required = true) public String bootloader_version_mbed;
    @ApiModelProperty(required = true) public String bootloader_build_id;
    @ApiModelProperty(required = true) public String bootloader_build_datetime;

    @Valid
    public List<WS_DeviceConnected> devices_summary  = new ArrayList<>();
}
