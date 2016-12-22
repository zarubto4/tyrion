package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Model_VersionObject;
import models.project.b_program.Model_BProgramHwGroup;
import models.project.m_program.Model_MProjectProgramSnapShot;

import java.util.ArrayList;
import java.util.List;


@ApiModel(description = "Json Model of Version of B_Program",
        value = "B_Program_Version")
public class Swagger_B_Program_Version {

    @ApiModelProperty(required = true, readOnly = true)
    public Model_VersionObject version_object;

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_BProgramHwGroup> hardware_group = new ArrayList<>();

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_MProjectProgramSnapShot> m_project_program_snapshots = new ArrayList<>();

    @ApiModelProperty(required = true, readOnly = true, value = "Json/Javascript Code")
    public String program;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean remove_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean edit_permission;

}
