package responses;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Result_Not_Validated", description="When you call unsupported functions")
public class Result_UnsupportedException extends _Response_Interface {

    public Result_UnsupportedException() {}

    public Result_UnsupportedException(String message) {
        this.message = message;
    }

    @ApiModelProperty(value = "state", allowableValues = "unsupported_exception", required = true, readOnly = true)
    public String state() {
        return "unsupported_exception";
    }

    @ApiModelProperty(value = "code", allowableValues = "400", required = true, readOnly = true)
    public int code() {
        return 400;
    }

    @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = false, readOnly = true)
    public String message() {
        if(message != null) return message;
        return "Please contact technical support. Your request required unsupported parts of system";
    }

}
