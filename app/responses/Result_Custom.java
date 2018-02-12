package responses;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Result custom", description="Customizable response object")
public class Result_Custom extends Response_Interface {

    @ApiModelProperty(value = "state", required = true, readOnly = true)
    public String state = "error";


}
