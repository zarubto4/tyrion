package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.project.global.Model_Product;
import utilities.Server;

import javax.persistence.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of SingleLibrary",
        value = "SingleLibrary")
public class Model_SingleLibrary extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @ApiModelProperty(required = true) public String id;
                         @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true) public String description;
                                                            @ApiModelProperty(required = true) public String library_name;

                                                                       @JsonIgnore @ManyToOne  public Model_Product product;

    @JsonIgnore @OneToMany(mappedBy="single_library", cascade=CascadeType.ALL) @OrderBy("azureLinkVersion DESC") public List<Model_VersionObject> version_objects = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL)   public List<Model_Processor> processors = new ArrayList<>();


    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String>  versions_id()        { List<String> l = new ArrayList<>();  for( Model_VersionObject m : version_objects)  l.add(m.id); return l;  }

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


    @JsonIgnore  public String azure_single_library_link;

    @JsonIgnore @Override public void save() {
        while(true){ // I need Unique Value

            this.id = UUID.randomUUID().toString();
            if( product != null ) this.azure_single_library_link = product.get_path() + "/libraries/"  + this.id;
            else  this.azure_single_library_link = "/libraries/"  + this.id;

            if (Model_SingleLibrary.find.byId(this.id) == null) break;
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

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                    public boolean create_permission(){  return Controller_Security.getPerson().has_permission("SingleLibrary_create"); }
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return true; }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  return Controller_Security.getPerson().has_permission("SingleLibrary_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return Controller_Security.getPerson().has_permission("SingleLibrary_delete"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission(){  return Controller_Security.getPerson().has_permission("SingleLibrary_update"); }


    public enum permissions{SingleLibrary_create, SingleLibrary_edit, SingleLibrary_update, SingleLibrary_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, Model_SingleLibrary> find = new Finder<>(Model_SingleLibrary.class);

}
