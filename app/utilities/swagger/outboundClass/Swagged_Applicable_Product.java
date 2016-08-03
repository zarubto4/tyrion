package utilities.swagger.outboundClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for selecting Product for registration new project",
             value = "Applicable_Product")
public class Swagged_Applicable_Product {

    @ApiModelProperty(required = true, readOnly = true)
    public Long product_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String product_individual_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String product_type;
}
