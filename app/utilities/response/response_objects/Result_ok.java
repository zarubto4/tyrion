package utilities.response.response_objects;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(value="Result ok", description="Some Json value missing - don't show that to users.. ")
public class Result_ok {


    @ApiModelProperty(value = "state", allowableValues = "Ok Status", required = true, readOnly = true)
    public String state = "ok";

    @ApiModelProperty(value = "code", allowableValues = "200", required = true, readOnly = true)
    public Integer code = 200;

    @ApiModelProperty(value = "Can be null! If not, you can show that to User. Server fills the message only when it is important.", required = false, readOnly = true)
    public String message = "";


}
