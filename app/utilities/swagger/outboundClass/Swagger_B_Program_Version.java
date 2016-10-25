package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Version_Object;
import models.project.b_program.B_Program_Hw_Group;
import models.project.m_program.M_Project_Program_SnapShot;

import java.util.ArrayList;
import java.util.List;


@ApiModel(description = "Json Model of Version of B_Program",
        value = "B_Program_Version")
public class Swagger_B_Program_Version {

    @ApiModelProperty(required = true, readOnly = true)
    public Version_Object version_object;

    @ApiModelProperty(required = true, readOnly = true)
    public List<B_Program_Hw_Group> hardware_group = new ArrayList<>();

    @ApiModelProperty(required = true, readOnly = true)
    public List<M_Project_Program_SnapShot> m_project_program_snapshots = new ArrayList<>();

    @ApiModelProperty(required = true, readOnly = true, value = "Json/Javascript Code")
    public String program;

}
