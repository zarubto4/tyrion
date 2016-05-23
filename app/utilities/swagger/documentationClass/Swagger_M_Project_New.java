package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for update M_Project",
          value = "M_Project_New")
public class Swagger_M_Project_New {

    @ApiModelProperty(required = false)
    public String program_description;


    @Constraints.Required
    @Constraints.MinLength(value = 4)
    @ApiModelProperty(required = true, value = "MinLength >= 4")
    public String program_name;

}
