package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for resending authentication email.",
          value = "Person_Authentication")
public class Swagger_Person_Authentication {

    @Constraints.Email
    @Constraints.Required
    @ApiModelProperty(required = true, value = "Required valid mail ")
    public String mail;

}
