package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for crating new Block Model",
        value = "NameAndDesc_ProjectIdOptional")
public class Swagger_NameAndDesc_ProjectIdOptional extends Swagger_NameAndDescription {

    @ApiModelProperty("Provide project id if you want to create private object")
    public UUID project_id;

    @ApiModelProperty(value = "Tags - Optional", required = false)
    public List<String> tags = new ArrayList<>();
}
