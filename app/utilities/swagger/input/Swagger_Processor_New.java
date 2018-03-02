package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for new Processor",
          value = "Processor_New")
public class Swagger_Processor_New extends Swagger_NameAndDescription {

    @Constraints.Required
    @Constraints.MinLength(value = 4)
    @ApiModelProperty(required = true, value = "The Processor Code must have at least 4 characters")
    public String  processor_code;

    @Constraints.Required
    @Constraints.Min(value = 0)
    @ApiModelProperty(required = true) public Integer speed;
}
