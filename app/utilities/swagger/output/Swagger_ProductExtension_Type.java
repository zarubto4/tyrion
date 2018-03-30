package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

@ApiModel(description = "Json Model for ProductExtension_Type",
        value = "ProductExtension_Type")
public class Swagger_ProductExtension_Type extends _Swagger_Abstract_Default {

    @ApiModelProperty(required = true, readOnly = true)
    public String type;

    @ApiModelProperty(required = true, readOnly = true)
    public String name;

    @ApiModelProperty(required = true, readOnly = true)
    public String description;

}