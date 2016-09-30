package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.List;


@ApiModel(description = "Json Model for create new Board",
          value = "BootLoader_Update_Devices")
public class Swagger_BootLoader_Update_Devices {

    @Constraints.Required
    @Valid
    @ApiModelProperty(value = "Must be unique!!!, The hardware_id must have 20 hexadecimal characters!", required = true)
    public List<String> device_ids;


}
