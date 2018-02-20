package responses;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Result_BadRequest", description="When is something wrong")
public class Result_BadRequest extends _Response_Interface {

    public Result_BadRequest() {}

    public Result_BadRequest(String message) {
        this.message = message;
    }

    @ApiModelProperty(value = "state", allowableValues = "error", required = true, readOnly = true)
    public String state() {
        return "error";
    }

    @ApiModelProperty(value = "code", allowableValues = "400", required = true, readOnly = true)
    public int code() {
        return 400;
    }

    @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = false, readOnly = true)
    public String message() {
        return this.message;
    }

}
