package web_socket.message_objects.homer_hardware_with_tyrion.updates;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;
import utilities.enums.Enum_HardwareHomerUpdate_state;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;


public class WS_Message_Hardware_UpdateProcedure_Progress extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String message_type = "update_hardware_progress";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required public String tracking_id   = null;
    @Constraints.Required public String tracking_group_id = null;

    @Constraints.Max(value = 100) @Constraints.Min(value = 0) public Integer percentage_progress;

    @Constraints.Required public String phase;

    @Constraints.Required public String hardware_id = null;


    public Enum_HardwareHomerUpdate_state get_phase() {
        return Enum_HardwareHomerUpdate_state.get_state(phase);
    }


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

}
