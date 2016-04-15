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

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String id;
                                                            public String group_name;
                         @Column(columnDefinition = "TEXT") public String description;

                                                @JsonIgnore public String azurePackageLink;
                                                @JsonIgnore public String azureStorageLink;

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL) public List<Processor> processors = new ArrayList<>();

    @JsonIgnore @OneToMany(mappedBy="libraryGroup", cascade=CascadeType.ALL) @OrderBy("azureLinkVersion DESC") public List<Version_Object> version_objects = new ArrayList<>();

    @JsonProperty public List<String>   versions_id()      { List<String> l = new ArrayList<>();  for( Version_Object m : version_objects)  l.add(m.id); return l;  }
    @JsonProperty public List<String>   processors_id()    { List<String> l = new ArrayList<>();  for( Processor m      : processors)       l.add(m.id); return l;  }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void setUniqueAzureStorageLink() {
        while(true){ // I need Unique Value
            this.azureStorageLink = UUID.randomUUID().toString();
            if (LibraryGroup.find.where().eq("azureStorageLink", azureStorageLink ).findUnique() == null) break;
        }
    }



/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, LibraryGroup> find = new Finder<>(LibraryGroup.class);

}
