package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for Status and all information about embedded Hardware",
        value = "Board_Registration_Status")
public class Swagger_Board_Registration_Status {

    @ApiModelProperty(value = "CAN_REGISTER, ALREADY_REGISTERED_YOUR_ACC, ALREADY_REGISTERED, PERMANENTLY_DISABLED, BROKEN_DEVICE", example = "CAN_REGISTER", readOnly = true, required = true)
    public String status;

}
