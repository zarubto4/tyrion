package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model with List of Board ID",
        value = "List of Boards")
public class Swagger_UploadBinaryFileToBoard {

    @Valid
    @ApiModelProperty(value = "List of Pairs for settings of Backup C_Program Version on boards", required = true)
    public List<Swagger_Board_CProgram_Pair> board_pairs  = new ArrayList<>();

}
