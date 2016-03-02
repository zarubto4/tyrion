package utilities.response.response_objects;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;

@ApiModel(value="Json_Value_Missing_Result", description="Some Json value missing")
public class JsonValueMissing {

    @ApiModelProperty(value = "state", allowableValues = "error", required = true, readOnly = true)
    public String state;

    @ApiModelProperty(value = "code", allowableValues = "400", required = true, readOnly = true)
    public Integer code;

    @ApiModelProperty(value = "message", required = true, readOnly = true)
    public String message;

    @ApiModelProperty(required = true, readOnly = true)
    public ArrayList<String> required_jSON_parameter  = new ArrayList<>();
}
