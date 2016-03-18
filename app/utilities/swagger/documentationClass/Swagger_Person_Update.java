package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for update Person. (For password or email, you have to used separate API)",
        value = "Person_Update")
public class Swagger_Person_Update {

    @ApiModelProperty(value = "", required = true)
    @Constraints.Required
    public String nick_name;

    @ApiModelProperty(value = "", required = true)
    @Constraints.Required
    public String first_name;

    @ApiModelProperty(value = "", required = true)
    @Constraints.Required
    public String middle_name;

    @ApiModelProperty(value = "", required = true)
    @Constraints.Required
    public String last_name;

    @ApiModelProperty(value = "Time in millis", required = true)
    @Constraints.Required
    public String date_of_birth;


    @ApiModelProperty(required = false)
    public String first_title;

    @ApiModelProperty(required = false)
    public String last_title;

}
