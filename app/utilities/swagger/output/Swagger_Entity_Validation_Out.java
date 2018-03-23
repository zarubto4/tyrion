package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

@ApiModel(description = "Json Model for Validation of *-some Entity",
         value = "Entity_Validation_Out")
public class Swagger_Entity_Validation_Out extends _Swagger_Abstract_Default {

    @ApiModelProperty(value = "Entity (Email, NickName.. etc) is valid if valid = true", required = true, readOnly = true)
    public boolean valid;

    @ApiModelProperty(value = "If valid = false, Json probably contains message for user", required = false, readOnly = true, example = "Email is used")
    public String message;

}
