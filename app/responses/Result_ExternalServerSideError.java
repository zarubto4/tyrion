package responses;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="ExternalServerSideError", description="Unknown Error on external server ")
public class Result_ExternalServerSideError implements Response_Interface {

    public Result_ExternalServerSideError() {}

    public Result_ExternalServerSideError(String message) {
        this.message = message;
    }

    @ApiModelProperty(value = "state", allowableValues = "error", required = true, readOnly = true)
    public String state = "internal_server_error";

    @ApiModelProperty(value = "code", allowableValues = "478", required = true, readOnly = true)
    public Integer code = 478;

    @ApiModelProperty(required = false, readOnly = true)
    public String message = "Internal Server Error";
}
