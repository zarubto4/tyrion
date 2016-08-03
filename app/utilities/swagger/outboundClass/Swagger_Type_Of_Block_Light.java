package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Type_Of_Block Light (only few properties)",
        value = "Type_Of_Block_Light")
public class Swagger_Type_Of_Block_Light {

    @ApiModelProperty(required = true, readOnly = true)
    public String type_of_block_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String type_of_block_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String type_of_block_description;
}
