package utilities.response.response_objects;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Permission Required", description="Some Json value missing - don't show that to users.. " +
          "SERVER IS LOGGING THIS FRONTEND ISSUE")
public class Result_PermissionRequired {

    @ApiModelProperty(value = "state", allowableValues = "Permission required", required = true, readOnly = true)
    public String state = "Permission required";

    @ApiModelProperty(value = "code", allowableValues = "403", required = true, readOnly = true)
    public Integer code = 403;

    @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = false, readOnly = true)
    public String message = "For this operation you have to be object owner, or you need special permission for that.";

}
