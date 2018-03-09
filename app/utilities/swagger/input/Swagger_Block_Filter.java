package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.UUID;

@ApiModel(description = "Json Model for getting Block Filter List",
        value = "Block_Filter")
public class Swagger_Block_Filter {

    @ApiModelProperty(required = false, value = "Include only if you want to get Blocks of given project")
    public UUID project_id;

    @ApiModelProperty(required = false, value = "Only for Admins with permissions")
    public boolean pending_blocks;

    @ApiModelProperty(required = false, value = "Show - All Public Programs which are confirmed and approved.")
    public boolean public_programs;
}
