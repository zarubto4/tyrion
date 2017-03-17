package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for set Backup Firmware in devices, its combination of pairs <Board_id, C_Program_version_id> ",
        value = "Board_SetBackup")
public class Swagger_Board_SetBackup {

    @Valid @ApiModelProperty(value = "List of Pairs for settings of Backup C_Program Version on boards", required = true)
    public List<Board_backup_pair> board_backup_pair_list  = new ArrayList<>();

    @ApiModel(value = "Hardware_group_IN")
    public static class Board_backup_pair {

        public Board_backup_pair(){}

        @Constraints.Required @ApiModelProperty(required = true)  public String c_program_version_id;
        @Constraints.Required @ApiModelProperty(required = true)  public String board_id;

    }

}
