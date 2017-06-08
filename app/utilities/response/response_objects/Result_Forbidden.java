package utilities.response.response_objects;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Forbidden", description="Permission is needed for this action.")
public class Result_Forbidden {

    @ApiModelProperty(value = "state", required = true, readOnly = true)
    public String state = "forbidden";

    @ApiModelProperty(value = "code", allowableValues = "403", required = true, readOnly = true)
    public Integer code = 403;

    @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = false, readOnly = true)
    public String message = "For this operation you have to be object owner, or you need special permission for that.";

}
