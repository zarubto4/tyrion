package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.TypeOfBoard;

import java.util.List;


@ApiModel(description = "Json Model for Blocko in Becki for accessible hardware and firmware versions",
        value = "Boards_For_Blocko")
public class Swagger_Boards_For_Blocko {


    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_C_program_Short_Detail> c_programs;

    @ApiModelProperty(required = true, readOnly = true)
    public List<TypeOfBoard> type_of_boards;

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_Board_Short_Detail> boards;

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_M_Project_Short_Detail> m_projects;


}
