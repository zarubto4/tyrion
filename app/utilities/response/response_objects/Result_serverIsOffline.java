package utilities.response.response_objects;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="External cloud_blocko_server is offline", description="External servers (compilation, blocko cloud_blocko_server etc.) are offline")
public class Result_serverIsOffline {

    @ApiModelProperty(value = "state", required = true, readOnly = true)
    public String state = "External Server is offline";

    @ApiModelProperty(value = "code", allowableValues = "400", required = true, readOnly = true)
    public Integer code = 400;

    @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = true, readOnly = true)
    public String message;
}
