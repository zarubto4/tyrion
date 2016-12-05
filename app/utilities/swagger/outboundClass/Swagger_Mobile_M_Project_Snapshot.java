package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.project.m_program.M_Project_Program_SnapShot;

import java.util.List;

@ApiModel(description = "",
        value = "Terminal_M_Project_Snapshot")
public class Swagger_Mobile_M_Project_Snapshot {

    @ApiModelProperty(required = true, readOnly = true)  public String instance_record_id;

    @ApiModelProperty(required = true, readOnly = true)   public String b_program_name;
    @ApiModelProperty(required = true, readOnly = true)    public String b_program_description;

    @ApiModelProperty(required = true, readOnly = true)    public List<M_Project_Program_SnapShot> snapshots;
}
