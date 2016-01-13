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
   @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Board> electronicDevicesList = new ArrayList<>();
   @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<HomerProgram>     programs = new ArrayList<>();

   @JsonProperty public Integer countOfHomer(){ return homerList.size(); }
   @JsonProperty public Integer countOfBoards(){ return electronicDevicesList.size(); }
   @JsonProperty public Integer countOfPrograms(){ return programs.size(); }

    @JsonProperty public String homers()             { return "http://localhost:9000/project/project/homerList/" + projectId; }
    @JsonProperty public String boards() { return "http://localhost:9000/project/project/electronicDevicesList/" + projectId; }
    @JsonProperty public String programs()              { return "http://localhost:9000/project/project/programs/" + projectId; }


    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "owningProjects")  @JoinTable(name = "connected_Projects") public List<Person> ownersOfProject = new ArrayList<>();

 /* FINDER --------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Project> find = new Finder<>(Project.class);
    public Project(){}


}

