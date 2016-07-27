package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "B_Program Light (only few properties)",
        value = "B_Program_Light")
public class Swagger_B_Program_Light {

    @ApiModelProperty(required = true, readOnly = true)
    public String b_program_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String b_program_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String b_program_version_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String b_program_version_name;
}
