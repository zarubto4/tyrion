package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for new Blocko Server",
          value = "Cloud_Homer_Server_New")
public class Swagger_HomerServer_New {

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The name must have at least 6 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    @ApiModelProperty(value = "Personal name For Homer cloud_homer_server. Length must be between 6 and 60 characters", required = true)
    public String personal_server_name;

    @Constraints.Required public int mqtt_port;

    @Constraints.Required public int grid_port;
    @Constraints.Required public int web_view_port;
    @Constraints.Required public int hardware_logger_port;
    @Constraints.Required public int rest_api_port;

    @Constraints.MinLength(value = 6, message = "The name must have at least 4 characters")
    @Constraints.MaxLength(value = 60, message = "The name must have at least 60 characters")
    public String server_url;

    @ApiModelProperty(value = "Optiona value - only for private server")
    public UUID project_id;
}
