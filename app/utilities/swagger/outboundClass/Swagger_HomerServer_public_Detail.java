package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Cloud_HomerServer_type;
import utilities.enums.Enum_Online_status;

import java.util.UUID;

@ApiModel(description = "Json Model for Person_Short_Detail",
        value = "HomerServer_public_Detail")
public class Swagger_HomerServer_public_Detail {

    @ApiModelProperty(required = true, readOnly = true)
    public String id;

    @ApiModelProperty(required = true, readOnly = true)
    public String personal_server_name;

    @ApiModelProperty(required = true, readOnly = true)
    public Enum_Cloud_HomerServer_type server_type;

    @ApiModelProperty(required = true, readOnly = true)
    public Enum_Online_status online_state;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean update_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean edit_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean delete_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public Integer hardware_log_port;

    @ApiModelProperty(required = true, readOnly = true)
    public String server_url;

}