package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new B_Program",
        value = "B_Program_New")
public class Swagger_B_Program_New {

    @Constraints.Required
    @ApiModelProperty(required = false, value = "program_description can be null")
    public String program_description;

    @Constraints.Required
    @Constraints.MinLength(value = 8)
    @ApiModelProperty(required = true, value = "MinLength >= 8")
    public String name;
}
