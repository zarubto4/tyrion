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
    @ApiModelProperty(value = "Personal name For Homer cloud_homer_server. Length must be between 6 and 60 characters", required = true)
    public String personal_server_name;

    public int mqtt_port;


    @Constraints.MinLength(value = 4, message = "The name must have at least 4 characters")
    @Constraints.MaxLength(value = 40, message = "The name must not have more than 5 characters")
    public String mqtt_username;


    @Constraints.MinLength(value = 4, message = "The name must have at least 4 characters")
    @Constraints.MaxLength(value = 40, message = "The name must not have more than 5 characters")
    public String mqtt_password;

    public int grid_port;
    public int web_view_port;

    @Constraints.MinLength(value = 6, message = "The name must have at least 4 characters")
    @Constraints.MaxLength(value = 60, message = "The name must have at least 60 characters")
    public String server_url;

}
