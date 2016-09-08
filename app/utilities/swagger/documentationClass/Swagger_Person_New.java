package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for creating new Person",
          value = "Person_New")
public class Swagger_Person_New {

    @Constraints.Required
    @Constraints.MinLength(value = 8, message = "The nick_name must have at least 8 characters")
    @Constraints.MaxLength(value = 60, message = "The nick_name must not have more than 60 characters")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 60 characters.")
    public String nick_name;

    @Constraints.Email
    @Constraints.Required
    @ApiModelProperty(required = true, value = "Required valid mail ")
    public String mail;

    @ApiModelProperty(value = "The password length must be between 8 and 60 characters", required = true)
    @Constraints.MinLength(value = 8, message = "The password must have at least 8 characters")
    @Constraints.MaxLength(value = 60, message = "The password must not have more than 60 characters")
    @Constraints.Required
    public String password;
}
