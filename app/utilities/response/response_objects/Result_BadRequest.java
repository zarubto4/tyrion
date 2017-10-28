package utilities.response.response_objects;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Result_BadRequest", description="When is something wrong")
public class Result_BadRequest implements Response_Interface{

    @ApiModelProperty(value = "state", allowableValues = "error", required = true, readOnly = true)
    public String state = "error";

    @ApiModelProperty(value = "code", allowableValues = "400", required = true, readOnly = true)
    public Integer code = 400;

    @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = false, readOnly = true)
    public String message;

}
