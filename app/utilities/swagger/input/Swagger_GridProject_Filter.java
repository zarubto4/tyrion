package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Constraint;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for Filtering Grids Projects",
          value = "GridProject_Filter")
public class Swagger_GridProject_Filter extends _Swagger_filter_parameter{

    @ApiModelProperty(required = true)
    @Constraints.Required
    public UUID project_id;

}
