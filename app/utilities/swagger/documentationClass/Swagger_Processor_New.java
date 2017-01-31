package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for new Processor",
          value = "Processor_New")
public class Swagger_Processor_New {

    @Constraints.Required
    @Constraints.MinLength(value = 8, message = "The description must have at least 8 characters")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    @ApiModelProperty(required = true, value = "Length must be between 24 and 255 characters.")
    public String  description;


    @Constraints.Required
    @Constraints.MinLength(value = 4)
    @ApiModelProperty(required = true, value = "The Processor Code must have at least 4 characters")
    public String  processor_code;


    @Constraints.Required
    @Constraints.MinLength(value = 4,  message = "The name must have at least 4 characters.")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters.")
    @ApiModelProperty(required = true, value = "Length must be between 4 and 60 characters.")
    public String  processor_name;


    @Constraints.Required
    @Constraints.Min(value = 0)
    @ApiModelProperty(required = true) public Integer speed;

}
