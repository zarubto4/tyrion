package utilities.response.response_objects;

import io.swagger.annotations.ApiModelProperty;

public class Result_Unauthorized {

    @ApiModelProperty(value = "state", allowableValues = "Unauthorized", required = true, readOnly = true)
    public String state = "Unauthorized";

    @ApiModelProperty(value = "code", allowableValues = "401", required = true, readOnly = true)
    public Integer code = 401;

    @ApiModelProperty(value = "Unauthorized access - please log in", required = false, readOnly = true)
    public String message = "Unauthorized access - please log in";

}
