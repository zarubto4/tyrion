package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.enums.Enum_Library_state;

import java.util.List;

@ApiModel(value = "Library_New", description = "Json Model for new Library")
public class Swagger_Library_New {


    @ApiModelProperty(required = true, value = "Project ID only for private libraries. For Public, permission is required. Its Required only for Creating, For update is used previous settings. ")
    public String project_id = null;

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

}
