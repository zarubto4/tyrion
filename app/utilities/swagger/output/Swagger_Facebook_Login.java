package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.UUID;

@ApiModel(description = "Json Model for Login on Social Networks",
          value = "Facebook_Login")
public class Swagger_Facebook_Login extends _Swagger_Abstract_Default {

    @ApiModelProperty(required = true, value = "Valid URL fo redirect")
    public String link;

    @ApiModelProperty(required = true, value = "Token for Cookies")
    public UUID auth_token;
}
