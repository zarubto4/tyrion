package utilities.web_socket.message_objects.homer_instance;

import play.data.validation.Constraints;
import utilities.web_socket.message_objects.common.WS_AbstractMessageInstance;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;


public class WS_YodaConnected  extends WS_AbstractMessageInstance {

    @Constraints.Required public String deviceId;
    @Constraints.Required public boolean autobackup;

    @Constraints.Required public String firmware_version_core;
    @Constraints.Required public String firmware_version_mbed;     //
    @Constraints.Required public String firmware_version_lib;
    @Constraints.Required public String firmware_build_id;         // Číslo Buildu
    @Constraints.Required public String firmware_build_datetime;   // Kdy bylo vybylděno

    @Constraints.Required public String bootloader_version_core;
    @Constraints.Required public String bootloader_version_mbed;
    @Constraints.Required public String bootloader_build_id;
    @Constraints.Required public String bootloader_build_datetime;

    @Valid
    public List<WS_DeviceConnected> devices_summary  = new ArrayList<>();
}
