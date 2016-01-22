package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class LibraryGroup extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String id;
                                                            public String groupName;
             @JsonIgnore @Column(columnDefinition = "TEXT") public String description;

                                                @JsonIgnore public String azurePackageLink;
                                                @JsonIgnore public String azureStorageLink;
                                                            public String azurePrimaryUrl;
                                                            public String azureSecondaryUrl;

         @JsonIgnore @ManyToMany(cascade = CascadeType.ALL) public List<Processor> processors = new ArrayList<>();


    @JsonIgnore @OneToMany(mappedBy="libraryGroup", cascade=CascadeType.ALL) @OrderBy("azureLinkVersion DESC") public List<Version> versions = new ArrayList<>();


    @JsonProperty public Integer versionsCount() { return versions.size(); }
    @JsonProperty public Double  lastVersion()   { return versions.isEmpty()      ? null : versions.get(0).azureLinkVersion; }
    @JsonProperty public String  versions()      { return "http://localhost:9000/compilation/libraryGroup/versions/" + id; }


    @JsonProperty public String  description     (){ return description.isEmpty()   ? null  : "http://localhost:9000/compilation/libraryGroup/generalDescription/" +  this.id;}
    @JsonProperty public String  processors      (){ return processors.isEmpty()    ? null  : "http://localhost:9000/compilation/libraryGroup/processors/" +  this.id;}

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_EMPTY) public String  lastLibraries(){ return versions.isEmpty() ? null  : "http://localhost:9000/compilation/libraryGroup/libraries/" + this.id;}

    public static Finder<String, LibraryGroup> find = new Finder<>(LibraryGroup.class);


}
