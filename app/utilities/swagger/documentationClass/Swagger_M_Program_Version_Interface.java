package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Model_VersionObject;

@ApiModel(description = "Json Model for Version of M_program",
          value = "M_Program_Version_Interface")
public class Swagger_M_Program_Version_Interface {

    @ApiModelProperty(required = true, readOnly = true)
    public Model_VersionObject version_object;

    @ApiModelProperty(required = true, readOnly = true)
    public String virtual_input_output;
}
