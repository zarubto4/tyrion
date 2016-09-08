package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for update personal description of Board",
          value = "Board_Personal_Description")
public class Swagger_Board_Personal {

    @Constraints.Required
    @Constraints.MinLength(value = 24, message = "The description must have at least 24 characters")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    @ApiModelProperty(required = true, value = "Length must be between 24 and 255 characters.")
    public String personal_description;

}
