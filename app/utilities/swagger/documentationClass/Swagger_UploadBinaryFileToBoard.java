package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model with List of Board ID",
        value = "List of Boards")
public class Swagger_UploadBinaryFileToBoard {


    @Valid
    @ApiModelProperty(value = "List of Pairs for settings of Backup C_Program Version on boards", required = true)
    public List<Board_pair> board_pairs  = new ArrayList<>();

    @ApiModel(value = "Hardware_IN")
    public static class Board_pair {

        public Board_pair(){}

        @Constraints.Required @ApiModelProperty(required = true)  public String c_program_version_id;
        @Constraints.Required @ApiModelProperty(required = true)  public String board_id;

    }
}
