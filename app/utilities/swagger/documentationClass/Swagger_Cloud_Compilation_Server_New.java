package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new Compilation Server",
        value = "Compilation_Server")
public class Swagger_Cloud_Compilation_Server_New {

    @Constraints.Required
    @ApiModelProperty(value = "Unique name For Compiler server", required = true)
    public String server_name;

}
