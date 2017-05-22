package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "",
        value = "Board_Backup_settings")
public class Swagger_Board_Backup_settings {

    @Valid @ApiModelProperty(value = "List of Pairs for settings of Backup on boards", required = true)
    public List<Board_backup_pair> board_backup_pair_list  = new ArrayList<>();

    public static class Board_backup_pair {

        public Board_backup_pair(){}

        @Constraints.Required @ApiModelProperty(required = true, value = "True - for auto_backup. False for static backup. If static c_program_version_id is required!")  public boolean backup_mode;
                              @ApiModelProperty(required = false, value = "Required if backup_mode is false. C_program_version_id must be compiled and for same type of Board!")  public String c_program_version_id;
        @Constraints.Required @ApiModelProperty(required = true)  public String  board_id;

    }
}
