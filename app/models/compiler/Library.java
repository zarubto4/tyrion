package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Library extends Model {

    @Id public String id;
    @JsonIgnore @Column(columnDefinition = "TEXT")  public String description;
    @JsonIgnore @Column(columnDefinition = "TEXT")  public String content;

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "libraries")  @JoinTable(name = "libraries_libraryGroups")  public List<LibraryGroup> libraryGroups = new ArrayList<>();

    @JsonProperty public String description(){  return "http://localhost:9000/compilation/library/generalDescription/" +  this.id;}
    @JsonProperty public String content (){  return "http://localhost:9000/compilation/library/content/" +  this.id;}
    @JsonProperty public String libraryGroups (){  return "http://localhost:9000/compilation/library/libraryGroups/" +  this.id;}
}
