package utilities.swagger.outboundClass.Filter_List;

import models.compiler.Version_Object;
import models.project.b_program.B_Pair;

import java.util.ArrayList;
import java.util.List;

public class Swagger_B_Program_Version {
    public Version_Object version_Object;
    public List<B_Pair> connected_boards = new ArrayList<>();
    public B_Pair master_board;
}
