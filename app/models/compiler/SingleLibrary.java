package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import utilities.Server;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class SingleLibrary  extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
                         @Column(columnDefinition = "TEXT")  public String description;
                                                             public String library_name;
                                                 @JsonIgnore public String azurePackageLink;
                                                 @JsonIgnore public String azureStorageLink;


    @JsonIgnore @OneToMany(mappedBy="singleLibrary", cascade=CascadeType.ALL) @OrderBy("azureLinkVersion DESC") public List<Version_Object> version_objects = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL)   public List<Processor> processors = new ArrayList<>();


    @JsonProperty public Integer versionsCount()   { return version_objects.size(); }
    @JsonProperty public String  versions()        { return Server.tyrion_serverAddress + "/compilation/library/versions/"   + id; }



    @JsonIgnore
    public void setUniqueAzureStorageLink() {
        while(true){ // I need Unique Value
            this.azureStorageLink = UUID.randomUUID().toString();
            if (SingleLibrary.find.where().eq("azureStorageLink", azureStorageLink ).findUnique() == null) break;
        }
    }

    public static Finder<String, SingleLibrary> find = new Finder<>(SingleLibrary.class);

}
