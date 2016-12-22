package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.project.c_program.actualization.Model_CProgramUpdatePlan;
import utilities.Server;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of BootLoader",
        value = "BootLoader")
public class Model_BootLoader extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Id
    @ApiModelProperty(required = true)    public String id;

    @ApiModelProperty(required = true,
            dataType = "integer", readOnly = true,
            value = "UNIX time in ms",
            example = "1466163478925")    public Date date_of_create;

                                          public String name;
    @Column(columnDefinition = "TEXT")    public String description;
                                          public String version_identificator; // HW identifikator od kluků ve formátu 255.255.255 -> ex. 0.1.6 || 0.1.77
    @Column(columnDefinition = "TEXT")    public String changing_note;


    @JsonIgnore @OneToMany(mappedBy="bootloader",cascade=CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_CProgramUpdatePlan> c_program_update_plans = new ArrayList<>();

    @JsonIgnore  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)                public Model_TypeOfBoard type_of_board;
                                                                        @JsonIgnore  @OneToOne()   public Model_TypeOfBoard main_type_of_board;

    @JsonIgnore  @OneToMany(mappedBy="actual_boot_loader", cascade = CascadeType.ALL)              public List<Model_Board> boards  = new ArrayList<>();

                                                @JsonIgnore  @OneToOne(mappedBy = "boot_loader")   public Model_FileRecord file;


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ---------------------------------------------------------------------------------------------------------*/
    @JsonIgnore  private String azure_product_link;

    @JsonIgnore @Override public void save() {

        while(true){
            // I need Unique Value
            this.id = UUID.randomUUID().toString();
            this.azure_product_link = get_Container().getName() + "/" + this.id;
            if (Model_BootLoader.find.byId(this.id) == null) break;
        }
        super.save();

    }

    @JsonIgnore @Transient
    public CloudBlobContainer get_Container(){

        try {

            return Server.blobClient.getContainerReference("bootloaders"); // Jméno kontejneru

        }catch (Exception e){
            e.printStackTrace();
            throw new NullPointerException();
        }

    }

    @JsonIgnore @Transient
    public String get_path(){
        return  azure_product_link;
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient  @ApiModelProperty(required = true) public boolean create_permission(){  return Controller_Security.getPerson().has_permission("BootLoader_create");      }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean update_permission(){  return Controller_Security.getPerson().has_permission("BootLoader_update"); }
    @JsonIgnore   @Transient  @ApiModelProperty(required = true) public boolean read_permission()  {  return true; }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean edit_permission()  {  return Controller_Security.getPerson().has_permission("BootLoader_read"); }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean delete_permission(){  return Controller_Security.getPerson().has_permission("BootLoader_delete"); }

    public enum permissions{  BootLoader_create,  BootLoader_update, BootLoader_read ,  BootLoader_edit, BootLoader_delete; }


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_BootLoader> find = new Finder<>(Model_BootLoader.class);

}
