package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for data for new Embedded Hardware",
          value = "Hardware_New_Hardware_Result")
public class Swagger_Hardware_New_Hardware_Result {

    @ApiModelProperty(required = true, readOnly = true)
    @Constraints.Required
    public String mac_address;

    public String status;

}
