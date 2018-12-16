package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model with List of Board ID",
        value = "DeployFirmware")
public class Swagger_DeployFirmware {

    @Constraints.Required @ApiModelProperty(required = true)
    public UUID hardware_id;

    @Constraints.Required @ApiModelProperty(required = true)
    public UUID c_program_version_id;

}
