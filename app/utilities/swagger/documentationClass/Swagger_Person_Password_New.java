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

    @ApiModelProperty(value = "The password must have at least 8 characters", required = true)
    @Constraints.MinLength(value = 8)
    @Constraints.Required
    public String password;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Required authToken")
    public String password_recovery_token;
}