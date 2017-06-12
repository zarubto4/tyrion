package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for new Basic Login throw the social networks",
          value = "Social_Login")
public class Swagger_SocialNetwork_Result {
    @ApiModelProperty(readOnly = true, allowableValues = "Facebook,GitHub" )
    public String type;

    @ApiModelProperty(value = "URL that you have to redirect user", readOnly = true)
    public String redirect_url;

    @ApiModelProperty(value = "token, that you have to used in HTML head for verifying the identities", readOnly = true)
    public String authToken;
}
