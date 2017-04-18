package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for data for new Embedded Hardware",
          value = "Hardware_New_Hardware_Result")
public class Swagger_Hardware_New_Hardware_Request {

    @ApiModelProperty(required = true,  value = "Value must be unique! Required 30 min chars!")
    @Constraints.Required
    @Constraints.MinLength(value = 30)
    public String uuid_request_number;

    @ApiModelProperty(required = true, value = "must be supported in Tyrion Hardware-Generator management")
    @Constraints.Required
    public String compiler_target_name;


    @ApiModelProperty(required = true)
    @Constraints.Required
    public String full_id;

        @ApiModelProperty(required = true)
        @Constraints.Required
        public String   bootloader_id;

        @ApiModelProperty(required = true)
        @Constraints.Required
        public String   firmware_version_id;


    @ApiModelProperty(required = true)
    @Constraints.Required
    public String mac_address;

    @ApiModelProperty(required = true, value = "State:: [complete, in_progress, broken_device,unknown_error]")
    @Constraints.Required
    public String status;

}
