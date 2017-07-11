package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for Person_Short_Detail",
        value = "HomerServer_public_Detail")
public class Swagger_HomerServer_public_Detail {

    @ApiModelProperty(required = true, readOnly = true)
    public String unique_identificator;

    @ApiModelProperty(required = true, readOnly = true)
    public String personal_server_name;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean online_state;

}