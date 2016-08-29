package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new Blocko Server",
          value = "Cloud_Homer_Server")
public class Swagger_Cloud_Homer_Server_New {

    @Constraints.Required
    @Constraints.MinLength(value = 6, message = "The name must have at least 6 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    @ApiModelProperty(value = "Unique name For Blocko cloud_blocko_server. Length must be between 6 and 60 characters", required = true)
    public String server_name;

}
