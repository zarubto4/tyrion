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
@ApiModel(description = "Model of LibraryGroup",
        value = "LibraryGroup")
public class Model_LibraryGroup extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @ApiModelProperty(required = true) public String id;
                                                            @ApiModelProperty(required = true) public String group_name;
                         @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true) public String description;

                                                                       @JsonIgnore @ManyToOne  public Model_Product product;

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL) public List<Model_Processor> processors = new ArrayList<>();

    @JsonIgnore @OneToMany(mappedBy="library_group", cascade=CascadeType.ALL) @OrderBy("azureLinkVersion DESC") public List<Model_VersionObject> version_objects = new ArrayList<>();

    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String>   versions_id()      { List<String> l = new ArrayList<>();  for( Model_VersionObject m : version_objects)  l.add(m.id); return l;  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String>   processors_id()    { List<String> l = new ArrayList<>();  for( Model_Processor m      : processors)       l.add(m.id); return l;  }

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore  public String azure_library_group_link;

    @JsonIgnore @Override public void save() {
        while(true){ // I need Unique Value

            this.id = UUID.randomUUID().toString();
            if( product != null ) this.azure_library_group_link = product.get_path() + "/libraries/"  + this.id;
                            else  this.azure_library_group_link = "/libraries/"  + this.id;

            if (Model_LibraryGroup.find.byId(this.id) == null) break;
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
        return  azure_library_group_link;
    }


/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                    public boolean create_permission(){  return Controller_Security.getPerson().has_permission("LibraryGroup_create"); }
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return true; }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  return Controller_Security.getPerson().has_permission("LibraryGroup_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return Controller_Security.getPerson().has_permission("LibraryGroup_delete"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission(){  return Controller_Security.getPerson().has_permission("LibraryGroup_update"); }

    public enum permissions{LibraryGroup_create, LibraryGroup_edit, LibraryGroup_delete, LibraryGroup_update}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_LibraryGroup> find = new Finder<>(Model_LibraryGroup.class);

}
