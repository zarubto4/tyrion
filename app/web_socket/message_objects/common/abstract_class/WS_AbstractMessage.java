package web_socket.message_objects.common.abstract_class;

import play.data.validation.Constraints;

public abstract class  WS_AbstractMessage {

    @Constraints.Required public String websocket_identificator;   // Becki ID, Homer Server ID Etc - Dosazuje do JSONu WS_SendMesage
    @Constraints.Required public String message_type;
    @Constraints.Required public String message_id;
    @Constraints.Required public String message_channel;

    public String status = "error";
    public String error  = null;
    public Integer error_code = null;

}


