package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.UUID;

@ApiModel(description = "Blocko_Block Light (only few properties)",
        value = "Blocko_Block_Filter_Detail")
public class Swagger_Blocko_Block_Filter_Detail { // TODO whole class sucks

    @ApiModelProperty(required = true, readOnly = true)
    public UUID blocko_block_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String blocko_block_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String blocko_block_description;

    @ApiModelProperty(required = true, readOnly = true)
    public UUID blocko_block_version_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String blocko_block_version_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String blocko_block_version_description;

    @ApiModelProperty(required = true, readOnly = true)
    public String blocko_block_type_of_block_name;

    @ApiModelProperty(required = true, readOnly = true)
    public UUID blocko_block_type_of_block_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String blocko_block_type_of_block_description;
}
