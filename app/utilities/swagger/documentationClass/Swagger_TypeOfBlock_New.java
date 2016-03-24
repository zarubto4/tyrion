package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for new TypeOfBlock",
          value = "TypeOfBlock_New")
public class Swagger_TypeOfBlock_New {

    @Constraints.Required
    @Constraints.MinLength(value = 8)
    @ApiModelProperty(required = false, value = "MinLength >= 8")
    public String name;

    @Constraints.Required
    @Constraints.MinLength(value = 24)
    @ApiModelProperty(required = false, value = "The description must have at least 24 characters")
    public String general_description;

    @ApiModelProperty(required = false, value = "\n \n if you want make private TypeOfBlock group. You have to have \"project_id\" parameter in Json.",
                      allowableValues = "Value can be null or contains project_id")
    public String project_id;
}
