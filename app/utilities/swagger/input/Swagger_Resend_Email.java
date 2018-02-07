package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for change description for Permission",
        value = "Resend_Email")
public class Swagger_Resend_Email {

    @Constraints.Email
    @ApiModelProperty(required = false, value = "Email is optional value! If it is empty, default invoice email will be used.")
    public String mail;

}
