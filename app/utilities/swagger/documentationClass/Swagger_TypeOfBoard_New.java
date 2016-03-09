package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new TypeOfBoard",
          value = "TypeOfBoard_New")
public class Swagger_TypeOfBoard_New {

    @Constraints.Required @Constraints.MinLength(value = 8)  @ApiModelProperty(required = true, value = "MinLength >= 8")   public String name;
    @Constraints.Required @Constraints.MinLength(value = 24) @ApiModelProperty(required = true)       public String description;
    @Constraints.Required @ApiModelProperty(required = true, value = "Required valid producer_id")    public String producer_id;
    @Constraints.Required @ApiModelProperty(required = true, value = "Required valid processor_id")   public String processor_id;

}
