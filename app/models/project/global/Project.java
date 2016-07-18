package models.project.global;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.blocko.TypeOfBlock;
import models.compiler.Board;
import models.grid.Screen_Size_Type;
import models.person.Invitation;
import models.person.Person;
import models.project.b_program.B_Program;
import models.project.b_program.Homer_Instance;
import models.project.b_program.servers.Private_Homer_Server;
import models.project.c_program.C_Program;
import models.project.c_program.actualization.Actualization_procedure;
import models.project.m_program.M_Project;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Project extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
                                                             public String project_name;
                                                             public String project_description;

    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Private_Homer_Server> privateHomerServerList = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<B_Program>                b_programs        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<C_Program>                c_programs        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<M_Project>                m_projects        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Screen_Size_Type>         screen_size_types = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<TypeOfBlock>              type_of_blocks    = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Board>                    boards            = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Actualization_procedure>  procedures        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Homer_Instance>           instances         = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Invitation>          invitations       = new ArrayList<>();


    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "owningProjects")  @JoinTable(name = "connected_projects") public List<Person> ownersOfProject = new ArrayList<>();


    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String> homers_id()           { List<String> l = new ArrayList<>();  for( Private_Homer_Server m    : privateHomerServerList)   l.add(m.id); return l;  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String> boards_id()           { List<String> l = new ArrayList<>();  for( Board m                   : boards)                   l.add(m.id); return l;  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String> b_programs_id()       { List<String> l = new ArrayList<>();  for( B_Program m               : b_programs)               l.add(m.id); return l;  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String> c_programs_id()       { List<String> l = new ArrayList<>();  for( C_Program m               : c_programs)               l.add(m.id); return l;  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String> m_projects_id()       { List<String> l = new ArrayList<>();  for( M_Project m               : m_projects)               l.add(m.id); return l;  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String> owners_id()           { List<String> l = new ArrayList<>();  for( Person m                  : ownersOfProject)          l.add(m.id); return l;  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String> type_of_blocks_id()   { List<String> l = new ArrayList<>();  for( TypeOfBlock m             : type_of_blocks)           l.add(m.id); return l;  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String> screen_size_types_id(){ List<String> l = new ArrayList<>();  for( Screen_Size_Type m        : screen_size_types)        l.add(m.id); return l;  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String> actual_procedures_id(){ List<String> l = new ArrayList<>();  for( Actualization_procedure m : procedures)               l.add(m.id); return l;  }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: For all project: User can read project on API: {GET /project/project) - get Project by logged Person ";


    @JsonIgnore   @Transient @ApiModelProperty(required = true) public Boolean create_permission()    {  return true;  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public Boolean update_permission()    {  return ( Project.find.where().eq("ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_update");  }
    @JsonIgnore   @Transient @ApiModelProperty(required = true) public Boolean read_permission()      {  return ( Project.find.where().eq("ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_read");}

    @JsonProperty @Transient @ApiModelProperty(required = true) public Boolean unshare_permission()  {  return ( Project.find.where().eq("ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_unshare"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public Boolean share_permission ()    {  return ( Project.find.where().eq("ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_share");   }

    @JsonProperty @Transient @ApiModelProperty(required = true) public Boolean edit_permission()      {  return ( Project.find.where().eq("ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_edit");    }
    @JsonProperty @Transient @ApiModelProperty(required = true) public Boolean delete_permission()    {  return ( Project.find.where().eq("ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_delete");  }

    public enum permissions{Project_update, Project_read, Project_unshare , Project_share, Project_edit, Project_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
   public static Model.Finder<String,Project> find = new Finder<>(Project.class);

}

