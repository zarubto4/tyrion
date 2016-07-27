package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Blocko_Block Light (only few properties)",
        value = "Blocko_Block_Light")
public class Swagger_Blocko_Block_Light {

    @ApiModelProperty(required = true, readOnly = true)
    public String blocko_block_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String blocko_block_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String blocko_block_version_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String blocko_block_version_name;
}
