package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.UUID;

@ApiModel(description = "Json Model for register SIMCard Modul to project",
        value = "GSM_Register")
public class Swagger_GSM_Unregister {

    @Constraints.Required
    @ApiModelProperty(required = true) public UUID registration_hash;

}
