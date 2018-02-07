package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json pattern for crating new object with name, description and required project id.",
        value = "NameAndDesc_ProjectIdRequired")
public class Swagger_NameAndDesc_ProjectIdRequired extends Swagger_NameAndDescription {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Provide project id if you want to create private object")
    public String project_id;
}
