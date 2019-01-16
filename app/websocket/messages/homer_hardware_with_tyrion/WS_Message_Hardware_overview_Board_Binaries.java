package websocket.messages.homer_hardware_with_tyrion;

import play.data.validation.Constraints;

import javax.validation.Valid;

public class WS_Message_Hardware_overview_Board_Binaries {

    public WS_Message_Hardware_overview_Board_Binaries() {}

    @Valid public WS_Message_Hardware_overview_Board_Binaries_Info firmware;
    @Valid public WS_Message_Hardware_overview_Board_Binaries_Info bootloader;
    @Valid public WS_Message_Hardware_overview_Board_Binaries_Info backup;
    @Valid public WS_Message_Hardware_overview_Board_Binaries_Info buffer;


}
