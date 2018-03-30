package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.UUID;

@ApiModel(description = "Json Model that you will get, if login was successful",
        value = "Login_Token")
public class Swagger_Login_Token extends _Swagger_Abstract_Default {

    @ApiModelProperty(value = "X-AUTH-TOKEN - used this token in HTML head for verifying the identities", readOnly = true, required = true)
    public UUID auth_token;

}
