package utilities.webSocket.messageObjects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.io.IOException;

public class WS_ValidPersonToken_OnHomerServer {

    public static WS_ValidPersonToken_OnHomerServer getObject(ObjectNode json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json.toString(), WS_ValidPersonToken_OnHomerServer.class);
    }

    @ApiModelProperty(required = true) @Constraints.Required  public String token;

    @ApiModelProperty(required = true) @Constraints.Required  public String messageType;
    @ApiModelProperty(required = true) @Constraints.Required  public String messageId;
    @ApiModelProperty(required = true) @Constraints.Required  public String messageChannel;

}
