package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class LibraryGroup extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String id;
                                                            public String group_name;
                         @Column(columnDefinition = "TEXT") public String description;

                                                @JsonIgnore public String azurePackageLink;
                                                @JsonIgnore public String azureStorageLink;

         @JsonIgnore @ManyToMany(cascade = CascadeType.ALL) public List<Processor> processors = new ArrayList<>();


    @JsonIgnore @OneToMany(mappedBy="libraryGroup", cascade=CascadeType.ALL) @OrderBy("azureLinkVersion DESC") public List<Version_Object> versionObjects = new ArrayList<>();


    @JsonProperty public Integer versionsCount() { return versionObjects.size(); }
    @JsonProperty public String  versions()      { return                                   "http://localhost:9000/compilation/libraryGroup/versions/"   + id; }
    @JsonProperty public String  processors()    { return processors.isEmpty()    ? null :  "http://localhost:9000/compilation/libraryGroup/processors/" +  id;}


    @JsonIgnore
    public void setUniqueAzureStorageLink() {
        while(true){ // I need Unique Value
            this.azureStorageLink = UUID.randomUUID().toString();
            if (LibraryGroup.find.where().eq("azureStorageLink", azureStorageLink ).findUnique() == null) break;
        }
    }

    public static Finder<String, LibraryGroup> find = new Finder<>(LibraryGroup.class);
}
