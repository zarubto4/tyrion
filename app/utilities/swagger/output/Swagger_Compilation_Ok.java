package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;


@ApiModel(description = "Json Model for Compilation Result",
        value = "Compilation_Ok")
public class Swagger_Compilation_Ok extends _Swagger_Abstract_Default {

    @ApiModelProperty(value = "Value is success", required = true, readOnly = true)
    public String state = "success";
}
