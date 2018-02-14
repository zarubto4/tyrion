package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for logi via Facebook, Github etc.",
        value = "SocialNetwork_Login ")
public class Swagger_SocialNetwork_Login {

    @Constraints.Required public String redirect_url;

}
