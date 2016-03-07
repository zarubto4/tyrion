package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new M_Program",
        value = "M_Program_New")
public class Swagger_M_Program_New {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Required valid m_program_id")
    public String  m_program_id;


    @Constraints.Required
    @ApiModelProperty(required = true, value = "Required valid screen_type_id")
    public String  screen_type_id;


    @ApiModelProperty(required = false, value = "Can be null")
    public String  program_description;


    @Constraints.Required
    @Constraints.MinLength(value = 8, message = "The description must have at least 8 characters")
    @ApiModelProperty(required = true, value = "The description must have at least 8 characters")
    public String  program_name;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Code in converted to String")
    public String  m_code;


    @Constraints.Required
    @ApiModelProperty(required = true, value = "If true - its possible turn on that on height")
    public boolean  height_lock;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "If true - its possible turn on that on width")
    public boolean  width_lock;

}
