package utilities.response.response_objects;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Json_Value_Missing_Result", description="Some Json value missing - don't show that to users.. " +
        "SERVER IS LOGGING THIS FRONTEND ISSUE")
public class Result_JsonValueMissing {

    @ApiModelProperty(value = "state", required = true, readOnly = true)
    public String state = "Some Json value missing";

    @ApiModelProperty(value = "code", allowableValues = "400", required = true, readOnly = true)
    public Integer code = 400;

    @ApiModelProperty(value = "message", required = true, readOnly = true)
    public String message;

    @ApiModelProperty(required = true, readOnly = true)
    public JsonNode exception;
}
