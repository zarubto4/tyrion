package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(value = "C_Program_Update_program")
public class Swagger_C_Program_Update_program {
    @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String c_program_id;
    @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String c_program_version_id;
    @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String c_program_program_name;
    @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String c_program_version_name;
}
