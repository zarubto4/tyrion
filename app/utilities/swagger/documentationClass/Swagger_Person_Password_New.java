package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for recovering person password",
        value = "Person_Password_New")
public class Swagger_Person_Password_New {

    @Constraints.Email
    @Constraints.Required
    @ApiModelProperty(required = true, value = "Required valid mail ")
    public String mail;

    @ApiModelProperty(value = "The password length must be between 8 and 60 characters", required = true)
    @Constraints.MinLength(value = 8, message = "The password must have at least 8 characters")
    @Constraints.MaxLength(value = 60, message = "The password must not have more than 60 characters")
    @Constraints.Required
    public String password;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Required password_recovery_token")
    public String password_recovery_token;
}