package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Board_update_collision;

@ApiModel(description = "Json Model for board with details for fast upload",
        value = "Board_for_fast_upload_detail")
public class Swagger_Board_for_fast_upload_detail {

    @ApiModelProperty(required = true, readOnly = true)  public String id;
    @ApiModelProperty(required = true, readOnly = true)  public String personal_description;

    @ApiModelProperty(required = true, readOnly = true)  public Board_update_collision collision;

    @ApiModelProperty(required = true, readOnly = true)  public String type_of_board_id;
    @ApiModelProperty(required = true, readOnly = true)  public String type_of_board_name;
}
