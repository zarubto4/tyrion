package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new Version of M_program",
        value = "M_Program_Version_New")
public class Swagger_M_Program_Version_New extends Swagger_NameAndDescription {

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String m_code;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String virtual_input_output;

    @ApiModelProperty(required = false, value = "if value is true - program can be open throw the QR token (public) by everyone!")
    public boolean public_mode;
}
