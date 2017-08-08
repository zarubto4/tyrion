package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.Server;
import utilities.cache.helps_objects.TyrionCachedList;
import utilities.logger.Class_Logger;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@ApiModel(description = "Model of TypeOfBoard",
        value = "TypeOfBoard")
public class Model_TypeOfBoard extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_TypeOfBoard.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/
                                @Id    public String id;
                                       public String name;
    @JsonIgnore @Column(unique=true)   public String compiler_target_name;
                                       public String revision;
                       @JsonIgnore     public String azure_picture_link;
   @Column(columnDefinition = "TEXT")  public String description;

   @JsonIgnore @ManyToOne public Model_Producer producer;
   @JsonIgnore @ManyToOne public Model_Processor processor;
                          public Boolean connectible_to_internet;

   @JsonIgnore @OneToOne  public Model_FileRecord picture;


    @JsonIgnore @OneToMany(mappedBy="type_of_board", cascade = CascadeType.ALL,        fetch = FetchType.LAZY)  public List<Model_Board> boards = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="type_of_board",                                   fetch = FetchType.LAZY)  public List<Model_CProgram> c_programs = new ArrayList<>();

    @JsonIgnore @OneToMany(mappedBy="type_of_board", cascade = CascadeType.ALL,        fetch = FetchType.LAZY)  public List<Model_BootLoader> boot_loaders = new ArrayList<>();
    @JsonIgnore @OneToOne (mappedBy="main_type_of_board",                              fetch = FetchType.LAZY)  public Model_BootLoader main_boot_loader;
    @JsonIgnore @OneToOne(mappedBy="type_of_board_default", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public Model_CProgram version_scheme;

    @JsonIgnore @ManyToMany(mappedBy = "type_of_boards",                               fetch = FetchType.LAZY)  public List<Model_TypeOfBoardFeatures> features = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "type_of_boards",                               fetch = FetchType.LAZY)  public List<Model_Library> libraries = new ArrayList<>();

    @JsonIgnore              public boolean removed_by_user;


/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @TyrionCachedList private String cache_value_producer_id;
    @JsonIgnore @Transient @TyrionCachedList private String cache_value_picture_link;
    @JsonIgnore @Transient @TyrionCachedList public  String cache_value_main_bootloader_id;
    @JsonIgnore @Transient @TyrionCachedList public  String cache_main_c_program_id;            //TODO

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @ApiModelProperty(readOnly =true) @Transient @JsonProperty public String producer_name(){ return cache_value_producer_id  != null ? cache_value_producer_id : get_producer().name;}
    @ApiModelProperty(readOnly =true) @Transient @JsonProperty public String producer_id(){ return cache_value_producer_id  != null ? cache_value_producer_id : get_producer().id;}

    @ApiModelProperty(readOnly =true) @Transient @JsonProperty public String target_name(){ return compiler_target_name;}
    @ApiModelProperty(required =true) @Transient @JsonProperty public String picture_link(){

        try {

            if( cache_value_picture_link == null) {

                if (this.azure_picture_link == null) {
                    return null;
                }

                terminal_logger.debug("picture_link :: {}{}", Server.azure_blob_Link, azure_picture_link);
                cache_value_picture_link = Server.azure_blob_Link + azure_picture_link;
            }

            return cache_value_picture_link;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }


    @ApiModelProperty(required =true) @Transient @JsonProperty public Model_BootLoader main_boot_loader(){

        try {

            if (cache_value_main_bootloader_id == null) {
                Model_BootLoader main = Model_BootLoader.find.where().eq("main_type_of_board.id", id).select("id").findUnique();
                if(main == null) return null;
                cache_value_main_bootloader_id = main.id.toString();
            }

            return Model_BootLoader.get_byId(cache_value_main_bootloader_id);

        }catch (Exception e){
             terminal_logger.internalServerError(e);
            return null;
        }
    }


/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @TyrionCachedList
    public Model_Producer get_producer(){

        try {

            if(cache_value_producer_id == null){
                Model_Producer producer = Model_Producer.find.where().eq("type_of_boards.id", id).select("id").findUnique();
                cache_value_producer_id = producer.id;
            }

            return Model_Producer.get_byId(cache_value_producer_id);

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore @TyrionCachedList
    public Model_CProgram get_main_c_program(){

        try {

            if(cache_main_c_program_id == null){
                Model_CProgram c_program = Model_CProgram.find.where().eq("type_of_board_default.id", id).select("id").findUnique();
                cache_main_c_program_id = c_program.id;
            }

            return Model_CProgram.get_byId(cache_main_c_program_id);

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");
        removed_by_user  = false;

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_TypeOfBoard.find.byId(this.id) == null) break;
        }
        super.save();
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object value: {}",  this.id);

        super.update();

    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("update :: Delete object Id: {} ", this.id);

        removed_by_user = true;
        super.update();
    }


/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public CloudBlobContainer get_Container(){
        try {
            return Server.blobClient.getContainerReference("pictures");
        }catch (Exception e){
            terminal_logger.internalServerError("get_Container", e);
            throw new NullPointerException();
        }
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient public boolean create_permission(){  return Controller_Security.get_person().permissions_keys.containsKey("TypeOfBoard_create"); }
    @JsonIgnore   @Transient public boolean read_permission()  {  return true; }
    @JsonProperty @Transient public boolean edit_permission()  {  return Controller_Security.get_person().permissions_keys.containsKey("TypeOfBoard_edit");   }
    @JsonProperty @Transient public boolean delete_permission(){  return Controller_Security.get_person().permissions_keys.containsKey("TypeOfBoard_delete"); }
    @JsonProperty @Transient public boolean register_new_device_permission(){ return Controller_Security.get_person().permissions_keys.containsKey("TypeOfBoard_register_new_device"); }

    public enum permissions{TypeOfBoard_create, TypeOfBoard_edit, TypeOfBoard_delete, TypeOfBoard_register_new_device}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE = Model_TypeOfBoard.class.getSimpleName();

    public static Cache<String, Model_TypeOfBoard> cache = null; // < ID, Model_VersionObject>

    @JsonIgnore
    public static Model_TypeOfBoard get_byId(String id) {

        Model_TypeOfBoard typeOfBoard= cache.get(id);
        if (typeOfBoard == null){

            typeOfBoard = Model_TypeOfBoard.find.byId(id);
            if (typeOfBoard == null) return null;

            cache.put(id, typeOfBoard);
        }

        return typeOfBoard;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static  Model.Finder<String, Model_TypeOfBoard> find = new Finder<>(Model_TypeOfBoard.class);
}