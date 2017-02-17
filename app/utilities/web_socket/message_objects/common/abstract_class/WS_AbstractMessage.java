package utilities.web_socket.message_objects.common.abstract_class;

import play.data.validation.Constraints;

public abstract class  WS_AbstractMessage {

    public String messageType;
    public String messageId;
    public String messageChannel;

    @Constraints.Required  public String status = "error";
    public String error  = null;
    public Integer errorCode  = null;

}


