package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for update Person. (For password or email, you have to used separate API)",
        value = "Person_Update")
public class Swagger_Person_Update {


    @ApiModelProperty(value = "min length = 4", required = true)
    @Constraints.MinLength(value = 4)
    @Constraints.Required
    public String nick_name;

    @ApiModelProperty(value = "min length = 4", required = true)
    @Constraints.MinLength(value = 8)
    @Constraints.Required
    public String full_name;

    @ApiModelProperty(required = false)
    public String first_title;

    @ApiModelProperty(required = false)
    public String last_title;

}
