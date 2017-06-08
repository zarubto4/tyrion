package utilities.response.response_objects;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Not Found", description="Requested entity was not found")
public class Result_NotFound {

    @ApiModelProperty(value = "state", required = true, readOnly = true)
    public String state = "not found";

    @ApiModelProperty(value = "code", allowableValues = "404", required = true, readOnly = true)
    public Integer code = 404;

    @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = false, readOnly = true)
    public String message;

}
