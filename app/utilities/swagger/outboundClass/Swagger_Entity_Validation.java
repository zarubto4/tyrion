package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for Validation of *-some Entity",
         value = "Entity_Validation")
public class Swagger_Entity_Validation {

    @ApiModelProperty(value = "Entity (Email, NickName.. etc) is valid if valid = true", required = true, readOnly = true)
    public boolean valid;

    @ApiModelProperty(value = "If valid = false, Json probably contains message for user", required = false, readOnly = true, example = "Email is used")
    public String message;

}
