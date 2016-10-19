package models.project.m_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Version_Object;
import models.project.global.Project;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class M_Project extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true)      public String  id;
    @ApiModelProperty(required = true)                                                              public String  name;
    @Column(columnDefinition = "TEXT") @ApiModelProperty(required = false, value = "can be empty")  public String  description;
    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp in millis", example = "14618543121234") public Date    date_of_create;


    @JsonIgnore @ManyToOne                                                                                         public Project project;
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "m_projects")  @JoinTable(name = "b_program_id") public List<Version_Object> b_program_version;


    @ApiModelProperty(required = true)  public boolean auto_incrementing;

    @ApiModelProperty(required = true)
    @OneToMany(mappedBy="m_project", cascade = CascadeType.ALL) public List<M_Program> m_programs = new ArrayList<>();



/* JSON PROPERTY METHOD ---------------------------------------------------------------------------------------------------------*/
    
    @JsonProperty @Transient  @ApiModelProperty(required = true)                         public String project_id()                          {  return project.id; }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


    /* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/
    @JsonIgnore            private String azure_m_project_link;


    @JsonIgnore @Override public void save() {

        while(true){ // I need Unique Value
            this.azure_m_project_link = project.get_path()  + "/m-projects/"  + UUID.randomUUID().toString();
            if (M_Project.find.where().eq("azure_m_project_link", azure_m_project_link ).findUnique() == null) break;
        }

        super.save();
    }

    @JsonIgnore @Transient
    public String get_path(){
        return  azure_m_project_link;
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have Project.read_permission = true, you can read M_project on this Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have Project.update_permission = true, you can create M_project on this Project - Or you need static/dynamic permission key";

    @JsonIgnore   @Transient                                     public boolean create_permission(){  return ( Project.find.where().eq("ownersOfProject.id",   SecurityController.getPerson().id).eq("id", project.id ).findUnique().create_permission() ) || SecurityController.getPerson().has_permission("M_Project_create");      }
    @JsonIgnore   @Transient                                     public boolean read_permission()  {  return ( M_Project.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("M_Project_read"); }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean update_permission(){  return ( M_Project.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("M_Project_update"); }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean edit_permission()  {  return ( M_Project.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("M_Project_edit"); }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean delete_permission(){  return ( M_Project.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("M_Project_delete"); }

    public enum permissions{  M_Project_create, M_Project_update, M_Project_read,  M_Project_edit, M_Project_delete; }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,M_Project> find = new Finder<>(M_Project.class);
}

