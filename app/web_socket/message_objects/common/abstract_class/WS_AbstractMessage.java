package web_socket.message_objects.common.abstract_class;

import play.data.validation.Constraints;

public abstract class  WS_AbstractMessage {

    public String websocket_identificator;   // Becki ID, Homer Server ID Etc - Dosazuje do JSONu WS_SendMesage
    public String message_type;
    public String message_id;
    public String message_channel;

    @Constraints.Required  public String status = "error";
    public String error  = null;
    public Integer error_code = null;

}


