package utilities.response.response_objects;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Object not found", description="Called object is missing. You can show that to Person")
public class Result_NotFound {

    @ApiModelProperty(value = "state", allowableValues = "Object not Found", required = true, readOnly = true)
    public String state = "Object not Found";

    @ApiModelProperty(value = "code", allowableValues = "400", required = true, readOnly = true)
    public Integer code = 400;

    @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = true, readOnly = true)
    public String message;

}
