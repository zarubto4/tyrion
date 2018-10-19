package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

@ApiModel(
        value = "Facebook_LoginRedirect",
        description = "Json Model for Login with link for Redirection"
)
public class Swagger_Facebook_LoginRedirect extends _Swagger_Abstract_Default {

        @Constraints.Required
        @ApiModelProperty(value = "Required log_level", required = true)
        public String redirect_link;

}
