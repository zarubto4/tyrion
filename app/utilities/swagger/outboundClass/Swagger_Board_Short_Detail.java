package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Board_Status;


@ApiModel(description = "Json Model Board - only basic information",
          value = "Board_Short_Detail")
public class Swagger_Board_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true)  public String id;
    @ApiModelProperty(required = true, readOnly = true)  public String personal_description;

    public Board_Status status = Board_Status.not_yet_first_connected;

    @ApiModelProperty(required = true, readOnly = true)  public String type_of_board_id;
    @ApiModelProperty(required = true, readOnly = true)  public String type_of_board_name;

    @ApiModelProperty(required = true, readOnly = true)  public boolean edit_permission;
    @ApiModelProperty(required = true, readOnly = true)  public boolean update_permission;
    @ApiModelProperty(required = true, readOnly = true)  public boolean delete_permission;
}
