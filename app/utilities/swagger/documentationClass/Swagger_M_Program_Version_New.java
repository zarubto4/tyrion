package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

public class Swagger_M_Program_Version_New {


    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The name must have at least 4 characters")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    @ApiModelProperty(required = true, value = "Required valid screen_type_id")
    public String version_name;

    @ApiModelProperty(required = false, value = "program_description can be null or maximum length of 255 characters.")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    public String version_description;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String m_code;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String virtual_input_output;


    @ApiModelProperty(required = false, value = "if value is true - program can be open throw the QR token (public) by everyone!")
    public boolean public_mode;

}
