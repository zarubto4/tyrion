package models.project.global;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import models.blocko.TypeOfBlock;
import models.compiler.Board;
import models.grid.Screen_Size_Type;
import models.overflow.HashTag;
import models.overflow.Post;
import models.person.Person;
import models.project.b_program.B_Program;
import models.project.b_program.Homer;
import models.project.c_program.C_Program;
import models.project.community.Documentation;
import models.project.community.PrintedModel;
import models.project.community.RequiredHW;
import models.project.m_program.M_Project;
import utilities.project.Project_type;

import javax.persistence.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Project extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
                                                             public String project_name;
                                                             public String project_description;

    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Homer>             homerList         = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<B_Program>         b_programs        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<C_Program>         c_programs        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<M_Project>         m_projects        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Screen_Size_Type>  screen_size_types = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<TypeOfBlock>       type_of_blocks    = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Board>             boards            = new ArrayList<>();

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "owningProjects")  @JoinTable(name = "connected_projects") public List<Person> ownersOfProject = new ArrayList<>();


    @JsonProperty @Transient public List<String> homers_id()           { List<String> l = new ArrayList<>();  for( Homer m            : homerList)         l.add(m.id); return l;  }
    @JsonProperty @Transient public List<String> boards_id()           { List<String> l = new ArrayList<>();  for( Board m            : boards)            l.add(m.id); return l;  }
    @JsonProperty @Transient public List<String> b_programs_id()       { List<String> l = new ArrayList<>();  for( B_Program m        : b_programs)        l.add(m.id); return l;  }
    @JsonProperty @Transient public List<String> c_programs_id()       { List<String> l = new ArrayList<>();  for( C_Program m        : c_programs)        l.add(m.id); return l;  }
    @JsonProperty @Transient public List<String> m_projects_id()       { List<String> l = new ArrayList<>();  for( M_Project m        : m_projects)        l.add(m.id); return l;  }
    @JsonProperty @Transient public List<String> owners_id()           { List<String> l = new ArrayList<>();  for( Person m           : ownersOfProject)   l.add(m.id); return l;  }
    @JsonProperty @Transient public List<String> type_of_blocks_id()   { List<String> l = new ArrayList<>();  for( TypeOfBlock m      : type_of_blocks)    l.add(m.id); return l;  }
    @JsonProperty @Transient public List<String> screen_size_types_id(){ List<String> l = new ArrayList<>();  for( Screen_Size_Type m : screen_size_types) l.add(m.id); return l;  }

    // Community -----------------------------------------
    // TODO metody vracející id všech prvků listu
    //public Image mainImage;  TODO obrázek
    public Project_type typeOfProject;
    @OneToOne public Documentation documentation;
    @JsonIgnore public List<Project> similarProjects = new ArrayList<>();
    @JsonIgnore public List<RequiredHW> requiredHW = new ArrayList<>();
    @JsonIgnore public List<PrintedModel> printedModels = new ArrayList<>();
    public int views;
    @JsonIgnore public List<Person> followers = new ArrayList<>();
    @JsonIgnore public List<Person> editors = new ArrayList<>();
    @JsonIgnore public List<Post> posts = new ArrayList<>();
    @JsonIgnore public List<HashTag> hashTags = new ArrayList<>();

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: For all project: User can read project on API: {GET /project/project) - get Project by logged Person ";


    @JsonIgnore   @Transient public Boolean create_permission()    {  return true;  }
    @JsonProperty @Transient public Boolean update_permission()    {  return ( Project.find.where().eq("ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_update");  }
    @JsonIgnore   @Transient public Boolean read_permission()      {  return ( Project.find.where().eq("ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_read");    }

    @JsonProperty @Transient public Boolean unshare_permission()   {  return ( Project.find.where().eq("ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_unshare"); }
    @JsonProperty @Transient  public Boolean share_permission ()   {  return ( Project.find.where().eq("ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_share");   }

    @JsonProperty @Transient public Boolean edit_permission()      {  return ( Project.find.where().eq("ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_edit");    }
    @JsonProperty @Transient public Boolean delete_permission()    {  return ( Project.find.where().eq("ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Project_delete");  }

    public enum permissions{Project_update, Project_read, Project_unshare , Project_share, Project_edit, Project_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
   public static Finder<String,Project> find = new Finder<>(Project.class);

}

