package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model that you will get, if login was successful",
        value = "Login_Result")
public class Swagger_Login_Token {

    @ApiModelProperty(value = "X-AUTH-TOKEN - used this token in HTML head for verifying the identities", readOnly = true)
    public String authToken;

}
