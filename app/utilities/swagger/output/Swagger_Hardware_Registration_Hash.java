package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

@ApiModel(description = "Only For Admins",
        value = "Hardware_Registration_Hash")
public class Swagger_Hardware_Registration_Hash extends _Swagger_Abstract_Default {
    public String hash;
}
