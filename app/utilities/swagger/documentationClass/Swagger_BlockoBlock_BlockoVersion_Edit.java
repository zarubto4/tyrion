package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for editing BlockoVersion",
          value = "BlockoBlock_BlockoVersion_Edit")
public class Swagger_BlockoBlock_BlockoVersion_Edit {

    @Constraints.Required
    @Constraints.MinLength(value = 8, message = "The name must have at least 8 characters")
    @ApiModelProperty(required = true)
    public String version_name;

    @ApiModelProperty(required = false)
    public String version_description;
}
