package responses;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Server Offline", description="External servers (compilation, blocko cloud_blocko_server etc.) are offline")
public class Result_ServerOffline implements Response_Interface {

    public Result_ServerOffline(String message) {
        this.message = message;
    }

    @ApiModelProperty(value = "state", required = true, readOnly = true)
    public String state = "server offline";

    @ApiModelProperty(value = "code", allowableValues = "400", required = true, readOnly = true)
    public Integer code = 477;

    @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = true, readOnly = true)
    public String message;
}
