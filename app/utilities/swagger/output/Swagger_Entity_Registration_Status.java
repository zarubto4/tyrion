package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.BoardRegistrationStatus;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

@ApiModel(description = "Json Model for Status and all information about embedded Hardware, GSM Modul, etx..",
        value = "Entity_Registration_Status")
public class Swagger_Entity_Registration_Status extends _Swagger_Abstract_Default {

    @ApiModelProperty(value = "CAN_REGISTER, ALREADY_REGISTERED_IN_YOUR_ACCOUNT, ALREADY_REGISTERED, PERMANENTLY_DISABLED, BROKEN_DEVICE", example = "CAN_REGISTER", readOnly = true, required = true)
    public BoardRegistrationStatus status;

}
