package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new SecurityRole (Group)",
          value = "SecurityRole_New")
public class Swagger_SecurityRole_New {

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String name;


    @ApiModelProperty(required = false, value = "Not required, But strongly recommended")
    public String description;
}
