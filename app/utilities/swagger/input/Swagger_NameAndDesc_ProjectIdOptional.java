package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for crating new Block Model",
        value = "NameAndDesc_ProjectIdOptional")
public class Swagger_NameAndDesc_ProjectIdOptional extends Swagger_NameAndDescription {

    @ApiModelProperty("Provide project id if you want to create private object")
    public String project_id;
}
