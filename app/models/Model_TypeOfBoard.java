package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.Server;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.logger.Logger;
import utilities.model.NamedModel;
import utilities.swagger.input.Swagger_CompilationLibrary;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@ApiModel(value = "TypeOfBoard", description = "Model of TypeOfBoard")
@Table(name="TypeOfBoard")
public class Model_TypeOfBoard extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_TypeOfBoard.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Column(unique=true) public String compiler_target_name;

    @JsonIgnore @ManyToOne public Model_Producer producer;
    @JsonIgnore @ManyToOne public Model_Processor processor;
                          public Boolean connectible_to_internet;

    @JsonIgnore @OneToOne  public Model_Blob picture;

    @JsonIgnore @OneToMany(mappedBy="type_of_board", cascade = CascadeType.ALL,  fetch = FetchType.LAZY) public List<Model_TypeOfBoard_Batch> batchs = new ArrayList<>();

    @JsonIgnore @OneToMany(mappedBy="type_of_board", cascade = CascadeType.ALL,        fetch = FetchType.LAZY)  public List<Model_Hardware> boards = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="type_of_board",                                   fetch = FetchType.LAZY)  public List<Model_CProgram> c_programs = new ArrayList<>();


    @JsonIgnore @OneToMany(mappedBy="type_of_board", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_BootLoader> boot_loaders = new ArrayList<>();

    @JsonIgnore @OneToOne (mappedBy="main_type_of_board", fetch = FetchType.LAZY) public Model_BootLoader main_boot_loader;

    @JsonIgnore @OneToOne(mappedBy="type_of_board_default", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public Model_CProgram main_c_program;
    @JsonIgnore @OneToOne(mappedBy="type_of_board_test",    cascade = CascadeType.ALL, fetch = FetchType.LAZY) public Model_CProgram test_program;


    @JsonIgnore @ManyToMany(mappedBy = "type_of_boards", fetch = FetchType.LAZY)  public List<Model_TypeOfBoardFeatures> features = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "type_of_boards", fetch = FetchType.LAZY)  public List<Model_Library> libraries = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_producer_id;
    @JsonIgnore @Transient @Cached private UUID cache_processor_id;
    @JsonIgnore @Transient @Cached public  String cache_picture_link;
    @JsonIgnore @Transient @Cached public  UUID cache_main_bootloader_id;

    @JsonIgnore @Transient @Cached public  UUID cache_main_c_program_version_id;    // Výchozí defaault firmware chache
    @JsonIgnore @Transient @Cached public  UUID cache_main_c_program_id;
    @JsonIgnore @Transient @Cached public  UUID cache_test_program_version_id;      // testovací firmware chache
    @JsonIgnore @Transient @Cached public  UUID cache_test_c_program_id;
    @JsonIgnore @Transient @Cached public  List<Swagger_CompilationLibrary> cache_library_list; // Záměrně není pole definované!
    @JsonIgnore @Transient @Cached public  List<UUID> cache_bootloaders_id; // Záměrně není pole definované!

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty public String producer_name() { return get_producer().name;}
    @JsonProperty public UUID producer_id() { return cache_producer_id != null ? cache_producer_id : get_producer().id;}
    @JsonProperty public String processor_name() { return get_processor().name;}
    @JsonProperty public UUID processor_id() { return cache_processor_id != null ? cache_processor_id : get_processor().id;}

    @JsonProperty
    public String picture_link() {
        try {

            if ( cache_picture_link == null) {
                Model_Blob file = Model_Blob.find.query().where().eq("type_of_board.id", id).select("id").findOne();
                if (file != null) cache_picture_link = file.get_file_path_for_direct_download();
            }

            return cache_picture_link;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty
    public List<Swagger_CompilationLibrary> supported_libraries() {
        return cache_library_list;
    }


    @JsonProperty
    public Model_BootLoader main_boot_loader() {
        try {

            if (cache_main_bootloader_id == null) {
                Model_BootLoader main = Model_BootLoader.find.query().where().eq("main_type_of_board.id", id).select("id").findOne();
                if (main == null) return null;
                cache_main_bootloader_id = main.id;
            }

            return Model_BootLoader.getById(cache_main_bootloader_id);

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(value = "accessible only for persons with permissions", required = false) @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Model_BootLoader> boot_loaders() {
        try {

            if (cache_bootloaders_id == null) {

                List<Model_BootLoader> bootLoaders = Model_BootLoader.find.query().where().eq("type_of_board.id", id).order().desc("name").select("id").findList();
                cache_bootloaders_id = new ArrayList<>();

                // Získání seznamu
                for (Model_BootLoader bootLoader : bootLoaders) {
                    cache_bootloaders_id.add(bootLoader.id);
                }
            }

            if (cache_bootloaders_id.isEmpty()) {
                return new ArrayList<>();
            }

            List<Model_BootLoader> bootLoaders = new ArrayList<>();

            for (UUID bootLoader_id : cache_bootloaders_id) {
                bootLoaders.add(Model_BootLoader.getById(bootLoader_id));
            }

            return bootLoaders;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonProperty @ApiModelProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public Model_CProgram main_c_program() {
        try {

            if (cache_main_c_program_id == null) {
                Model_CProgram c_program = Model_CProgram.find.query().where().eq("type_of_board_default.id", id).select("id").findOne();
                if (c_program == null) return null;
                cache_main_c_program_id = c_program.id;
            }

            return Model_CProgram.getById(cache_main_c_program_id);

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    // Záměrně - kvuli dokumentaci a přehledu v Becki - nemá žádný podstatný vliv než jen umožnit vypsat přehled
    @JsonProperty @ApiModelProperty(value = "accessible only for persons with permissions", required = false) @JsonInclude(JsonInclude.Include.NON_NULL)
    public Model_CProgram main_test_c_program() {
        try {

            if (!test_c_program_edit_permission()) return null;

            if (cache_test_c_program_id == null) {
                Model_CProgram c_program = Model_CProgram.find.query().where().eq("type_of_board_test.id", id).select("id").findOne();
                if (c_program == null) return null;
                cache_test_c_program_id = c_program.id;
            }

            return Model_CProgram.getById(cache_test_c_program_id);

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    // Záměrně - kvuli dokumentaci a přehledu v Becki - nemá žádný podstatný vliv než jen umožnit vypsat přehled
    @Transient @JsonProperty @ApiModelProperty(value = "accessible only for persons with permissions", required = false) @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Model_TypeOfBoard_Batch> batchs () {
        try {

            if (!test_c_program_edit_permission()) return null;
            return Model_TypeOfBoard_Batch.find.query().where().eq("type_of_board.id", this.id).eq("deleted", false).findList();

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }


/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore // Pouze Pro synchronizaci s GitHubem - musí obsahovat i smazané
    public List<Model_BootLoader> boot_loaders_get_for_github_include_removed() {
        return Model_BootLoader.find.query().where().eq("type_of_board.id",id).findList();
    }

    @JsonIgnore
    public Model_Producer get_producer() {
        try {

            if (cache_producer_id == null) {
                Model_Producer producer = Model_Producer.find.query().where().eq("type_of_boards.id", id).select("id").findOne();
                cache_producer_id = producer.id;
            }

            return Model_Producer.getById(cache_producer_id);

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public Model_Processor get_processor() {

        try {

            if (cache_processor_id == null) {
                Model_Processor processor = Model_Processor.find.query().where().eq("type_of_boards.id", id).select("id").findOne();
                cache_processor_id = processor.id;
            }

            return Model_Processor.getById(cache_processor_id);

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public Model_CProgram get_main_c_program() {

        try {

            if (cache_main_c_program_id == null) {
                Model_CProgram c_program = Model_CProgram.find.query().where().eq("type_of_board_default.id", id).select("id").findOne();
                cache_main_c_program_id = c_program.id;
            }

            if (cache_main_c_program_id != null) {

                return Model_CProgram.getById(cache_main_c_program_id);
            } else {
                logger.error("get_main_c_program: cache_main_c_program_id is null!");
                return null;
            }


        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void update() {

        logger.debug("update :: Update object value: {}",  this.id);

        cache.put(id, this);
        super.update();

    }

    @JsonIgnore @Override public boolean delete() {

        logger.debug("update :: Delete object Id: {} ", this.id);

        deleted = true;
        cache.remove(id);
        super.update();

        return false;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public CloudBlobContainer get_Container() {
        try {
            return Server.blobClient.getContainerReference("pictures");
        } catch (Exception e) {
            logger.internalServerError(e);
            throw new NullPointerException();
        }
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   public boolean create_permission() {  return BaseController.person().has_permission("TypeOfBoard_create"); }
    @JsonIgnore   public boolean read_permission()  {  return true; }
    @JsonProperty public boolean edit_permission()  {  return BaseController.person().has_permission("TypeOfBoard_edit");   }
    @JsonProperty public boolean update_permission()  {  return BaseController.person().has_permission("TypeOfBoard_update");   }
    @JsonProperty public boolean delete_permission() {  return BaseController.person().has_permission("TypeOfBoard_delete"); }
    @JsonProperty public boolean register_new_device_permission() { return BaseController.person().has_permission("TypeOfBoard_register_new_device"); }
    @JsonProperty public boolean bootloader_edit_permission() { return BaseController.person().has_permission("TypeOfBoard_bootloader"); }
    @JsonProperty public boolean default_c_program_edit_permission() { return BaseController.person().has_permission("TypeOfBoard_c_program_edit_permission"); }
    @JsonProperty public boolean test_c_program_edit_permission() { return BaseController.person().has_permission("TypeOfBoard_test_c_program_edit_permission"); }

    public enum Permission { TypeOfBoard_create, TypeOfBoard_edit, TypeOfBoard_update, TypeOfBoard_delete, TypeOfBoard_register_new_device, TypeOfBoard_bootloader,  TypeOfBoard_c_program_edit_permission, TypeOfBoard_test_c_program_edit_permission }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_TypeOfBoard.class, timeToIdle = 600)
    public static Cache<UUID, Model_TypeOfBoard> cache;

    public static Model_TypeOfBoard getById(String id) {
        return getById(UUID.fromString(id));
    }
    
    public static Model_TypeOfBoard getById(UUID id) {

        Model_TypeOfBoard typeOfBoard = cache.get(id);
        if (typeOfBoard == null) {

            typeOfBoard = Model_TypeOfBoard.find.byId(id);
            if (typeOfBoard == null) return null;

            cache.put(id, typeOfBoard);
        }

        return typeOfBoard;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_TypeOfBoard> find = new Finder<>(Model_TypeOfBoard.class);
}