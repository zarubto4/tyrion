package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model with email and password",
        value = "EmailAndPassword")
public class Swagger_EmailAndPassword extends Swagger_EmailRequired {

    @Constraints.Required
    @Constraints.MinLength(value = 8, message = "The password must have at least 8 characters")
    @Constraints.MaxLength(value = 60, message = "The password must not have more than 60 characters")
    @ApiModelProperty(value = "The password length must be between 8 and 60 characters", required = true)
    public String password;
}
