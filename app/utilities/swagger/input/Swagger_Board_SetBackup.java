package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for set Backup Firmware in devices, its combination of pairs <Board_id, C_Program_version_id> ",
        value = "Board_SetBackup")
public class Swagger_Board_SetBackup {

    @Valid @ApiModelProperty(value = "List of Pairs for settings of Backup C_Program Version on boards", required = true)
    public List<Swagger_Board_CProgram_Pair> board_backup_pair_list  = new ArrayList<>();


}
