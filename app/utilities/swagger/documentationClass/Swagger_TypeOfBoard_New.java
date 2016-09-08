package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new TypeOfBoard",
          value = "TypeOfBoard_New")
public class Swagger_TypeOfBoard_New {

    @Constraints.Required
    @Constraints.MinLength(value = 4,  message = "The name must have at least 4 characters.")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters.")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 60 characters.")
    public String name;

    @Constraints.Required
    @Constraints.MinLength(value = 4)
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters.")
    @ApiModelProperty(required = true, value = "The description must have at least 4 characters")
    public String compiler_target_name;

    @Constraints.Required
    @Constraints.MinLength(value = 4)
    @Constraints.MaxLength(value = 255, message = "The name must not have more than 255 characters.")
    @ApiModelProperty(required = true, value = "The description must have at least 4 characters")
    public String description;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Required valid producer_id")
    public String producer_id;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Required valid processor_id")
    public String processor_id;

    @Constraints.Required
    @ApiModelProperty(value = "If device can connect to internet", required = true)
    public boolean connectible_to_internet;

}
