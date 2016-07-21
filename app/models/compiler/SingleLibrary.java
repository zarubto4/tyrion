package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class SingleLibrary  extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true) public String id;
                         @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true) public String description;
                                                            @ApiModelProperty(required = true) public String library_name;
                                                                                   @JsonIgnore public String azureStorageLink;
                                                                                   @JsonIgnore public String azurePackageLink;

    @JsonIgnore @OneToMany(mappedBy="single_library", cascade=CascadeType.ALL) @OrderBy("azureLinkVersion DESC") public List<Version_Object> version_objects = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL)   public List<Processor> processors = new ArrayList<>();


    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String>  versions_id()        { List<String> l = new ArrayList<>();  for( Version_Object m : version_objects)  l.add(m.id); return l;  }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void setUniqueAzurePackageLink() {
        while(true){ // I need Unique Value
            this.azurePackageLink = UUID.randomUUID().toString();
            if (SingleLibrary.find.where().eq("azurePackageLink", azurePackageLink ).findUnique() == null) break;
        }
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                    public Boolean create_permission(){  return SecurityController.getPerson().has_permission("SingleLibrary_create"); }
    @JsonIgnore   @Transient                                    public Boolean read_permission()  {  return true; }
    @JsonProperty @Transient @ApiModelProperty(required = true) public Boolean edit_permission()  {  return SecurityController.getPerson().has_permission("SingleLibrary_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public Boolean delete_permission(){  return SecurityController.getPerson().has_permission("SingleLibrary_delete"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public Boolean update_permission(){  return SecurityController.getPerson().has_permission("SingleLibrary_update"); }


    public enum permissions{SingleLibrary_create, SingleLibrary_edit, SingleLibrary_update, SingleLibrary_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, SingleLibrary> find = new Finder<>(SingleLibrary.class);

}
