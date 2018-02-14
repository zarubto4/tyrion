package responses;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Server Offline", description="External servers (compilation, blocko cloud_blocko_server etc.) are offline")
public class Result_ServerOffline extends _Response_Interface {

    public Result_ServerOffline(String message) {
        this.message = message;
    }

    @ApiModelProperty(value = "state", allowableValues = "server_is_offline", required = true, readOnly = true)
    public String state() {
        return "server_is_offline";
    }

    @ApiModelProperty(value = "code", allowableValues = "403", required = true, readOnly = true)
    public Integer code() {
        return 477;
    }

    @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = false, readOnly = true)
    public String message() {
        if(message != null) return message;
        return "FServer is offline, operation is not supported now";
    }

}
