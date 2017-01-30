package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.enums.Library_state;
import utilities.enums.Library_tag;

@ApiModel(description = "Json Model for new ImportLibrary",
          value = "ImportLibrary_New")
public class Swagger_ImportLibrary_New {

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The name must have at least 4 characters, must be unique!")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters, must be unique!")
    @ApiModelProperty(required = true, value = "Length must be between 4 and 60 characters, must be unique!")
    public String name;

    @Constraints.Required
    @Constraints.MinLength(value = 8, message = "The description must have at least 8 characters")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 255 characters.")
    public String description;

    @Constraints.Required
    @Constraints.MinLength(value = 8, message = "The description must have at least 8 characters")
    @ApiModelProperty(required = true, value = "The description must have at least 8 characters")
    public String long_description;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "The tag describes what the library is doing")
    public Library_tag tag;

    @ApiModelProperty(required = false, value = "The state describes if the library is tested, new or old.")
    public Library_state state;
}
