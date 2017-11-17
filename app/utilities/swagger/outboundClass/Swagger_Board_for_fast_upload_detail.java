package utilities.swagger.outboundClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Board_update_collision;

@ApiModel(description = "Json Model for board with details for fast upload",
        value = "Board_for_fast_upload_detail")
public class Swagger_Board_for_fast_upload_detail {

    @ApiModelProperty(required = true, readOnly = true)  public String id;
    @ApiModelProperty(required = true, readOnly = true)  public String name;
    @ApiModelProperty(required = true, readOnly = true)  public String description;

    @ApiModelProperty(required = true, readOnly = true)  public Enum_Board_update_collision collision;

    @ApiModelProperty(required = true, readOnly = true)  public String type_of_board_id;
    @ApiModelProperty(required = true, readOnly = true)  public String type_of_board_name;
}
