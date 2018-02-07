package responses;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Result custom", description="Customizable response object")
public class Result_Custom {

    @ApiModelProperty(value = "state", required = true, readOnly = true)
    public String state = "error";

    @ApiModelProperty(value = "code", required = true, readOnly = true)
    public Integer code;

    @ApiModelProperty(value = "Can be null! If not, you can show that to User. Server fills the message only when it is important.", required = false, readOnly = true)
    public String message;


}
