package utilities.webSocket.messageObjects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.io.IOException;

public class WS_CheckPersonPermission_OnHomerServer {


    public static WS_CheckPersonPermission_OnHomerServer getObject(ObjectNode json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json.toString(), WS_CheckPersonPermission_OnHomerServer.class);
    }




    @ApiModelProperty(required = true) @Constraints.Required  public String email;
    @ApiModelProperty(required = true) @Constraints.Required  public String password;
    @ApiModelProperty(required = true) @Constraints.Required  public String user_agent;

    @ApiModelProperty(required = true) @Constraints.Required  public String messageType;
    @ApiModelProperty(required = true) @Constraints.Required  public String messageId;
    @ApiModelProperty(required = true) @Constraints.Required  public String messageChannel;

}
