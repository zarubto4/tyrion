package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_GridProgramVersion;

@ApiModel(description = "Json Model for Version of M_program",
          value = "M_Program_Version_Interface")
public class Swagger_M_Program_Version_Interface {

    @ApiModelProperty(required = true, readOnly = true)
    public Model_GridProgramVersion version;

    @ApiModelProperty(required = true, readOnly = true)
    public String virtual_input_output;
}
