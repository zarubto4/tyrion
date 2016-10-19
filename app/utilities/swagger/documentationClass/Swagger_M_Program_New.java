package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new M_Program",
        value = "M_Program_New")
public class Swagger_M_Program_New {


    @Constraints.Required
    @ApiModelProperty(required = true, value = "Required valid screen_type_id")
    public String  screen_type_id;


    @ApiModelProperty(required = false, value = "program_description can be null or maximum length of 255 characters.")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    public String  description;


    @Constraints.Required
    @Constraints.MinLength(value = 8, message = "The name must have at least 8 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 60 characters.")
    public String  name;

}
