package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;

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

    @JsonProperty @Transient public List<String>   versions_id()      { List<String> l = new ArrayList<>();  for( Version_Object m : version_objects)  l.add(m.id); return l;  }
    @JsonProperty @Transient public List<String>   processors_id()    { List<String> l = new ArrayList<>();  for( Processor m      : processors)       l.add(m.id); return l;  }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void setUniqueAzurePackageLink() {
        while(true){ // I need Unique Value
            this.azurePackageLink = UUID.randomUUID().toString();
            if (LibraryGroup.find.where().eq("azurePackageLink", azurePackageLink ).findUnique() == null) break;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient public Boolean create_permission(){  return SecurityController.getPerson().has_permission("LibraryGroup_create"); }
    @JsonIgnore   @Transient public Boolean read_permission()  {  return true; }
    @JsonProperty @Transient public Boolean edit_permission()  {  return SecurityController.getPerson().has_permission("LibraryGroup_edit");   }
    @JsonProperty @Transient public Boolean delete_permission(){  return SecurityController.getPerson().has_permission("LibraryGroup_delete"); }

    public enum permissions{LibraryGroup_create, LibraryGroup_edit, LibraryGroup_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, LibraryGroup> find = new Finder<>(LibraryGroup.class);

}
