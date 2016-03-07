package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for new Single Library",
        value = "ShareProject_Person ")
public class Swagger_SingleLibrary_New {

    @Constraints.Required
    @Constraints.MinLength(value = 8)
    @ApiModelProperty(required = true, value = "MinLength >= 8")
    public String library_name;

    @Constraints.Required
    @Constraints.MinLength(value = 8)
    @ApiModelProperty(required = false, value = "MinLength >= 8")
    public String description;
}
