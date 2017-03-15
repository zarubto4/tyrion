package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "",
        value = "Board_Bootloader_Update ")
public class Swagger_Board_Bootloader_Update {

    @Constraints.Required @Valid
    @ApiModelProperty(value = "Must be unique!!!, The hardware_id must have 20 hexadecimal characters!", required = true)
    public List<String> device_ids = new ArrayList<>();

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String bootloader_id;

}
