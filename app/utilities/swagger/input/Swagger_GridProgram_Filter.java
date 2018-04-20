package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for Filtering Grids Programs App",
          value = "GridProgram_Filter")
public class Swagger_GridProgram_Filter extends _Swagger_filter_parameter{

    @ApiModelProperty(required = true)
    @Constraints.Required
    public String project_id;

}
