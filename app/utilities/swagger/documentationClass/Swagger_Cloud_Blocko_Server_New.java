package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new Blocko Server",
          value = "Blocko_Server")
public class Swagger_Cloud_Blocko_Server_New {

    @Constraints.Required
    @ApiModelProperty(value = "Unique name For Blocko server", required = true)
    public String server_name;

}
