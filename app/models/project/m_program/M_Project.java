package models.project.m_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Version_Object;
import models.project.b_program.B_Program;
import models.project.global.Project;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@ApiModel( value =
        "<h3>Permissions:</h3>" +
        "<span style=\"color: green\">"    + M_Project.create_permission_docs +  "</span>" +
        "<br><span style=\"color: blue\">" + M_Project.read_permission_docs +    "</span>" +
        "<br>"
)
public class M_Project extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true)      public String  id;
                                                                                                    public String  program_name;
    @Column(columnDefinition = "TEXT") @ApiModelProperty(required = false, value = "can be empty")  public String  program_description;
    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1461854312") public Date    date_of_create;


    @JsonIgnore @ManyToOne                                      public Project project;
    @JsonIgnore @OneToOne   @JoinColumn(name="b_program_id")    public B_Program b_program;
    @JsonIgnore @OneToOne   @JoinColumn(name="vrs_obj_id")      public Version_Object b_program_version;
                                                                public boolean auto_incrementing;

    @OneToMany(mappedBy="m_project", cascade = CascadeType.ALL) public List<M_Program> m_programs = new ArrayList<>();


    @JsonProperty @Transient public String project_id()                    {  return project.id; }
    @JsonProperty @Transient public String b_progam_connected_version_id() {  return b_program_version == null ? null : b_program_version.id;   }
    @JsonProperty @Transient public String b_program_id()                  {  return b_program         == null ? null : b_program.id; }



    /* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/



/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have Project.update_permission = true, you can create M_project on this Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have Project.update_permission = true, you can create M_project on this Project - Or you need static/dynamic permission key";

    @JsonIgnore   @Transient public Boolean create_permission(){  return ( Project.find.where().eq("ownersOfProject.id",   SecurityController.getPerson().id).eq("id", project.id ).findUnique().create_permission() ) || SecurityController.getPerson().has_permission("M_Project_create");      }
    @JsonProperty @Transient public Boolean update_permission(){  return ( M_Project.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("M_Project_update"); }
    @JsonIgnore   @Transient public Boolean read_permission()  {  return ( M_Project.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("M_Project_read"); }
    @JsonProperty @Transient public Boolean edit_permission()  {  return ( M_Project.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("M_Project_edit"); }
    @JsonProperty @Transient public Boolean delete_permission(){  return ( M_Project.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("M_Project_delete"); }

    public enum permissions{  M_Project_create, M_Project_update, M_Project_read,  M_Project_edit, M_Project_delete; }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,M_Project> find = new Finder<>(M_Project.class);
}

