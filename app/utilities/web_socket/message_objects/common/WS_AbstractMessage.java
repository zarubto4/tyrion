package utilities.web_socket.message_objects.common;

import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

public abstract class  WS_AbstractMessage {

    @ApiModelProperty(required = true) @Constraints.Required  public String messageType;
    @ApiModelProperty(required = true) @Constraints.Required  public String messageId;
    @ApiModelProperty(required = true) @Constraints.Required  public String messageChannel;
    @ApiModelProperty(required = true) public String status;
}
