package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

@ApiModel(description = "Json Model with settings and firwmare and bootloader for Embedded hardware",
        value = "Hardware_New_Settings_Result")
public class Swagger_Hardware_New_Settings_Result extends _Swagger_Abstract_Default {

    @ApiModelProperty(required = false, readOnly = true) public String   full_id;  // [číslo procesoru - přiloží se jen když ho zašle request (oprava vypálení)

    @ApiModelProperty(required = true, readOnly = true) public Swagger_Hardware_New_Settings_Result_Configuration configuration;
}


