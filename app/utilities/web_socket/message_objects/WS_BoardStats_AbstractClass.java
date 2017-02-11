package utilities.web_socket.message_objects;

import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.web_socket.message_objects.common.WS_AbstractMessage;

public class WS_BoardStats_AbstractClass extends WS_AbstractMessage {

    @ApiModelProperty(required = true) @Constraints.Required public String firmware_version_core;
    @ApiModelProperty(required = true) @Constraints.Required public String firmware_version_mbed;     //
    @ApiModelProperty(required = true) @Constraints.Required public String firmware_version_lib;
    @ApiModelProperty(required = true) @Constraints.Required public String firmware_build_id;         // Číslo Buildu
    @ApiModelProperty(required = true) @Constraints.Required public String firmware_build_datetime;   // Kdy bylo vybylděno

    @ApiModelProperty(required = true) @Constraints.Required public String bootloader_version_core;
    @ApiModelProperty(required = true) @Constraints.Required public String bootloader_version_mbed;
    @ApiModelProperty(required = true) @Constraints.Required public String bootloader_build_id;
    @ApiModelProperty(required = true) @Constraints.Required public String bootloader_build_datetime;

}
