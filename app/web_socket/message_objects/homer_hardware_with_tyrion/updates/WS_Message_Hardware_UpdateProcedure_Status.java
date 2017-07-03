package web_socket.message_objects.homer_hardware_with_tyrion.updates;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;


public class WS_Message_Hardware_UpdateProcedure_Status extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "update_hardware_status";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required public String actualization_procedure_id = null;
    @Constraints.Required public String c_program_update_plan_id = null;

    @Constraints.Required public String update_state;
    @Constraints.Required public String device_id = null;


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

}