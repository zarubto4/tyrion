package responses;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Result_Forbidden", description="Permission is needed for this action.")
public class Result_Forbidden  extends _Response_Interface {

    public Result_Forbidden() {}

    public Result_Forbidden(String message) {
        this.message = message;
    }


    @ApiModelProperty(value = "state", allowableValues = "forbidden", required = true, readOnly = true)
    public String state() {
        return "forbidden";
    }

    @ApiModelProperty(value = "code", allowableValues = "403", required = true, readOnly = true)
    public Integer code() {
        return 403;
    }

    @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = false, readOnly = true)
    public String message() {
        if(message != null) return message;
        return "For this operation you have to be object owner, or you need special permission for that.";
    }

}
