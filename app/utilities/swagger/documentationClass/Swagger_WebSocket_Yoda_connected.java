package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model with List of Board ID",
          value = "YodaConnected")
public class Swagger_WebSocket_Yoda_connected {

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
    public List<Swagger_WebSocket_Device_connected> devices_summary  = new ArrayList<>();
}
