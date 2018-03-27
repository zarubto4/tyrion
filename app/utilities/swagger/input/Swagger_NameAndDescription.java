package utilities.swagger.input;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

@ApiModel(description = "Json Model with name and description",
          value = "NameAndDescription")
public class Swagger_NameAndDescription extends _Swagger_Abstract_Default {

    @Constraints.Required
    @Constraints.MinLength(value = 2, message = "The name must have at least 2 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    @ApiModelProperty(required = true, value = "Length must be between 2 and 60 characters.")
    public String name;

    @ApiModelProperty(required = false, value = "description can be null or maximum length of 255 characters.")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    public String description;
}
