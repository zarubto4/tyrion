package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for getting Blocko_Block Filter List",
        value = "Blocko_Block_Filter")
public class Swagger_Blocko_Block_Filter {

    @ApiModelProperty(required = false, value = "Include only if you want to get Blocks of given project")
    public String project_id;

    @ApiModelProperty(required = false, value = "Only for Admins with permissions")
    public  boolean pending_blocks;
}
