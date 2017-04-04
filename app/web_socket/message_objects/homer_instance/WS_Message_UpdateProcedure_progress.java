package web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;


public class WS_Message_UpdateProcedure_progress extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "updateProcedure_progress";

    public String  typeOfProgress;
    public Integer percentageProgress;
    public String  updatePlanId;

}
