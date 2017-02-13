package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for update Person. (For password or email, you have to used separate API)",
        value = "Person_Update")
public class Swagger_Person_Update {


    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The nick_name must have at least 4 characters")
    @Constraints.MaxLength(value = 60, message = "The nick_name must not have more than 60 characters")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 60 characters.")
    public String nick_name;


    @Constraints.MinLength(value = 4, message = "The full_name must have at least 8 characters")
    @Constraints.MaxLength(value = 60, message = "The full_name must not have more than 60 characters")
    @ApiModelProperty(required = false, value = "Length must be between 8 and 60 characters.")
    public String full_name;

    @ApiModelProperty(required = false, value = "Where does the user come from.")
    public String country;

    @ApiModelProperty(required = false, value = "Gender of the user.", allowableValues = "male, female")
    public String gender;
}
