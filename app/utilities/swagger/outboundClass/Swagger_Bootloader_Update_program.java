package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Bootloader_Update_program")
public class Swagger_Bootloader_Update_program {
    @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String bootloader_id;
    @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String bootloader_name;
    @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String version_identificator;
}
