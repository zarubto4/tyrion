package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for Filtering Grids Apps",
          value = "Grid_Filter")
public class Swagger_Grid_Filter {

    @ApiModelProperty(value = "List of project.id", required = false)
    public List<UUID> project_ids;

}
