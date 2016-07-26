package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "C_Program Light (only few properties)",
        value = "C_Program_Light")
public class Swagger_C_Program_Light {

    @ApiModelProperty(required = true, readOnly = true)
    public String c_program_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String c_program_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String c_program_version_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String c_program_version_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String type_of_board_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String type_of_board_name;
}
