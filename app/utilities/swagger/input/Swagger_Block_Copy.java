package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for copy Block",
          value = "Blocko_Block_Copy")
public class Swagger_Block_Copy extends Swagger_NameAndDesc_ProjectIdRequired {

    @Constraints.Required
    public String block_id;
}

