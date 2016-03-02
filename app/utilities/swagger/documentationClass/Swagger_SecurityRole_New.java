package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModelProperty;

public class Swagger_SecurityRole_New {

    @ApiModelProperty(required = true)
    public String name;

    @ApiModelProperty(required = false, value = "But strongly recommended", example = "null")
    public String description;
}
