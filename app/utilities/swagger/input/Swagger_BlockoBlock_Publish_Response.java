package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for crating new BlockoBlock Publish Response Model",
        value = "BlockoBlock_Publish_Response")
public class Swagger_BlockoBlock_Publish_Response {

    @ApiModelProperty(required = false, readOnly = true, value = "Required only if decision == true")
    public String blocko_block_type_of_block_id;

    @Constraints.Required
    public String version_id;

    @ApiModelProperty(required = true)
    public String version_name;

    @ApiModelProperty(required = true)
    public String version_description;

    @ApiModelProperty(required = true)
    public String program_name;

    @ApiModelProperty(required = true)
    public String program_description;

    @Constraints.Required
    public boolean decision;

    public String reason;

}
