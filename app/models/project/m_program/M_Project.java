package models.project.m_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Version_Object;
import models.project.b_program.B_Program;
import models.project.global.Project;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class M_Project extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true)      public String  id;
                                                                                                    public String  program_name;
    @Column(columnDefinition = "TEXT") @ApiModelProperty(required = false, value = "can be empty")  public String  program_description;
    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1458315085338") public Date    date_of_create;


    @JsonIgnore @ManyToOne                                      public Project project;
    @JsonIgnore @OneToOne   @JoinColumn(name="id")    public B_Program b_program; // TODO asi časem předělat na MayToMany!
    @JsonIgnore @OneToOne   @JoinColumn(name="vrs_obj_id")      public Version_Object b_program_version;
                                                                public boolean auto_incrementing;


    @OneToMany(mappedBy="m_project", cascade = CascadeType.ALL) public List<M_Program> m_programs = new ArrayList<>();


    @JsonProperty @Transient public String project_id()                    {  return project.id; }
    @JsonProperty @Transient public String b_progam_connected_version_id() {  return b_program_version == null ? null : b_program_version.id;   }
    @JsonProperty @Transient public String b_program_id()                  {  return b_program         == null ? null : b_program.id; }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/



/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   public Boolean create_permission(){  return ( Project.find.where().where().eq("ownersOfProject.id", SecurityController.getPerson().id ).eq("id", project.id ).findUnique().create_permission() ) || SecurityController.getPerson().has_permission("M_Project_create");      }
    @JsonProperty public Boolean update_permission(){  return ( M_Project.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("M_Project_read"); }
    @JsonIgnore   public Boolean read_permission()  {  return ( M_Project.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("M_Project_read"); }
    @JsonProperty public Boolean edit_permission()  {  return ( M_Project.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("M_Project_edit"); }
    @JsonProperty public Boolean delete_permission(){  return ( M_Project.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("M_Project_delete"); }

    public enum permissions{ M_Project_create, M_Project_read, M_Project_edit, M_Project_delete }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,M_Project> find = new Finder<>(M_Project.class);
}

