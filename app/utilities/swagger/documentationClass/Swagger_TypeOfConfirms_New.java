package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new TypeOfConfirms",
          value = "TypeOfConfirms_New")
public class Swagger_TypeOfConfirms_New {

    @Constraints.Required
    @Constraints.MinLength(value = 8)
    @ApiModelProperty(required = true, value = "MinLength >= 8")
    public String type;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String color;

    @Constraints.Required
    @Constraints.Min(value = 0)
    @ApiModelProperty(required = true, value = "must be positive")
    public Integer size;
}
