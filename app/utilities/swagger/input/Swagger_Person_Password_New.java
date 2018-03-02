package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for recovering person password",
        value = "Person_Password_New")
public class Swagger_Person_Password_New extends Swagger_EmailAndPassword {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Required password_recovery_token")
    public String password_recovery_token;
}