package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for copy Library",
          value = "Library_Copy")
public class Swagger_Library_Copy extends Swagger_NameAndDesc_ProjectIdRequired{

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String library_id;

}

