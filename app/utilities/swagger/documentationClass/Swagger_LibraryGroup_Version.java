package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new Version for LibraryGroup",
        value = "LibraryGroup_Version")
public class Swagger_LibraryGroup_Version {


    @Constraints.Required
    @Constraints.MinLength(value = 2, message = "The name must have at least 2 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    @ApiModelProperty(required = true, value = "Length must be between 2 and 60 characters.")
    public String version_name;

    @ApiModelProperty(required = false, value = "version_description can be null or maximum length of 255 characters.")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    public String version_description;
}
