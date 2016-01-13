package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Homer> homerList = new ArrayList<>();
    @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<Board> electronicDevicesList = new ArrayList<>();
    @OneToMany(mappedBy="project", cascade = CascadeType.ALL) public List<HomerProgram>     programs = new ArrayList<>();

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "owningProjects")  @JoinTable(name = "connected_Projects") public List<Person> ownersOfProject = new ArrayList<>();

 /* FINDER --------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Project> find = new Finder<>(Project.class);
    public Project(){}


}

