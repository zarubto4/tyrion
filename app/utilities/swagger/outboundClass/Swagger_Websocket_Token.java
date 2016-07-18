package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model that you will get, if login was successful",
        value = "WebSocket_Token")
public class Swagger_Websocket_Token {

    @ApiModelProperty(value = "Swagger_Websocket_Token - used this token for WebSocket access. The lifetime of the token is 5 seconds. It is disposable. It can not be used twice. In the event of the expiration of the life of the disabled. ", readOnly = true, required = true)
    public String websocket_token;

}
