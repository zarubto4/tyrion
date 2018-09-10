package websocket.messages.homer_hardware_with_tyrion.updates;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;
import utilities.enums.Enum_HardwareHomerUpdate_state;
import websocket.messages.common.abstract_class.WS_AbstractMessage_Instance;

import java.util.UUID;

public class WS_Message_Hardware_UpdateProcedure_Progress extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String message_type = "update_hardware_progress";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required public UUID tracking_id   = null;
    @Constraints.Required public UUID tracking_group_id = null;

    @Constraints.Max(value = 100) @Constraints.Min(value = 0) public Integer percentage_progress;

    @Constraints.Required public String phase;

    @Constraints.Required public String full_id = null;


    public Enum_HardwareHomerUpdate_state get_phase() {
        return Enum_HardwareHomerUpdate_state.get_state(phase);
    }


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

}
