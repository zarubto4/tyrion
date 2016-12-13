package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for new TypeOfWidget",
          value = "TypeOfWidget_New")
public class Swagger_TypeOfWidget_New {

    @Constraints.Required
    @Constraints.MinLength(value = 4,  message = "The name must have at least 4 characters.")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters.")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 60 characters.")
    public String name;

    @Constraints.MinLength(value = 0, message = "The description must have at least 0 characters")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    @ApiModelProperty(required = true, value = "Length must be between 24 and 255 characters.")
    public String description;

    @ApiModelProperty(required = false, value = "If you want make private TypeOfWidget group. You have to have \"project_id\" parameter in Json., Value can be null or contains project_id")
    public String project_id;
}
