package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.UUID;

@ApiModel(value = "Bootloader_Update_program")
public class Swagger_Bootloader_Update_program extends _Swagger_Abstract_Default {
    @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public UUID bootloader_id;
    @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String bootloader_name;
    @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String version_identificator;
    @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public UUID hardware_type_id;
    @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String hardware_type_name;
}
