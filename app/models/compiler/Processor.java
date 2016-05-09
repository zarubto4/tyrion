package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Processor extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
                                                             public String processor_name;
                @Column(columnDefinition = "TEXT")           public String description;
                                                             public String processor_code;
                                                             public int speed;

    @JsonIgnore @OneToMany(mappedBy="processor", cascade = CascadeType.ALL) public List<TypeOfBoard> typeOfBoards = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "processors")  @JoinTable(name = "processor_libraryGroups")  public List<LibraryGroup>  libraryGroups = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "processors")  @JoinTable(name = "processor_singleLibrary")  public List<SingleLibrary> singleLibraries = new ArrayList<>();


    @JsonProperty @Transient public List<String> libraryGroups    (){ List<String> l = new ArrayList<>();  for( LibraryGroup m  : libraryGroups)    l.add(m.id); return l;  }
    @JsonProperty @Transient public List<String> singleLibraries  (){ List<String> l = new ArrayList<>();  for( SingleLibrary m : singleLibraries)  l.add(m.id); return l;  }




/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/




/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, Processor> find = new Finder<>(Processor.class);


}
