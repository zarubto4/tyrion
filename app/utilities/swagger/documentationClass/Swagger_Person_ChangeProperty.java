package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for changing Person property",
        value = "Person_ChangeProperty")
public class Swagger_Person_ChangeProperty {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Required property ('password' or 'email')")

    public String property;

    @Constraints.Email
    @ApiModelProperty(required = false, value = "Valid mail ")
    public String email;

    @ApiModelProperty(value = "The password length must be between 8 and 60 characters", required = false)
    @Constraints.MinLength(value = 8, message = "The password must have at least 8 characters")
    @Constraints.MaxLength(value = 60, message = "The password must not have more than 60 characters")
    public String password;
}
