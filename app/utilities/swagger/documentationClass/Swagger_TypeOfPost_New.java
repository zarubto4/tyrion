package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for create new TypeOfPost",
        value = "TypeOfPost_New")
public class Swagger_TypeOfPost_New {

    @Constraints.Required
    @Constraints.MinLength(value = 3,  message = "The type must have at least 3 characters.")
    @Constraints.MaxLength(value = 60, message = "The type must not have more than 60 characters.")
    @ApiModelProperty(required = true, value = "Length must be between 3 and 60 characters.")
    public String type;
}
