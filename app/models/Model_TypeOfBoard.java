package models;

import com.avaje.ebean.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.Server;
import utilities.cache.helps_objects.TyrionCachedList;
import utilities.logger.Class_Logger;
import utilities.swagger.documentationClass.Swagger_CompilationLibrary;
import utilities.swagger.outboundClass.Swagger_C_Program_Version_Short_Detail;
import utilities.swagger.outboundClass.Swagger_C_program_Short_Detail;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;


@Entity
@ApiModel(value = "TypeOfBoard", description = "Model of TypeOfBoard")
@Table(name="TypeOfBoard")
public class Model_TypeOfBoard extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_TypeOfBoard.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/
                                @Id    public String id;
                  @Column(unique=true) public String name;
                  @Column(unique=true) public String compiler_target_name;
                                       public String revision;
    @Column(columnDefinition = "TEXT")  public String description;

    @JsonIgnore @ManyToOne public Model_Producer producer;
    @JsonIgnore @ManyToOne public Model_Processor processor;
                          public Boolean connectible_to_internet;

    @JsonIgnore @OneToOne  public Model_FileRecord picture;

    @JsonIgnore @OneToMany(mappedBy="type_of_board", cascade = CascadeType.ALL,  fetch = FetchType.LAZY) public List<Model_TypeOfBoard_Batch> batchs = new ArrayList<>();

    @JsonIgnore @OneToMany(mappedBy="type_of_board", cascade = CascadeType.ALL,        fetch = FetchType.LAZY)  public List<Model_Board> boards = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="type_of_board",                                   fetch = FetchType.LAZY)  public List<Model_CProgram> c_programs = new ArrayList<>();


    @JsonIgnore @OneToMany(mappedBy="type_of_board", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_BootLoader> boot_loaders = new ArrayList<>();

    @JsonIgnore @OneToOne (mappedBy="main_type_of_board", fetch = FetchType.LAZY) public Model_BootLoader main_boot_loader;

    @JsonIgnore @OneToOne(mappedBy="type_of_board_default", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public Model_CProgram main_c_program;
    @JsonIgnore @OneToOne(mappedBy="type_of_board_test",    cascade = CascadeType.ALL, fetch = FetchType.LAZY) public Model_CProgram test_program;


    @JsonIgnore @ManyToMany(mappedBy = "type_of_boards", fetch = FetchType.LAZY)  public List<Model_TypeOfBoardFeatures> features = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "type_of_boards", fetch = FetchType.LAZY)  public List<Model_Library> libraries = new ArrayList<>();

    @JsonIgnore public boolean removed_by_user;


/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @TyrionCachedList private String cache_value_producer_id;
    @JsonIgnore @Transient @TyrionCachedList private String cache_value_processor_id;
    @JsonIgnore @Transient @TyrionCachedList public  String cache_value_picture_link;
    @JsonIgnore @Transient @TyrionCachedList public  String cache_value_main_bootloader_id;

    @JsonIgnore @Transient @TyrionCachedList public  String cache_main_c_program_version_id;    // Výchozí defaault firmware chache
    @JsonIgnore @Transient @TyrionCachedList public  String cache_main_c_program_id;
    @JsonIgnore @Transient @TyrionCachedList public  String cache_test_program_version_id;      // testovací firmware chache
    @JsonIgnore @Transient @TyrionCachedList public  String cache_test_c_program_id;
    @JsonIgnore @Transient @TyrionCachedList public  List<Swagger_CompilationLibrary> cache_library_list; // Záměrně není pole definované!
    @JsonIgnore @Transient @TyrionCachedList public  List<String> cache_bootloaders_id; // Záměrně není pole definované!

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @Transient @JsonProperty public String producer_name(){ return get_producer().name;}
    @Transient @JsonProperty public String producer_id(){ return cache_value_producer_id  != null ? cache_value_producer_id : get_producer().id.toString();}
    @Transient @JsonProperty public String processor_name(){ return get_processor().processor_name;}
    @Transient @JsonProperty public String processor_id(){ return cache_value_processor_id  != null ? cache_value_processor_id : get_processor().id.toString();}

    @Transient @JsonProperty @TyrionCachedList
    public String picture_link(){
        try {

            if( cache_value_picture_link == null) {
                Model_FileRecord file = Model_FileRecord.find.where().eq("type_of_board.id", id).select("id").findUnique();
                if(file != null) cache_value_picture_link = file.get_file_path_for_direct_download();
            }

            return cache_value_picture_link;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @Transient
    public List<Swagger_CompilationLibrary> supported_libraries() {
        return cache_library_list;
    }


    @JsonProperty @Transient @TyrionCachedList
    public Model_BootLoader main_boot_loader(){
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

    @Transient @JsonProperty @ApiModelProperty(value = "accessible only for persons with permissions", required = false) @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Model_BootLoader> boot_loaders(){
        try {

            if (cache_bootloaders_id == null) {

                List<Model_BootLoader> bootLoaders = Model_BootLoader.find.where().eq("type_of_board.id", id).select("id").findList();
                cache_bootloaders_id = new ArrayList<>();

                // Získání seznamu
                for (Model_BootLoader bootLoader : bootLoaders) {
                    cache_bootloaders_id.add(bootLoader.id.toString());
                }
            }

            if (cache_bootloaders_id.isEmpty()) {
                return new ArrayList<>();
            }

            List<Model_BootLoader> bootLoaders = new ArrayList<>();

            for (String bootLoader_id : cache_bootloaders_id) {
                bootLoaders.add(Model_BootLoader.get_byId(bootLoader_id));
            }

            return bootLoaders;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @Transient @JsonProperty(required = false) @ApiModelProperty(required = false) @TyrionCachedList @JsonInclude(JsonInclude.Include.NON_NULL)
    public Model_CProgram main_c_program(){
        try {

            if (cache_main_c_program_id == null) {
                Model_CProgram c_program = Model_CProgram.find.where().eq("type_of_board_default.id", id).select("id").findUnique();
                if(c_program == null) return null;
                cache_main_c_program_id = c_program.id;
            }

            return Model_CProgram.get_byId(cache_main_c_program_id);

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    // Záměrně - kvuli dokumentaci a přehledu v Becki - nemá žádný podstatný vliv než jen umožnit vypsat přehled
    @Transient @JsonProperty @ApiModelProperty(value = "accessible only for persons with permissions", required = false) @JsonInclude(JsonInclude.Include.NON_NULL)
    public Model_CProgram main_test_c_program(){
        try {

            if(!test_c_program_edit_permission()) return null;

            if (cache_test_c_program_id == null) {
                Model_CProgram c_program = Model_CProgram.find.where().eq("type_of_board_test.id", id).select("id").findUnique();
                if(c_program == null) return null;
                cache_test_c_program_id = c_program.id;
            }

            return Model_CProgram.get_byId(cache_test_c_program_id);

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    // Záměrně - kvuli dokumentaci a přehledu v Becki - nemá žádný podstatný vliv než jen umožnit vypsat přehled
    @Transient @JsonProperty @ApiModelProperty(value = "accessible only for persons with permissions", required = false) @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Model_TypeOfBoard_Batch> batchs (){
        try {

            if(!test_c_program_edit_permission()) return null;
            return Model_TypeOfBoard_Batch.find.where().eq("type_of_board.id", this.id).eq("removed_by_user", false).findList();

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }


/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @Transient @JsonIgnore // Pouze Pro synchronizaci s GitHubem - musí obsahovat i smazané
    public List<Model_BootLoader> boot_loaders_get_for_github_include_removed(){
        return Model_BootLoader.find.where().eq("type_of_board.id",id).findList();
    }

    @JsonIgnore @TyrionCachedList
    public Model_Producer get_producer(){

        try {

            if(cache_value_producer_id == null){
                Model_Producer producer = Model_Producer.find.where().eq("type_of_boards.id", id).select("id").findUnique();
                cache_value_producer_id = producer.id.toString();
            }

            return Model_Producer.get_byId(cache_value_producer_id);

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore @TyrionCachedList
    public Model_Processor get_processor(){

        try {

            if(cache_value_processor_id == null){
                Model_Processor processor = Model_Processor.find.where().eq("type_of_boards.id", id).select("id").findUnique();
                cache_value_processor_id = processor.id.toString();
            }

            return Model_Processor.get_byId(cache_value_processor_id);

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore @Transient @TyrionCachedList
    public Model_CProgram get_main_c_program(){

        try {

            if(cache_main_c_program_id == null){
                Model_CProgram c_program = Model_CProgram.find.where().eq("type_of_board_default.id", id).select("id").findUnique();
                cache_main_c_program_id = c_program.id;
            }

            if(cache_main_c_program_id != null) {

                return Model_CProgram.get_byId(cache_main_c_program_id);
            }else {
                terminal_logger.error("get_main_c_program: cache_main_c_program_id is null!");
                return null;
            }


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

        cache.put(id, this);
        super.update();

    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("update :: Delete object Id: {} ", this.id);

        removed_by_user = true;
        cache.remove(id);
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
            terminal_logger.internalServerError(e);
            throw new NullPointerException();
        }
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient public boolean create_permission(){  return Controller_Security.get_person().has_permission("TypeOfBoard_create"); }
    @JsonIgnore   @Transient public boolean read_permission()  {  return true; }
    @JsonProperty @Transient public boolean edit_permission()  {  return Controller_Security.get_person().has_permission("TypeOfBoard_edit");   }
    @JsonProperty @Transient public boolean update_permission()  {  return Controller_Security.get_person().has_permission("TypeOfBoard_update");   }
    @JsonProperty @Transient public boolean delete_permission(){  return Controller_Security.get_person().has_permission("TypeOfBoard_delete"); }
    @JsonProperty @Transient public boolean register_new_device_permission(){ return Controller_Security.get_person().has_permission("TypeOfBoard_register_new_device"); }
    @JsonProperty @Transient public boolean bootloader_edit_permission(){ return Controller_Security.get_person().has_permission("TypeOfBoard_bootloader"); }
    @JsonProperty @Transient public boolean default_c_program_edit_permission(){ return Controller_Security.get_person().has_permission("TypeOfBoard_c_program_edit_permission"); }
    @JsonProperty @Transient public boolean test_c_program_edit_permission(){ return Controller_Security.get_person().has_permission("TypeOfBoard_test_c_program_edit_permission"); }

    public enum permissions{TypeOfBoard_create, TypeOfBoard_edit, TypeOfBoard_update, TypeOfBoard_delete, TypeOfBoard_register_new_device, TypeOfBoard_bootloader,  TypeOfBoard_c_program_edit_permission, TypeOfBoard_test_c_program_edit_permission}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE = Model_TypeOfBoard.class.getSimpleName();

    public static Cache<String, Model_TypeOfBoard> cache = null; // < ID, Model_VersionObject>

    @JsonIgnore
    public static Model_TypeOfBoard get_byId(String id) {

        if(id == null) return null;

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