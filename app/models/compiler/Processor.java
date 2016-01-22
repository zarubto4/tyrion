package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Processor extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
                                                             public String processorName;
         @JsonIgnore @Column(columnDefinition = "TEXT")      public String description;
                                                             public String processorCode;
                                                             public int speed;

    @JsonIgnore @OneToMany(mappedBy="processor", cascade = CascadeType.ALL) public List<TypeOfBoard> typeOfBoards = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "processors")  @JoinTable(name = "processor_libraryGroups")  public List<LibraryGroup> libraryGroups = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "processors")  @JoinTable(name = "processor_singleLibrary")  public List<SingleLibrary> singleLibraries = new ArrayList<>();


    @JsonProperty public String description   (){return "http://localhost:9000/compilation/processor/generalDescription/"    +  this.id;}
    @JsonProperty public String libraryGroups (){return "http://localhost:9000/compilation/processor/libraryGroups/" +  this.id;}
    @JsonProperty public String singleLibraries (){return "http://localhost:9000/compilation/processor/singleLibrary/" +  this.id;}


    @JsonProperty public Integer libraryGroupsCount()  { return libraryGroups.size(); }
    @JsonProperty public Integer singleLibrariesCount(){ return singleLibraries.size(); }




    public static Finder<String, Processor> find = new Finder<>(Processor.class);


}
