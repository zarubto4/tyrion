package utilities.webSocket.messageObjects;

import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

public class WS_CheckHomerServerPermission {

    @ApiModelProperty(required = true) @Constraints.Required  public String hashToken;
}
