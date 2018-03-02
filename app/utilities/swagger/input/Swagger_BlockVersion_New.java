package utilities.swagger.input;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for crating new version and also content of BlockVersion Model",
          value = "Swagger_BlockVersion_New")
public class Swagger_BlockVersion_New extends Swagger_NameAndDescription {

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String design_json;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String logic_json;
}
