package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.Person;
import models.compiler.Board;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Project extends Model {

 /* DATABASE VALUES ---------------------------------------------------------------------------------------------- */
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)         public String projectId;
    public String projectName;
    public String projectDescription;

   @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Homer> homerList = new ArrayList<>();
   @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<HomerProgram>     programs = new ArrayList<>();


    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "owningProjects")  @JoinTable(name = "connected_projects") public List<Person> ownersOfProject = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "projects")        @JoinTable(name = "board_project")      public List<Board> boards = new ArrayList<>();



    @JsonProperty public Integer countOfHomer()     { return homerList.size(); }
    @JsonProperty public Integer countOfBoards()    { return boards.size(); }
    @JsonProperty public Integer countOfPrograms()  { return programs.size(); }
    @JsonProperty public Integer countOfOwners()    { return ownersOfProject.size(); }

    @JsonProperty public String  homers()           { return "http://localhost:9000/project/project/homerList/" + projectId; }
    @JsonProperty public String  boards()           { return "http://localhost:9000/project/boards/" + projectId; }
    @JsonProperty public String  programs()         { return "http://localhost:9000/project/project/programs/" + projectId; }
    @JsonProperty public String  owners()           { return "http://localhost:9000/project/project/owners/" + projectId; }
 /* FINDER --------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Project> find = new Finder<>(Project.class);
    public Project(){}


}

