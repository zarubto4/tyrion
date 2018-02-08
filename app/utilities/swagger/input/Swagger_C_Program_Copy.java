package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.UUID;


@ApiModel(description = "Json Model for copy C_Program",
          value = "C_Program_Copy")
public class Swagger_C_Program_Copy extends Swagger_NameAndDesc_ProjectIdRequired {

    @Constraints.Required
    @ApiModelProperty(required = true)
    public UUID c_program_id;

}

