package web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;


public class WS_Message_UpdateProcedure_result extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "updatePairProcedureResult";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required    public String updatePlanId;
    @Constraints.Required    public String updateState;

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

}