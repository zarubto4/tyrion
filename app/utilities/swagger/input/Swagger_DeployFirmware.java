package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model with List of Board ID",
        value = "DeployFirmware")
public class Swagger_DeployFirmware {

    @Valid
    @ApiModelProperty(value = "List of Pairs for settings of Backup C_Program Version on hardware", required = true)
    public List<Swagger_Board_CProgram_Pair> hardware_pairs = new ArrayList<>();

}
