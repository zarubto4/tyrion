package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.UUID;

@ApiModel(description = "",
          value = "Board_Server_Redirect")
public class Swagger_Board_Server_Redirect {

    @ApiModelProperty(required = false, value = "Required only if server_url & server_port is null") public UUID server_id;

    @ApiModelProperty(required = false, value = "Required only if server_id is null && server_port is not null ") public String server_url;
    @ApiModelProperty(required = false, value = "Required only if server_id is null && server_url is not null") public String server_port;

}
