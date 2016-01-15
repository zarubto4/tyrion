package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class SingleLibrary  extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
             @JsonIgnore @Column(columnDefinition = "TEXT")  public String description;
                                                             public String libraryName;
                                                 @JsonIgnore public String azurePackageLink;
                                                 @JsonIgnore public String azureStorageLink;

    @JsonProperty public String  description()     { return description == null     ? null : "http://localhost:9000/compilation/library/generalDescription/" +  this.id;}
    @JsonIgnore @OneToMany(mappedBy="singleLibrary", cascade=CascadeType.ALL) @OrderBy("azureLinkVersion DESC") public List<Version> versions = new ArrayList<>();

    @JsonProperty public Integer versions()        { return versions.size(); }
    @JsonProperty public Double  lastVersion()     { return versions.isEmpty()      ? null : versions.get(0).azureLinkVersion; }


    //  @JsonProperty public String  records       (){ return records.isEmpty()     ? null  : "http://localhost:9000/compilation/libraryGroup/libraries/"  +  this.id;}
    //  @JsonProperty public Integer librariesCount  (){ return records.size(); }
    

    public static Finder<String, SingleLibrary> find = new Finder<>(SingleLibrary.class);

}
