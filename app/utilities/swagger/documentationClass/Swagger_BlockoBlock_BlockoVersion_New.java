package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for crating new version and also content of BlockoBlock Model",
          value = "BlockoBlock_BlockoVersion_New")
public class Swagger_BlockoBlock_BlockoVersion_New {

    @Constraints.Required
    @Constraints.MinLength(value = 8, message = "The name must have at least 8 characters")
    @ApiModelProperty(required = true)
    public String version_name;

    @Constraints.Required
    @Constraints.MinLength(value = 24, message = "The name must have at least 24 characters")
    @ApiModelProperty(required = true)
    public String version_description;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String design_json;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String logic_json;
}
