package utilities.web_socket.message_objects.common;

import play.data.validation.Constraints;

public abstract class WS_AbstractMessageBoard extends WS_AbstractMessageInstance {

    @Constraints.Required public String firmware_version_core;
    @Constraints.Required public String firmware_version_mbed;     //
    @Constraints.Required public String firmware_version_lib;
    @Constraints.Required public String firmware_build_id;         // Číslo Buildu
    @Constraints.Required public String firmware_build_datetime;   // Kdy bylo vybylděno

    @Constraints.Required public String bootloader_version_core;
    @Constraints.Required public String bootloader_version_mbed;
    @Constraints.Required public String bootloader_build_id;
    @Constraints.Required public String bootloader_build_datetime;
}
