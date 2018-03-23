package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

@ApiModel(description = "",
        value = "Hardware_New_Password")
public class Swagger_Hardware_New_Password extends _Swagger_Abstract_Default {
    @ApiModelProperty(required = true, readOnly = true)  @Constraints.Required  public String   mqtt_username;
    @ApiModelProperty(required = true, readOnly = true)  @Constraints.Required  public String   mqtt_password;
}


