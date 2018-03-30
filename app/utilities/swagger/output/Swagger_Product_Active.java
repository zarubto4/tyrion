package utilities.swagger.output;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.UUID;

@ApiModel(description = "Json Model for selecting Product for registration new project",
             value = "Applicable_Product")
public class Swagger_Product_Active extends _Swagger_Abstract_Default {

    @ApiModelProperty(required = true, readOnly = true)
    public UUID id;

    @ApiModelProperty(required = true, readOnly = true)
    public String name;

    @ApiModelProperty(required = true, readOnly = true)
    public String tariff;
}
