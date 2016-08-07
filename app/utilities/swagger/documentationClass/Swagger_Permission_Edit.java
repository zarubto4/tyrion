package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for change description for Permission",
             value = "Permission_Edit")
public class Swagger_Permission_Edit {

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String name;

    @ApiModelProperty(required = false)
    public String description;

}
