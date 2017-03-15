package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(description = "Json Model Board - only basic information",
          value = "Board_Short_Detail")
public class Swagger_Board_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true)  public String id;
    @ApiModelProperty(required = true, readOnly = true)  public String personal_description;



    @ApiModelProperty(required = true, readOnly = true)  public boolean update_boot_loader_required;
    @ApiModelProperty(required = true, readOnly = true)  public boolean board_online_status;

    @ApiModelProperty(required = true, readOnly = true)  public String type_of_board_id;
    @ApiModelProperty(required = true, readOnly = true)  public String type_of_board_name;

    @ApiModelProperty(required = true, readOnly = true)  public boolean edit_permission;
    @ApiModelProperty(required = true, readOnly = true)  public boolean update_permission;
    @ApiModelProperty(required = true, readOnly = true)  public boolean delete_permission;
}
