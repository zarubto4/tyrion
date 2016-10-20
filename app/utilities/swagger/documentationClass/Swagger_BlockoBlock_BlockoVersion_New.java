package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for crating new version and also content of BlockoBlock Model",
          value = "BlockoBlock_BlockoVersion_New")
public class Swagger_BlockoBlock_BlockoVersion_New {

    @Constraints.Required
    @Constraints.MinLength(value = 2, message = "The name must have at least 2 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    @ApiModelProperty(required = true, value = "Length must be between 2 and 60 characters.")
    public String version_name;

    @Constraints.Required
    @Constraints.MinLength(value = 0)
    @Constraints.MaxLength(value = 255)
    @ApiModelProperty(required = false)
    public String version_description;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String design_json;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String logic_json;
}
