package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_GridProgramVersion;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

@ApiModel(description = "Json Model for Version of M_program",
          value = "M_Program_Version_Interface")
public class Swagger_M_Program_Version_Interface extends _Swagger_Abstract_Default {

    @ApiModelProperty(required = true, readOnly = true)
    public Model_GridProgramVersion version;

    @ApiModelProperty(required = true, readOnly = true)
    public String virtual_input_output;
}
