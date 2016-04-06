package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for creating new Person",
          value = "Person_New")
public class Swagger_Person_New {

    @Constraints.Required
    @Constraints.MinLength(value = 8)
    @ApiModelProperty(required = true, value = "The nick_name must have at least 9 characters")
    public String nick_name;

    @Constraints.Email
    @Constraints.Required
    @ApiModelProperty(required = true, value = "Required valid mail ")
    public String mail;

    @ApiModelProperty(value = "The password must have at least 8 characters", required = true)
    @Constraints.MinLength(value = 8)
    @Constraints.Required
    public String password;

}
