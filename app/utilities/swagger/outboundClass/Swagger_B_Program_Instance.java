package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.project.b_program.B_Program_Hw_Group;
import models.project.m_program.M_Project_Program_SnapShot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiModel(description = "Json Model for B_Program state",
        value = "B_Program_Instance")
public class Swagger_B_Program_Instance {

    @ApiModelProperty(required = true,  readOnly = true)     public String instance_record_id;

    @ApiModelProperty(required = true,  readOnly = true, value = "can be null")     public Date date_of_created;
    @ApiModelProperty(required = true,  readOnly = true, value = "can be null")     public Date running_from;
    @ApiModelProperty(required = true,  readOnly = true, value = "can be null")     public Date running_to;
    @ApiModelProperty(required = false, readOnly = true, value = "can be null")     public Date planed_when;


    @ApiModelProperty(required = false, readOnly = true)  public String b_program_name;
    @ApiModelProperty(required = false, readOnly = true)  public String b_program_id;

    @ApiModelProperty(required = false, readOnly = true)  public String b_program_version_name;
    @ApiModelProperty(required = false, readOnly = true)  public String b_program_version_id;

    @ApiModelProperty(required = true, readOnly = true)   public boolean server_is_online;
    @ApiModelProperty(required = true, readOnly = true)   public boolean instance_is_online;

    @ApiModelProperty(required = true, readOnly = true)   public String server_name;
    @ApiModelProperty(required = true, readOnly = true)   public String server_id;

    @ApiModelProperty(required = true, readOnly = true, value = "can be empty")
    public List<B_Program_Hw_Group> hardware_group = new ArrayList<>();

    @ApiModelProperty(required = true, readOnly = true, value = "can be empty")
    public List<M_Project_Program_SnapShot> m_project_program_snapshots = new ArrayList<>();


    @ApiModelProperty(required = false, readOnly = true)  public String instance_remote_url;

}
