package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new Compilation Server",
        value = "Compilation_Server_New")
public class Swagger_Cloud_Compilation_Server_New {

    @Constraints.Required
    @Constraints.MinLength(value = 6, message = "The name must have at least 6 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    @ApiModelProperty(value = "Unique name For Compiler cloud_blocko_server, The name length must be between 6 and 60 characters", required = true)
    public String personal_server_name;

    public String server_url;

}
