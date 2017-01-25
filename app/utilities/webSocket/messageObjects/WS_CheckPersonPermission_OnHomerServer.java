package utilities.webSocket.messageObjects;

import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

public class WS_CheckPersonPermission_OnHomerServer {

    @ApiModelProperty(required = true) @Constraints.Required  public String email;
    @ApiModelProperty(required = true) @Constraints.Required  public String password;
    @ApiModelProperty(required = true) @Constraints.Required  public String unique_homer_server_identification;
    @ApiModelProperty(required = true) @Constraints.Required  public String user_agent;

}
