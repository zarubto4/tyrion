package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for new TypeOfBlock",
          value = "TypeOfBlock_New")
public class Swagger_TypeOfBlock_New {

    @Constraints.Required
    @Constraints.MinLength(value = 8,  message = "The name must have at least 8 characters.")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters.")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 60 characters.")
    public String name;

    @Constraints.Required
    @Constraints.MinLength(value = 24, message = "The description must have at least 24 characters")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    @ApiModelProperty(required = true, value = "Length must be between 24 and 255 characters.")
    public String general_description;

    @ApiModelProperty(required = false, value = "\n \n if you want make private TypeOfBlock group. You have to have \"project_id\" parameter in Json.",
                      allowableValues = "Value can be null or contains project_id")
    public String project_id;
}
