package utilities.swagger.outboundClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Board_Alert;
import utilities.enums.Enum_Online_status;

import java.util.ArrayList;
import java.util.List;


@ApiModel(description = "Json Model Board - only basic information",
          value = "Board_Update_Short_Detail")
public class Swagger_Board_Update_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true) public String board_id;
    @ApiModelProperty(required = true, readOnly = true) public Enum_Online_status online_state;
    @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String description;
    @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String name;
    @ApiModelProperty(required = true, readOnly = true) public String type_of_board_id;
    @ApiModelProperty(required = true, readOnly = true) public String type_of_board_name;

}
