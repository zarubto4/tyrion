package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for change description for Permission",
        value = "Resend_Email")
public class Swagger_Resend_Email {

    @Constraints.Email
    @ApiModelProperty(required = false, value = "Email is voluntary Value! If is email empty, the system uses the e mail which is in registration.")
    public String mail;

}
