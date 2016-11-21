package models.project.b_program.instnace;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Version_Object;
import models.project.b_program.B_Program_Hw_Group;
import models.project.m_program.M_Project_Program_SnapShot;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class Homer_Instance_Record  extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
    @JsonIgnore  public String websocket_grid_token;

    @JsonIgnore @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY) public Homer_Instance main_instance_history;

    @ApiModelProperty(required = false, readOnly = true, value = "can be null")   public Date date_of_created;
    @ApiModelProperty(required = false, readOnly = true, value = "can be null")   public Date running_from;
    @ApiModelProperty(required = false, readOnly = true, value = "can be null")   public Date running_to;
    @ApiModelProperty(required = false, readOnly = true, value = "can be null")   public Date planed_when;

    @JsonIgnore @ManyToOne() public Version_Object version_object;
    @JsonIgnore @OneToOne(cascade=CascadeType.ALL) public Homer_Instance actual_running_instance;



/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @Transient @JsonProperty @ApiModelProperty(required = true, readOnly = true) public  String b_program_version_id()    {  return version_object.id;}
    @Transient @JsonProperty @ApiModelProperty(required = true, readOnly = true) public  String b_program_version_name()  {  return version_object.version_name;}
    @Transient @JsonProperty @ApiModelProperty(required = true, readOnly = true) public  String instance_record_id()      {  return this.id;}

    @Transient @JsonProperty @ApiModelProperty(required = true, readOnly = true) public List<B_Program_Hw_Group> hardware_group()               {  return version_object.b_program_hw_groups;}
    @Transient @JsonProperty @ApiModelProperty(required = true, readOnly = true) public List<M_Project_Program_SnapShot> m_project_snapshop()    {  return version_object.b_program_version_snapshots;}

/* ENUMS PARAMETERS ----------------------------------------------------------------------------------------------------*/


    @Override
    public void save(){
        this.websocket_grid_token = UUID.randomUUID().toString() +"_"+ UUID.randomUUID().toString();
        super.save();
    }


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Homer_Instance_Record> find = new Finder<>(Homer_Instance_Record.class);

}
