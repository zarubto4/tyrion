package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for new C_Program",
          value = "C_Program_New")
public class Swagger_C_program_New {

    @Constraints.Required
    @Constraints.MinLength(value = 8)
    @ApiModelProperty(required = true, value = "MinLength >= 8")
    public String program_name;


    @Constraints.Required
    @ApiModelProperty(required = false)
    public String program_description;
}

