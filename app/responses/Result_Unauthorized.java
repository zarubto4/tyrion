package responses;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Unauthorized")
public class Result_Unauthorized {

    @ApiModelProperty(value = "state", allowableValues = "unauthorized", required = true, readOnly = true)
    public String state = "unauthorized";

    @ApiModelProperty(value = "code", allowableValues = "401", required = true, readOnly = true)
    public Integer code = 401;

    @ApiModelProperty(value = "Unauthorized access - please log in", required = true, readOnly = true)
    public String message = "Unauthorized access - please log in";

}
