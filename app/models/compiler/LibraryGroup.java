package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class LibraryGroup extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String id;
                                                            public String groupName;
             @JsonIgnore @Column(columnDefinition = "TEXT") public String description;

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL)   public List<Processor> processors = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL)   public List<Library>   libraries = new ArrayList<>();


    @JsonProperty public String description(){  return "http://localhost:9000/compilation/libraryGroup/generalDescription/" +  this.id;}
    @JsonProperty public String processors (){  return "http://localhost:9000/compilation/libraryGroup/processors/" +  this.id;}
    @JsonProperty public String libraries  (){  return "http://localhost:9000/compilation/libraryGroup/libraries/"  +  this.id;}



    public static Finder<String, LibraryGroup> find = new Finder<>(LibraryGroup.class);
}
