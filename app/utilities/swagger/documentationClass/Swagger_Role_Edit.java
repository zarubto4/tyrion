package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for change description for Permission",
        value = "Role_Edit")
public class Swagger_Role_Edit {

    @Constraints.Required
    @Constraints.MinLength(value = 4,  message = "The name must have at least 4 characters.")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters.")
    @ApiModelProperty(required = true, value = "Length must be between 4 and 60 characters.")
    public String name;

    @ApiModelProperty(required = false, value = "description can be null or maximum length of 255 characters.")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    public String description;

}
