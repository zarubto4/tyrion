package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class LibraryRecord extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
                                                         public String filename;

    @OrderBy("azureLinkVersion DESC")
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "records")
    @JoinTable(name = "libraries_versions")  public List<Version>   versions = new ArrayList<>();

    @JsonProperty   public String fileRecord()   { return "http://localhost:9000/compilation/library/fileRecord/" +id; }


   // @JsonProperty public Integer versions()        { return versions.size(); }
   // @JsonProperty public Double  lastVersion()     { return versions.isEmpty()      ? null : versions.get(0).azureLinkVersion; }


    //@JsonProperty public String  content ()      { return azureStorageLink == null   ? null : "http://localhost:9000/compilation/library/content/" +  this.id;}
   // @JsonProperty  @JsonInclude(JsonInclude.Include.NON_EMPTY) public String  libraryGroups() { return libraryGroups.isEmpty() ? null : "http://localhost:9000/compilation/library/libraryGroups/" +  this.id;}
   // @JsonProperty  @JsonInclude(JsonInclude.Include.NON_EMPTY) public Integer inLibraries()   { return libraryGroups.isEmpty() ? null : libraryGroups.size(); }

    public static Finder<String, LibraryRecord> find = new Finder<>(LibraryRecord.class);

}
