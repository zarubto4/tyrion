package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for update personal description of Board",
          value = "Board_Personal_Description")
public class Swagger_Board_Personal {

    @Constraints.MaxLength(value = 31, message = "The name must not have more than 32 characters.")
    @ApiModelProperty(required = true, value = "Length must be between 0 and 32 characters.")
    public String name;


    @ApiModelProperty(required = false, value = "Description")
    public String description;

}
