package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.Date;

@ApiModel(description = "Json Model for update Person. (For password or email, you have to used separate API)",
        value = "Person_Update")
public class Swagger_Person_Update {

    @ApiModelProperty(value = "", required = true)
    @Constraints.Required
    public String nick_name;

    @ApiModelProperty(value = "", required = true)
    @Constraints.Required
    public String full_name;

    @ApiModelProperty(value = "Time in millis", required = true)
    @Constraints.Required
    public Date date_of_birth;

    @ApiModelProperty(required = false)
    public String first_title;

    @ApiModelProperty(required = false)
    public String last_title;

}
