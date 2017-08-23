package utilities.swagger.outboundClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Online_status;

@ApiModel(description = "Instance Light (only few properties)",
        value = "Instance_Short_Detail")
public class Swagger_Instance_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true) public String id;
    @ApiModelProperty(required = true, readOnly = true) public String name;
    @ApiModelProperty(required = true, readOnly = true) public String description;
    @ApiModelProperty(required = true, readOnly = true) public String b_program_id;
    @ApiModelProperty(required = true, readOnly = true) public String b_program_name;
    @ApiModelProperty(required = true, readOnly = true) public String b_program_description;

    @ApiModelProperty(required = true, readOnly = true) public String b_program_version_id;
    @ApiModelProperty(required = true, readOnly = true) public String b_program_version_name;



    @ApiModelProperty(required = true, readOnly = true) public String server_name;
    @ApiModelProperty(required = true, readOnly = true) public String server_id;
    @ApiModelProperty(required = true, readOnly = true) public Enum_Online_status server_online_state;
    @ApiModelProperty(required = true, readOnly = true) public Enum_Online_status instance_status;

    @ApiModelProperty(required = true, readOnly = true)  public boolean edit_permission;
    @ApiModelProperty(required = true, readOnly = true)  public boolean update_permission;

}
