package utilities.swagger.output;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.UUID;

@ApiModel(description = "Json Model for selecting Product for registration new project",
             value = "Applicable_Product")
public class Swagger_Product_Active {

    @ApiModelProperty(required = true, readOnly = true)
    public UUID id;

    @ApiModelProperty(required = true, readOnly = true)
    public String name;

    @ApiModelProperty(required = true, readOnly = true)
    public String tariff;
}
