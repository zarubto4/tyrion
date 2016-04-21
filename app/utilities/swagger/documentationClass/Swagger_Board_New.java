package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for create new Board",
          value = "Board_New")
public class Swagger_Board_New {

        @Constraints.Required
        @ApiModelProperty(value = "Required valid type_of_post_id", required = true)
        public String type_of_board_id;

        @Constraints.Required
        @Constraints.MinLength(value = 8, message = "The hardware_id must have at least 8 characters")
        @ApiModelProperty(value = "Must be unique!!!, The hardware_id must have at least 8 characters", required = true)
        public String hardware_unique_id;
}
