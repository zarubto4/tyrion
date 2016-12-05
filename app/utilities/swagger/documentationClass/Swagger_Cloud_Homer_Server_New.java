package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new Blocko Server",
          value = "Cloud_Homer_Server")
public class Swagger_Cloud_Homer_Server_New {

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The name must have at least 6 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    @ApiModelProperty(value = "Unique name For Blocko cloud_blocko_server. Length must be between 6 and 60 characters", required = true)
    public String server_name;

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The name must have at least 4 characters")
    @Constraints.MaxLength(value = 5, message = "The name must not have more than 5 characters")
    public String mqtt_port;

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The name must have at least 4 characters")
    @Constraints.MaxLength(value = 40, message = "The name must not have more than 5 characters")
    public String mqtt_username;

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The name must have at least 4 characters")
    @Constraints.MaxLength(value = 40, message = "The name must not have more than 5 characters")
    public String mqtt_password;

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The name must have at least 4 characters")
    @Constraints.MaxLength(value = 5, message = "The name must not have more than 5 characters")
    public String grid_port;

    public String webView_port;


    @Constraints.Required
    @Constraints.MinLength(value = 6, message = "The name must have at least 4 characters")
    @Constraints.MaxLength(value = 60, message = "The name must have at least 60 characters")
    public String server_url;

}
