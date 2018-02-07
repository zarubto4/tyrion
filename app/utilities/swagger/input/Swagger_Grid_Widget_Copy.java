package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for copy Grid_Widget",
          value = "Grid_Widget_Copy")
public class Swagger_Grid_Widget_Copy extends Swagger_NameAndDesc_ProjectIdRequired {

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String widget_id;
}

