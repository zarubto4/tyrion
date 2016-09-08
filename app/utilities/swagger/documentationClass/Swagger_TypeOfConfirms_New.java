package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new TypeOfConfirms",
          value = "TypeOfConfirms_New")
public class Swagger_TypeOfConfirms_New {

    @Constraints.Required
    @Constraints.MinLength(value = 8,  message = "The type must have at least 8 characters.")
    @Constraints.MaxLength(value = 60, message = "The type must not have more than 60 characters.")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 60 characters.")
    public String type;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String color;

    @Constraints.Required
    @Constraints.Min(value = 0)
    @ApiModelProperty(required = true, value = "must be positive")
    public Integer size;
}
