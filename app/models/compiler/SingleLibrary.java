package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class SingleLibrary  extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
                         @Column(columnDefinition = "TEXT")  public String description;
                                                             public String library_name;
                                                 @JsonIgnore public String azureStorageLink;
                                                 @JsonIgnore public String azurePackageLink;

    @JsonIgnore @OneToMany(mappedBy="singleLibrary", cascade=CascadeType.ALL) @OrderBy("azureLinkVersion DESC") public List<Version_Object> version_objects = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL)   public List<Processor> processors = new ArrayList<>();


    @JsonProperty public List<String>  versions_id()        { List<String> l = new ArrayList<>();  for( Version_Object m : version_objects)  l.add(m.id); return l;  }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void setUniqueAzurePackageLink() {
        while(true){ // I need Unique Value
            this.azurePackageLink = UUID.randomUUID().toString();
            if (SingleLibrary.find.where().eq("azurePackageLink", azurePackageLink ).findUnique() == null) break;
        }
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, SingleLibrary> find = new Finder<>(SingleLibrary.class);

}
