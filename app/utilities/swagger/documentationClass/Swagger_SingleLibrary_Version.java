package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new Version for SingleLibrary",
        value = "SingleLibrary_Version")
public class Swagger_SingleLibrary_Version {


    @Constraints.Required
    @Constraints.MinLength(value = 8)
    @ApiModelProperty(required = true, value = "MinLength >= 8")
    public String version_name;


    @Constraints.Required
    @Constraints.MinLength(value = 8)
    @ApiModelProperty(required = true, value = "MinLength >= 8")
    public String version_description;
}
