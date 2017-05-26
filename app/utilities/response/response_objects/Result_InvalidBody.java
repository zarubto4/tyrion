package utilities.response.response_objects;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Invalid Body", description="Provided body is not valid. Something is missing or some field input is not allowed.")
public class Result_InvalidBody {

    @ApiModelProperty(value = "state", required = true, readOnly = true)
    public String state = "invalid body";

    @ApiModelProperty(value = "code", allowableValues = "400", required = true, readOnly = true)
    public Integer code = 400;

    @ApiModelProperty(value = "message", required = true, readOnly = true)
    public String message;

    @ApiModelProperty(required = true, readOnly = true)
    public JsonNode exception;
}
