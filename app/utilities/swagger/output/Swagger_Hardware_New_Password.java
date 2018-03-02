package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "",
        value = "Hardware_New_Password")
public class Swagger_Hardware_New_Password {
    @ApiModelProperty(required = true, readOnly = true)  @Constraints.Required  public String   mqtt_username;
    @ApiModelProperty(required = true, readOnly = true)  @Constraints.Required  public String   mqtt_password;
}


