package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json model for set Backup Mode on devices",
        value = "Board_ModeBackup")
public class Swagger_Board_ModeBackup {

    @Constraints.Required @Valid
    @ApiModelProperty(value = "Must be unique!!!, The hardware_id must have 20 hexadecimal characters!, It can combination of master devices and others", required = true)
    public List<UUID> device_ids = new ArrayList<>();

    @Constraints.Required @Valid
    public boolean backup_mode;

}
