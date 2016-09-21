package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Board;
import models.compiler.TypeOfBoard;
import models.project.c_program.C_Program;

import java.util.List;


@ApiModel(description = "Json Model for Blocko in Becki for accessible hardware and firmware versions",
        value = "Boards_for_blocko ")
public class Swagger_Boards_for_blocko {


    @ApiModelProperty(required = true, readOnly = true)
    public List<C_Program> c_programs;

    @ApiModelProperty(required = true, readOnly = true)
    public List<TypeOfBoard> type_of_boards;

    @ApiModelProperty(required = true, readOnly = true)
    public List<Board> boards;


}
