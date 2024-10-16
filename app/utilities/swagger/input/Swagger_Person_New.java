package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for creating new Person",
          value = "Person_New")
public class Swagger_Person_New extends Swagger_EmailAndPassword {

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The nick_name must have at least 4 characters")
    @Constraints.MaxLength(value = 60, message = "The nick_name must not have more than 60 characters")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 60 characters.")
    public String nick_name;

    @Constraints.MaxLength(value = 60, message = "The full_name must not have more than 60 characters")
    @ApiModelProperty(required = true, value = "The full_name is not required. Max lenght is 60 characters.")
    public String first_name;

    @Constraints.MaxLength(value = 60, message = "The full_name must not have more than 60 characters")
    @ApiModelProperty(required = true, value = "The full_name is not required. Max lenght is 60 characters.")
    public String last_name;
}
