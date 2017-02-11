package utilities.web_socket.message_objects;

import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.web_socket.message_objects.common.WS_AbstractMessage;

public class WS_CheckHomerServerPermission extends WS_AbstractMessage {

    @ApiModelProperty(required = true) @Constraints.Required  public String hashToken;
}
