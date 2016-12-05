package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for crating new GridWidget Model",
        value = "GridWidget_New")
public class Swagger_GridWidget_New {

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The name must have at least 4 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 60 characters.")
    public String name;

    @Constraints.Required
    @Constraints.MinLength(value = 0, message = "The description must have at least 0 characters")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    @ApiModelProperty(required = true, value = "Length must be between 24 and 255 characters.")
    public String general_description;


    @Constraints.Required
    @ApiModelProperty(value = "Required valid type_of_widget id", required = true)
    public String type_of_widget_id;

}
