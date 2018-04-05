package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

@ApiModel(description = "Json Model for logi via Facebook, Github etc.",
        value = "SocialNetwork_Login ")
public class Swagger_SocialNetwork_Login extends _Swagger_Abstract_Default {

    @Constraints.Required public String redirect_url;

}
