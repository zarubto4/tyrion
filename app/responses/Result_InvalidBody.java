package responses;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Invalid Body", description="Provided body is not valid. Something is missing or some field input is not allowed.")
public class Result_InvalidBody {

    public Result_InvalidBody(JsonNode errors) {
        this.exception = errors;
    }

    @ApiModelProperty(value = "state", required = true, readOnly = true)
    public String state = "invalid body";

    @ApiModelProperty(value = "code", allowableValues = "400", required = true, readOnly = true)
    public Integer code = 400;

    @ApiModelProperty(value = "message", required = true, readOnly = true)
    public String message = "Provided body is invalid. If it is possible, the reason will be returned in exception field.";

    @ApiModelProperty(required = true, readOnly = true)
    public JsonNode exception;
}
