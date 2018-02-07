package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for update M_Program",
          value = "M_Program_Update")
public class Swagger_M_Program_Update extends Swagger_NameAndDescription {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Required valid screen_type_id")
    public String  screen_size_type_id;

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
