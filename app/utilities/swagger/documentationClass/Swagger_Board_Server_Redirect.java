package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.enums.Enum_Board_Command;

@ApiModel(description = "",
          value = "Board_Server_Redirect")
public class Swagger_Board_Server_Redirect {

    @ApiModelProperty(required = false, value = "Required only if server_url & server_port is null") public String server_id;

    @ApiModelProperty(required = false, value = "Required only if server_id is null && server_port is not null ") public String server_url;
    @ApiModelProperty(required = false, value = "Required only if server_id is null && server_url is not null") public String server_port;

}
