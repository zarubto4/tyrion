package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(value = "ServerUpdate", description = "Json Model for scheduling server update.")
public class Swagger_ServerUpdate {

    @Constraints.Required
    public String version;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "UNIX time in millis", example = "1466163478925", dataType = "integer")
    public Long update_time;
}
