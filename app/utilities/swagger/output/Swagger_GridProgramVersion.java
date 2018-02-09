package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Version;

@ApiModel(description = "Json Model for Version of Grid Program",
        value = "GridProgramVersion")
public class Swagger_GridProgramVersion {

    @ApiModelProperty(required = true, readOnly = true)
    public Model_Version version;

    @ApiModelProperty(required = true, readOnly = true)
    public String m_code;

    @ApiModelProperty(required = true, readOnly = true)
    public String virtual_input_output;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean public_mode;

}
