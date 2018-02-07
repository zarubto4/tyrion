package responses;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Internal Server Error", description="When server is in fault state")
public class Result_InternalServerError implements Response_Interface {

    public Result_InternalServerError() {}

    public Result_InternalServerError(String message) {
        this.message = message;
    }

    @ApiModelProperty(value = "state", allowableValues = "error", required = true, readOnly = true)
    public String state = "internal server error";

    @ApiModelProperty(value = "code", allowableValues = "500", required = true, readOnly = true)
    public Integer code = 500;

    @ApiModelProperty(required = false, readOnly = true)
    public String message = "Server encountered fatal error";
}
