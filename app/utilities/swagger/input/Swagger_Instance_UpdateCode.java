package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for updating blocko code.js in Instance",
        value = "Instance_UpdateCode")
public class Swagger_Instance_UpdateCode {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Required code in JSON.stringify")
    public String code;

}
