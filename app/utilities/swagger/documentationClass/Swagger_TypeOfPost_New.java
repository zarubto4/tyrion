package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for create new TypeOfPost",
        value = "TypeOfPost_New")
public class Swagger_TypeOfPost_New {

    @Constraints.Required
    @Constraints.MinLength(value = 3)
    @ApiModelProperty(required = true, value = "MinLength >= 3")
    public String type;
}
