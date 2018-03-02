package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.UUID;


@ApiModel(description = "Json Model for copy Library",
          value = "Library_Copy")
public class Swagger_Library_Copy extends Swagger_NameAndDesc_ProjectIdRequired{

    @Constraints.Required
    @ApiModelProperty(required = true)
    public UUID library_id;

}

