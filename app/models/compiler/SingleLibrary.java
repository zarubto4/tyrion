package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.project.global.Product;
import utilities.Server;

import javax.persistence.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class SingleLibrary  extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true) public String id;
                         @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true) public String description;
                                                            @ApiModelProperty(required = true) public String library_name;

                                                                       @JsonIgnore @ManyToOne  public Product product;

    @JsonIgnore @OneToMany(mappedBy="single_library", cascade=CascadeType.ALL) @OrderBy("azureLinkVersion DESC") public List<Version_Object> version_objects = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL)   public List<Processor> processors = new ArrayList<>();


    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String>  versions_id()        { List<String> l = new ArrayList<>();  for( Version_Object m : version_objects)  l.add(m.id); return l;  }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


    @JsonIgnore  public String azure_single_library_link;

    @JsonIgnore @Override public void save() {
        while(true){ // I need Unique Value

            if( product != null ) this.azure_single_library_link = product.get_path() + "/libraries/"  + UUID.randomUUID().toString();
            else  this.azure_single_library_link = "/libraries/"  + UUID.randomUUID().toString();

            if (SingleLibrary.find.where().eq("azure_single_library_link", azure_single_library_link ).findUnique() == null) break;
        }
        super.save();
    }

    @JsonIgnore @Transient
    public CloudBlobContainer get_Container() throws URISyntaxException, StorageException {
        if(product == null) return Server.blobClient.getContainerReference("libraries");
        else return product.get_Container();
    }


    @JsonIgnore @Transient
    public String get_path(){
        return  azure_single_library_link;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                    public boolean create_permission(){  return SecurityController.getPerson().has_permission("SingleLibrary_create"); }
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return true; }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  return SecurityController.getPerson().has_permission("SingleLibrary_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return SecurityController.getPerson().has_permission("SingleLibrary_delete"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission(){  return SecurityController.getPerson().has_permission("SingleLibrary_update"); }


    public enum permissions{SingleLibrary_create, SingleLibrary_edit, SingleLibrary_update, SingleLibrary_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, SingleLibrary> find = new Finder<>(SingleLibrary.class);

}
