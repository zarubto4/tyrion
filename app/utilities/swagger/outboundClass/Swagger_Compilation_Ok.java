package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(description = "Json Model for Compilation Result",
        value = "Compilation_Ok")
public class Swagger_Compilation_Ok{

    @ApiModelProperty(value = "Value is success", required = true, readOnly = true)
    public String state = "success";
}
