package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new TypeOfBoard",
          value = "TypeOfBoard_New")
public class Swagger_TypeOfBoard_New {

    @Constraints.Required @Constraints.MinLength(value = 8)  @ApiModelProperty(required = true, value = "The name must have at least 8 characters")          public String name;
    @Constraints.Required @Constraints.MinLength(value = 10) @ApiModelProperty(required = true, value = "The description must have at least 10 characters")  public String description;
    @Constraints.Required @ApiModelProperty(required = true, value = "Required valid producer_id")    public String producer_id;
    @Constraints.Required @ApiModelProperty(required = true, value = "Required valid processor_id")   public String processor_id;

    @Constraints.Required @ApiModelProperty(value = "If device can connect to internet", required = true) public Boolean connectible_to_internet;

}
