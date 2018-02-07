package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for sending password recovery email",
        value = "Person_Password_RecoveryEmail")
public class Swagger_Person_Password_RecoveryEmail {

    @Constraints.Email
    @Constraints.Required
    @ApiModelProperty(required = true, value = "Required valid mail ")
    public String mail;
}
