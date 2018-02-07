package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Version;

@ApiModel(description = "Json Model for Version of M_program",
        value = "M_Program_Version")
public class Swagger_M_Program_Version {

    @ApiModelProperty(required = true, readOnly = true)
    public Model_Version version_object;

    @ApiModelProperty(required = true, readOnly = true)
    public String m_code;

    @ApiModelProperty(required = true, readOnly = true)
    public String virtual_input_output;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean public_mode;

}
