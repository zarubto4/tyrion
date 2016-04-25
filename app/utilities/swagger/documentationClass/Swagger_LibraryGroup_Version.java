package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new Version for LibraryGroup",
        value = "LibraryGroup_Version")
public class Swagger_LibraryGroup_Version {


    @Constraints.Required
    @Constraints.MinLength(value = 2)
    @ApiModelProperty(required = true, value = "MinLength >= 2")
    public String version_name;

    @ApiModelProperty(required = false)
    public String version_description;
}
