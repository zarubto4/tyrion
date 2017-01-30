package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.project.c_program.Model_CProgram;
import utilities.Server;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@ApiModel(description = "Model of TypeOfBoard",
        value = "TypeOfBoard")
public class Model_TypeOfBoard extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/
                                                         @Id @ApiModelProperty(required = true) public String id;
                                                             @ApiModelProperty(required = true) public String name;
                                                             @Column(unique=true)  @JsonIgnore  public String compiler_target_name;
                                                             @ApiModelProperty(required = true) public String revision;
                                                                                    @JsonIgnore public String azure_picture_link;

                       @Column(columnDefinition = "TEXT")    @ApiModelProperty(required = true) public String    description;
                                                                        @JsonIgnore  @ManyToOne public Model_Producer producer;
                                                                        @JsonIgnore  @ManyToOne public Model_Processor processor;
                                                             @ApiModelProperty(required = true) public Boolean   connectible_to_internet;
                                                                          @JsonIgnore @OneToOne public Model_FileRecord picture;

    @JsonIgnore @OneToMany(mappedBy="type_of_board", cascade = CascadeType.ALL)                 public List<Model_Board>       boards      = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="type_of_board")                                            public List<Model_CProgram>   c_programs  = new ArrayList<>();

    @JsonIgnore @OneToMany(mappedBy="type_of_board", cascade = CascadeType.ALL)                 public List<Model_BootLoader>  boot_loaders = new ArrayList<>();
                @OneToOne (mappedBy="main_type_of_board")                                       public Model_BootLoader main_boot_loader;
    @JsonIgnore @OneToOne(mappedBy="default_program_type_of_board")                             public Model_CProgram default_program;

    @JsonIgnore @ManyToMany(mappedBy = "type_of_boards",fetch = FetchType.LAZY)                 public List<Model_ImportLibrary> libraries = new ArrayList<>();

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(readOnly =true) @Transient @JsonProperty public String processor_name    (){ return processor == null ? null : processor.processor_name;}
    @ApiModelProperty(readOnly =true) @Transient @JsonProperty public String processor_id      (){ return processor == null ? null : processor.id;}

    @ApiModelProperty(readOnly =true) @Transient @JsonProperty public String producer_name     (){ return producer  == null ? null : producer.name;}
    @ApiModelProperty(readOnly =true) @Transient @JsonProperty public String producer_id       (){ return producer  == null ? null : producer.id;}

    @ApiModelProperty(readOnly =true) @Transient @JsonProperty public String target_name       (){ return compiler_target_name;}

    @JsonProperty @ApiModelProperty(required = true)
    public String picture_link(){
        if(this.azure_picture_link == null){ return null;}
        return Server.azureLink + azure_picture_link;
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_TypeOfBoard.find.byId(this.id) == null) break;
        }
        super.save();
    }

    @JsonIgnore @Override public void delete() {

        for (Model_CProgram c_program : this.c_programs){
            c_program.type_of_board = null;
            c_program.update();
        }

        if(default_program != null) this.default_program.delete();

        this.processor = null;
        this.producer = null;
        this.update();

        super.delete();
    }

    @JsonIgnore @Transient
    public CloudBlobContainer get_Container(){
        try {
            return Server.blobClient.getContainerReference("pictures");
        }catch (Exception e){
            e.printStackTrace();
            throw new NullPointerException();
        }
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                    public boolean create_permission(){  return Controller_Security.getPerson().has_permission("TypeOfBoard_create"); }
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return true; }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  return Controller_Security.getPerson().has_permission("TypeOfBoard_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return Controller_Security.getPerson().has_permission("TypeOfBoard_delete"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean register_new_device_permission(){ return Controller_Security.getPerson().has_permission("TypeOfBoard_register_new_device"); }

    public enum permissions{TypeOfBoard_create, TypeOfBoard_edit, TypeOfBoard_delete, TypeOfBoard_register_new_device}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static  Model.Finder<String, Model_TypeOfBoard> find = new Finder<>(Model_TypeOfBoard.class);


}
