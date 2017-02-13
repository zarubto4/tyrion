package utilities.response.response_objects;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Bad Request", description="When is something wrong")
public class Result_BadRequest {

    @ApiModelProperty(value = "state", allowableValues = "error", required = true, readOnly = true)
    public String state = "error";

    @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = false, readOnly = true)
    public String message;

}
