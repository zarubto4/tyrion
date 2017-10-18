package utilities.response.response_objects;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Not Validated", description="When account is not validated.")
public class Result_NotValidated {

    @ApiModelProperty(value = "state", allowableValues = "error", required = true, readOnly = true)
    public String state = "error";

    @ApiModelProperty(value = "code", allowableValues = "400", required = true, readOnly = true)
    public Integer code = 705;

    @ApiModelProperty(value = "message", required = true, readOnly = true)
    public String message = "Your account is not validated.";

}
