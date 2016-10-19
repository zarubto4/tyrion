package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Version_Object;

@ApiModel(description = "Json Model for Version of M_program",
          value = "M_Program_Version_Interface")
public class Swagger_M_Program_Version_Interface {

    @ApiModelProperty(required = true, readOnly = true)
    public Version_Object version_object;

    @ApiModelProperty(required = true, readOnly = true)
    public String virtual_input_output;
}
