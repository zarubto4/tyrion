package utilities.web_socket.message_objects.common;

import play.data.validation.Constraints;

public abstract class  WS_AbstractMessage {

                           public String messageType;
    @Constraints.Required  public String messageId;
    @Constraints.Required  public String messageChannel;

    public String status = "error";
    public String error  = null;

}


