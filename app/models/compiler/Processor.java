package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Processor extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true)  public String id;
                                                            @ApiModelProperty(required = true)  public String processor_name;
                @Column(columnDefinition = "TEXT")          @ApiModelProperty(required = true)  public String description;
                                                            @ApiModelProperty(required = true)  public String processor_code;
                                                            @ApiModelProperty(required = true)  public int speed;

    @JsonIgnore @OneToMany(mappedBy="processor", cascade = CascadeType.ALL) public List<TypeOfBoard> typeOfBoards = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "processors")  @JoinTable(name = "processor_libraryGroups")  public List<LibraryGroup>  libraryGroups = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "processors")  @JoinTable(name = "processor_singleLibrary")  public List<SingleLibrary> singleLibraries = new ArrayList<>();


    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String> libraryGroups    (){ List<String> l = new ArrayList<>();  for( LibraryGroup m  : libraryGroups)    l.add(m.id); return l;  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String> singleLibraries  (){ List<String> l = new ArrayList<>();  for( SingleLibrary m : singleLibraries)  l.add(m.id); return l;  }




/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                    public boolean create_permission(){  return SecurityController.getPerson().has_permission("Processor_create"); }
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return true; }

    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  return SecurityController.getPerson().has_permission("Processor_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return SecurityController.getPerson().has_permission("Processor_delete"); }

    public enum permissions{Processor_create, Processor_edit, Processor_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Processor> find = new Finder<>(Processor.class);


}
