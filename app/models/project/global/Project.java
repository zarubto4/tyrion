package models.project.global;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.blocko.TypeOfBlock;
import models.compiler.Board;
import models.grid.Screen_Size_Type;
import models.persons.Person;
import models.project.b_program.B_Program;
import models.project.c_program.C_Program;
import models.project.m_program.M_Project;
import utilities.Server;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Project extends Model {

 /* DATABASE VALUES ---------------------------------------------------------------------------------------------- */
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)         public String id;
    public String project_name;
    public String project_description;

    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Homer>             homerList         = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<B_Program>         b_programs        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<C_Program>         c_programs        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<M_Project>         m_projects        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Screen_Size_Type>  screen_size_types = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<TypeOfBlock>       type_of_blocks    = new ArrayList<>();


    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "owningProjects")  @JoinTable(name = "connected_projects") public List<Person> ownersOfProject = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "projects")        @JoinTable(name = "board_project")      public List<Board> boards = new ArrayList<>();



    @JsonProperty public Integer count_Homers()             { return homerList.size();          }
    @JsonProperty public Integer count_Boards()             { return boards.size();             }
    @JsonProperty public Integer count_c_programs()         { return c_programs.size();         }
    @JsonProperty public Integer count_b_programs()         { return b_programs.size();         }
    @JsonProperty public Integer count_m_projects()         { return m_projects.size();         }
    @JsonProperty public Integer count_owners()             { return ownersOfProject.size();    }
    @JsonProperty public Integer count_type_of_blocks()     { return type_of_blocks.size();     }
    @JsonProperty public Integer count_screen_size_types()  { return screen_size_types.size();  }

    @JsonProperty public String  homers()           { return Server.serverAddress + "/project/project/homers/"     + id; }
    @JsonProperty public String  boards()           { return Server.serverAddress + "/project/boards/"             + id; }
    @JsonProperty public String  b_programs()       { return Server.serverAddress + "/project/project/b_programs/" + id; }
    @JsonProperty public String  c_programs()       { return Server.serverAddress + "/project/project/c_programs/" + id; }
    @JsonProperty public String  m_projects()       { return Server.serverAddress + "/project/project/m_projects/" + id; }
    @JsonProperty public String  owners()           { return Server.serverAddress + "/project/project/owners/"     + id; }
    @JsonProperty public String  type_of_blocks()   { return Server.serverAddress + "/project/blockoBlock/project/"+ id; }
    @JsonProperty public String  screen_size_types(){ return Server.serverAddress + "/grid/screen_type/project/"   + id; }

 /* FINDER --------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Project> find = new Finder<>(Project.class);
    public Project(){}


}

