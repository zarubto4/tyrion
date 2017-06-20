package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Board_Alert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@ApiModel(description = "Json Model Board - only basic information",
          value = "Board_Short_Detail")
public class Swagger_Board_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true)  public String id;
    @ApiModelProperty(required = true, readOnly = true)  public String personal_description;


    @ApiModelProperty(required = true, readOnly = true)  public List<Enum_Board_Alert> alert_list = new ArrayList<>();
    @ApiModelProperty(required = true, readOnly = true)  public boolean board_online_status;
    @ApiModelProperty(required = true, readOnly = true)  public Date last_online;

    @ApiModelProperty(required = true, readOnly = true)  public String type_of_board_id;
    @ApiModelProperty(required = true, readOnly = true)  public String type_of_board_name;

    @ApiModelProperty(required = true, readOnly = true)  public boolean edit_permission;
    @ApiModelProperty(required = true, readOnly = true)  public boolean update_permission;
    @ApiModelProperty(required = true, readOnly = true)  public boolean delete_permission;
}
