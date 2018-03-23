package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.UUID;


@ApiModel(value = "C_Program_Update_program")
public class Swagger_C_Program_Update_program extends _Swagger_Abstract_Default {
    @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public UUID c_program_id;
    @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public UUID c_program_version_id;
    @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String c_program_program_name;
    @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String c_program_version_name;
}
