package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.project.c_program.actualization.C_Program_Update_Plan;
import utilities.Server;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class BootLoader extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true)  public String id;

    @ApiModelProperty(required = true,
            dataType = "integer", readOnly = true,
            value = "UNIX time in milis - Date: number of miliseconds elapsed since  Thursday, 1 January 1970",
            example = "1466163478925")    public Date date_of_create;

                                          public String name;
    @Column(columnDefinition = "TEXT")    public String description;
                                          public String version_identificator; // HW identifikator od kluků ve formátu 255.255.255 -> ex. 0.1.6 || 0.1.77
    @Column(columnDefinition = "TEXT")    public String changing_note;


    @JsonIgnore @OneToMany(mappedBy="bootloader",cascade=CascadeType.ALL, fetch = FetchType.LAZY)  public List<C_Program_Update_Plan> c_program_update_plans = new ArrayList<>();


    @JsonIgnore  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST) public TypeOfBoard type_of_board;
    @JsonIgnore  @OneToOne()                                                        public TypeOfBoard main_type_of_board;

    @JsonIgnore  @OneToMany(mappedBy="actual_boot_loader", cascade = CascadeType.ALL) public List<Board> boards  = new ArrayList<>();

    @JsonIgnore  @OneToOne(mappedBy = "boot_loader")  public FileRecord file;


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ---------------------------------------------------------------------------------------------------------*/
    @JsonIgnore  private String azure_product_link;

    @JsonIgnore @Override public void save() {

        while(true){
            // I need Unique Value
            this.azure_product_link = get_Container().getName() + "/" + UUID.randomUUID().toString();
            if (BootLoader.find.where().eq("azure_product_link", azure_product_link ).findUnique() == null) break;
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

    @JsonIgnore   @Transient  @ApiModelProperty(required = true) public boolean create_permission(){  return SecurityController.getPerson().has_permission("BootLoader_create");      }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean update_permission(){  return SecurityController.getPerson().has_permission("BootLoader_update"); }
    @JsonIgnore   @Transient  @ApiModelProperty(required = true) public boolean read_permission()  {  return true; }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean edit_permission()  {  return SecurityController.getPerson().has_permission("BootLoader_read"); }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean delete_permission(){  return SecurityController.getPerson().has_permission("BootLoader_delete"); }

    public enum permissions{  BootLoader_create,  BootLoader_update, BootLoader_read ,  BootLoader_edit, BootLoader_delete; }


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,BootLoader> find = new Finder<>(BootLoader.class);

}
