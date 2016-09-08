package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new SecurityRole (Group)",
          value = "SecurityRole_New")
public class Swagger_SecurityRole_New {

    @Constraints.Required
    @Constraints.MinLength(value = 4,  message = "The name must have at least 4 characters.")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters.")
    @ApiModelProperty(required = true, value = "Length must be between 4 and 60 characters.")
    public String name;


    @ApiModelProperty(required = false, value = "Not required, But strongly recommended, Maximum length is 255 characters")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    public String description;
}
