package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class FileRecord extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
                                                         public String filename;

    @JsonIgnore @OrderBy("azureLinkVersion DESC") @ManyToMany(cascade = CascadeType.ALL, mappedBy = "files") @JoinTable(name = "libraries_versions")  public List<Version>   versions = new ArrayList<>();

    @JsonProperty   public String fileRecord()   { return "http://localhost:9000/compilation/library/fileRecord/" +id; }


    public static Finder<String, FileRecord> find = new Finder<>(FileRecord.class);

}
