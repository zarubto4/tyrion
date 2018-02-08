package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.UUID;

@ApiModel(description = "Json Model for new HardwareType",
          value = "HardwareType_New")
public class Swagger_HardwareType_New extends Swagger_NameAndDescription {

    @Constraints.Required
    @Constraints.MinLength(value = 4)
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters.")
    @ApiModelProperty(required = true, value = "The description must have at least 4 characters")
    public String compiler_target_name;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Required valid producer_id")
    public UUID producer_id;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Required valid processor_id")
    public UUID processor_id;

    @Constraints.Required
    @ApiModelProperty(value = "If device can connect to internet", required = true)
    public boolean connectible_to_internet;

}
