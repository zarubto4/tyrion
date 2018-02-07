package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for ProductExtension_Type",
        value = "ProductExtension_Type")
public class Swagger_ProductExtension_Type {

    @ApiModelProperty(required = true, readOnly = true)
    public String type;

    @ApiModelProperty(required = true, readOnly = true)
    public String name;

    @ApiModelProperty(required = true, readOnly = true)
    public String description;

}