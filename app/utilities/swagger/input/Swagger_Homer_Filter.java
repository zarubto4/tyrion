package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(description = "Json Model representing Homer filter",
        value = "Homer_Filter")
public class Swagger_Homer_Filter {

   @ApiModelProperty(value = "Required valid project_id", required = false)
   public List<String > project_ids;

}
