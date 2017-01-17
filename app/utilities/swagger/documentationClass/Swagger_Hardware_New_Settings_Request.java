package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for data for new Embedded Hardware",
        value = "Hardware_New_Settings_Request")
public class Swagger_Hardware_New_Settings_Request {


    @ApiModelProperty(required = true, readOnly = true, value = "Value must be unique! Required 10 min chars!")
    @Constraints.Required
    @Constraints.MinLength(value = 10)
    public String uuid_request_number;


    @ApiModelProperty(required = true, readOnly = true, value = "must be supported in Tyrion Hardware-Generator management")
    @Constraints.Required
    public String compiler_target_name;

    @ApiModelProperty(required = true, readOnly = true, value = "Value must be unique! Required 24 chars!")
    @Constraints.MaxLength(value = 24)
    @Constraints.MinLength(value = 24)
    public String full_id;


    @ApiModelProperty(required = true, readOnly = true, value = "Value must be unique! Required 17 chars! - for Example 01:23:45:67:89:ab ")
    @Constraints.MaxLength(value = 17)
    @Constraints.MinLength(value = 17)
    public String mac_address;


}
