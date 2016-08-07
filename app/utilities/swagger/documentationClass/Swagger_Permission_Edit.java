package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for change description for Permission",
             value = "Permission_Edit")
public class Swagger_Permission_Edit {

    @ApiModelProperty(required = false)
    public String description;

}
