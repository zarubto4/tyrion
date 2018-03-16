package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "", value = "Board_Bootloader_Update")
public class Swagger_Board_Bootloader_Update {

    @Constraints.Required @Valid
    @ApiModelProperty(value = "Must be unique!!!, The hardware_id must have 20 hexadecimal characters!, It can combination of master devices and others", required = true)
    public List<UUID> device_ids = new ArrayList<>();

    @Constraints.Required
    @ApiModelProperty(hidden = true, required = false, value = "If bootloader_is empty, system will used latest version")
    public UUID bootloader_id;

}
