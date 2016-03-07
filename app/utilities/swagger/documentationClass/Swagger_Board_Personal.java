package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for update personal description of Board",
          value = "Board_Personal_Description")
public class Swagger_Board_Personal {

    @Constraints.Required
    @Constraints.MinLength(value = 8, message = "The description must have at least 8 characters")
    @ApiModelProperty(required = true)
    public String personal_description;

}
