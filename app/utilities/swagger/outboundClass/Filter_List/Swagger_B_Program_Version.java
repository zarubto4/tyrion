package utilities.swagger.outboundClass.Filter_List;

import io.swagger.annotations.ApiModel;
import models.compiler.Version_Object;
import models.project.b_program.B_Pair;

import java.util.ArrayList;
import java.util.List;


@ApiModel(description = "Json Model of Version of B_Program",
        value = "B_Program_Version")
public class Swagger_B_Program_Version {
    public Version_Object version_Object;
    public List<B_Pair> connected_boards = new ArrayList<>();
    public B_Pair master_board;
    public String program;
}
