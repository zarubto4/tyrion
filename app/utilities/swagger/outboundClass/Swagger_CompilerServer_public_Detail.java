package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Cloud_HomerServer_type;
import utilities.enums.Enum_Online_status;

@ApiModel(description = "Json Model for Person_Short_Detail",
        value = "CompilerServer_public_Detail")
public class Swagger_CompilerServer_public_Detail {

    @ApiModelProperty(required = true, readOnly = true)
    public String id;

    @ApiModelProperty(required = true, readOnly = true)
    public String personal_server_name;

    @ApiModelProperty(required = true, readOnly = true)
    public Enum_Online_status online_state;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean update_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean edit_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean delete_permission;
}