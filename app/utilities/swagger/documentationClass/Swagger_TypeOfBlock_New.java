package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModelProperty;

public class Swagger_TypeOfBlock_New {

    @ApiModelProperty(required = true)  public String name;

    @ApiModelProperty(required = true)  public String general_description;

    @ApiModelProperty(required = true, value = "if you want make private TypeOfBlock group. You have to have \"project_id\" parameter in Json.  Value can be null or project_id in String", allowableValues = "null or project_id")
    public String project_id;
}
