package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new LibraryGroup",
        value = "LibraryGroup_New")
public class Swagger_LibraryGroup_New {

    @Constraints.Required
    @Constraints.MinLength(value = 24, message = "The description must have at least 24 characters")
    @ApiModelProperty(required = true, value = "The description must have at least 24 characters")
    public String description;



    @Constraints.Required
    @Constraints.MinLength(value = 8, message = "The name must have at least 8 characters")
    @ApiModelProperty(required = true, value = "The name must have at least 8 characters")
    public String group_name;
}
