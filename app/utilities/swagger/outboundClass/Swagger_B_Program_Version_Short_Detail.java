package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Approval_state;

@ApiModel(description = "B_Program Version Light (only few properties)",
          value = "B_Program_Version_Short_Detail")
public class Swagger_B_Program_Version_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true)
    public String version_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String version_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String version_description;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean update_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean delete_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public Swagger_Person_Short_Detail author;


    @ApiModelProperty(required = true, readOnly = true)
    public Enum_Approval_state publish_status;

    @ApiModelProperty(required = false, readOnly = true)
    public boolean community_publishing_permission;
}
