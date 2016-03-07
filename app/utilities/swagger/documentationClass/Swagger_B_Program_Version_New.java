package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new Version of B_Program",
         value = "B_Program_Version_New")
public class Swagger_B_Program_Version_New {

    @Constraints.Required
    @Constraints.MinLength(value = 8, message = "The name must have at least 8 characters")
    @ApiModelProperty(required = true)
    public String version_name;


    @ApiModelProperty(required = false)
    public String version_description;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String program;
}
