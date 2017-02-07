package utilities.response.response_objects;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Internal Server Error", description="When server is in fault state")
public class Result_InternalServerError {

    @ApiModelProperty(value = "state", allowableValues = "error", required = true, readOnly = true)
    public String state = "internal_server_error";

    @ApiModelProperty(value = "code", allowableValues = "500", required = true, readOnly = true)
    public Integer code = 500;

    @ApiModelProperty(required = false, readOnly = true)
    public String message = "Internal Server Error";

}
