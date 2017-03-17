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

    @ApiModel(value = "Hardware_group_IN")
    public static class Board_backup_pair {

        public Board_backup_pair(){}

        @Constraints.Required @ApiModelProperty(required = true)  public boolean backup_mode;
        @Constraints.Required @ApiModelProperty(required = true)  public String  board_id;

    }
}
