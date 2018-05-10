package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.Server;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.NamedModel;
import utilities.swagger.input.Swagger_CompilationLibrary;
import utilities.swagger.output.Swagger_Short_Reference;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@ApiModel(value = "HardwareType", description = "Model of HardwareType")
@Table(name="HardwareType")
public class Model_HardwareType extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_HardwareType.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Column(unique=true) public String compiler_target_name;

    @JsonIgnore @ManyToOne public Model_Producer producer;
    @JsonIgnore @ManyToOne public Model_Processor processor;
                          public Boolean connectible_to_internet;

    @JsonIgnore @OneToOne  public Model_Blob picture;


    @JsonIgnore @OneToMany(mappedBy="hardware_type", cascade = CascadeType.ALL,        fetch = FetchType.LAZY)  public List<Model_Hardware> hardware = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="hardware_type",                                   fetch = FetchType.LAZY)  public List<Model_CProgram> c_programs = new ArrayList<>();


    @JsonIgnore @OneToMany(mappedBy="hardware_type", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_BootLoader> boot_loaders = new ArrayList<>();

    @JsonIgnore @OneToOne (mappedBy="main_hardware_type", fetch = FetchType.LAZY) public Model_BootLoader main_boot_loader;

    @JsonIgnore @OneToOne(mappedBy="hardware_type_default", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public Model_CProgram main_c_program;
    @JsonIgnore @OneToOne(mappedBy="hardware_type_test",    cascade = CascadeType.ALL, fetch = FetchType.LAZY) public Model_CProgram test_program;


    @ManyToMany(mappedBy = "hardware_types", fetch = FetchType.LAZY)  public List<Model_HardwareFeature> features = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "hardware_types", fetch = FetchType.LAZY)  public List<Model_Library> libraries = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached public String cache_picture_link; // Není klasický Cache objekt!!!
    @JsonIgnore @Transient @Cached public List<Swagger_CompilationLibrary> cache_library_list; // Není klasický Cache objekt nejde standartně cachovat!!!

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(value = "Only if user have permission for this object")
    public Swagger_Short_Reference producer() {
        try {
            Model_Producer type = this.get_producer();
            return new Swagger_Short_Reference(type.id, type.name, type.description);
        } catch (_Base_Result_Exception e) {
            // nothing
            return null;
        } catch (Exception e) {
            this.logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty
    public Swagger_Short_Reference processor() {
        try {
            Model_Processor type = this.get_processor();
            return new Swagger_Short_Reference(type.id, type.name, type.description);

        } catch (_Base_Result_Exception e) {
            // nothing
            return null;
        } catch (NullPointerException e) {
           this.logger.internalServerError(e);
           return null;
        }
    }

    @JsonProperty
    public String picture_link() {
        try {

            if ( cache_picture_link == null) {
                Model_Blob file = Model_Blob.find.query().where().eq("hardware_type.id", id).select("id").findOne();
                if (file != null) cache_picture_link = file.get_file_path_for_direct_download();
            }

            return cache_picture_link;

        } catch (_Base_Result_Exception e) {
            // nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty
    public List<Swagger_CompilationLibrary> supported_libraries() {
       try {


        return cache_library_list;
    }  catch (_Base_Result_Exception e) {
        // nothing
        return null;
    } catch (Exception e) {
        logger.internalServerError(e);
        return null;
        }
    }

    @JsonProperty
    public Model_BootLoader main_boot_loader() {
            try {
                return get_main_boot_loader();

            } catch(_Base_Result_Exception e){
                //nothing
                return null;
            } catch (Exception e) {
                logger.internalServerError(e);
                return null;
            }
    }


    @JsonProperty @ApiModelProperty(value = "accessible only for persons with permissions", required = false) @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Model_BootLoader> boot_loaders() {
        try {

            return get_bootloaders();

        } catch(_Base_Result_Exception e){
            //nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }


    @JsonProperty @ApiModelProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public Model_CProgram main_c_program() {
        try {
            return get_main_c_program();

        } catch (_Base_Result_Exception e){
            //nothing
            return null;
        } catch (Exception e) {
            return null;
        }

    }

    @JsonProperty @ApiModelProperty(value = "accessible only for persons with permissions", required = false) @JsonInclude(JsonInclude.Include.NON_NULL)
    public Model_CProgram main_test_c_program() {
            try {
                return get_main_test_c_program();

            } catch(_Base_Result_Exception e){
               //nothing
               return null;
            } catch (Exception e) {
                logger.internalServerError(e);
                return null;
            }
    }

    // Záměrně - kvuli dokumentaci a přehledu v Becki - nemá žádný podstatný vliv než jen umožnit vypsat přehled
    @JsonProperty @ApiModelProperty(value = "accessible only for persons with permissions", required = false) @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Model_HardwareBatch> batchs () {
        try {

            if (test_c_program_edit_permission()) {
                return Model_HardwareBatch.getByTypeOfBoardId(this.compiler_target_name);
            }

            return null;

        }catch (NullPointerException e) {
            //nothing
            return null;
        } catch(_Base_Result_Exception e){
            //nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }


/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore // Pouze Pro synchronizaci s GitHubem - musí obsahovat i smazané
    public List<Model_BootLoader> boot_loaders_get_for_github_include_removed() {
        return Model_BootLoader.find.query().where().eq("hardware_type.id",id).findList();
    }

    @JsonIgnore // Pouze Pro synchronizaci s GitHubem - musí obsahovat i smazané
    public List<UUID> get_bootloaders_id() {

        if (cache().gets(Model_BootLoader.class) == null) {
            cache().add(Model_BootLoader.class,  Model_BootLoader.find.query().where().eq("hardware_type.id", id).order().desc("name").select("id").findSingleAttributeList());
        }

        return cache().gets(Model_BootLoader.class);
    }

    @JsonIgnore
    public List<Model_BootLoader> get_bootloaders() {
        try {

            List<Model_BootLoader> list = new ArrayList<>();

            for (UUID id : get_bootloaders_id() ) {
                list.add(Model_BootLoader.getById(id));
            }

            return list;
        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public UUID get_producer_id() throws _Base_Result_Exception {

        if (cache().get(Model_Producer.class) == null) {
            cache().add(Model_Producer.class, (UUID) Model_Producer.find.query().where().eq("hardware_types.id", id).select("id").findSingleAttribute());
        }

        return cache().get(Model_Producer.class);
    }

    @JsonIgnore
    public Model_Producer get_producer() {

        try {
            return Model_Producer.getById(get_producer_id());
        } catch (Exception e) {
            return null;
        }
    }

    @JsonIgnore
    public UUID get_processor_id() throws _Base_Result_Exception {

        if (cache().get(Model_Processor.class) == null) {
            cache().add(Model_Processor.class, (UUID) Model_Processor.find.query().where().eq("hardware_types.id", id).select("id").findSingleAttribute());
        }

        return cache().get(Model_Processor.class);
    }

    @JsonIgnore
    public Model_Processor get_processor() {

        try {
            return Model_Processor.getById(get_processor_id());
        } catch (Exception e) {
            return null;
        }
    }

    @JsonIgnore
    public UUID get_main_test_c_program_id() throws _Base_Result_Exception {

        if (cache().get(Model_CProgram_Test_FakeClass.class) == null) {
            cache().add(Model_CProgram_Test_FakeClass.class, (UUID) Model_CProgram.find.query().where().eq("hardware_type_test.id", id).orderBy("UPPER(name) ASC").select("id").findSingleAttribute());
        }

        return cache().get(Model_CProgram_Test_FakeClass.class);
    }

    @JsonIgnore
    public Model_CProgram get_main_test_c_program() {
        try {
            return Model_CProgram.getById(get_main_test_c_program_id());
        } catch (Exception e) {
            return null;
        }
    }


    @JsonIgnore
    public UUID get_main_boot_loader_id() throws _Base_Result_Exception {
        if (cache().get(Model_BootLoader.class) == null) {
            cache().add(Model_BootLoader.class, (UUID) Model_BootLoader.find.query().where().eq("main_hardware_type.id", id).select("id").findSingleAttribute());
        }
        return cache().get(Model_BootLoader.class);
    }

    @JsonIgnore
    public Model_BootLoader get_main_boot_loader() throws _Base_Result_Exception {
        try {
            return Model_BootLoader.getById(get_main_boot_loader_id());
        } catch (Exception e) {
            return null;
        }
    }

    @JsonIgnore
    public UUID get_main_c_program_id() throws _Base_Result_Exception {

        if (cache().get(Model_CProgram.class) == null) {
            cache().add(Model_CProgram.class, (UUID) Model_CProgram.find.query().where().eq("hardware_type_default.id", id).select("id").findSingleAttribute());
        }

        return cache().get(Model_CProgram.class);
    }

    @JsonIgnore
    public Model_CProgram get_main_c_program() {

        try {
            return Model_CProgram.getById(get_main_c_program_id());
        } catch (Exception e) {
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

/* HELPER CLASS  ----------------------------------------------------------------------------------------------------------*/

    // Používáme protože nemáme rezervní klíč pro cachoání backup c program verze v lokální chache
    public abstract class Model_CProgram_Test_FakeClass {}

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.HardwareType_create.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception {}
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.HardwareType_update.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.HardwareType_delete.name())) return;
        throw new Result_Error_PermissionDenied();
    }

    @JsonIgnore @Transient public void check_register_new_device_permission() throws _Base_Result_Exception  {
        if(_BaseController.person().has_permission(Permission.HardwareType_register_new_device.name())) return;
        throw new Result_Error_PermissionDenied();
    }

    @JsonProperty @ApiModelProperty("Visible only for Administrator with Special Permission")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean bootloader_edit_permission() throws _Base_Result_Exception {
        try {
            if (_BaseController.person().has_permission(Permission.HardwareType_bootloader.name())) return true;
            return null;
        } catch (_Base_Result_Exception e){
            return null;
        }
    }

    @JsonProperty @ApiModelProperty("Visible only for Administrator with Special Permission")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean default_c_program_edit_permission() throws _Base_Result_Exception {
        try {
            if (_BaseController.person().has_permission(Permission.HardwareType_c_program_edit_permission.name())) return true;
            return null;
        } catch (_Base_Result_Exception e){
            return null;
        }
    }

    @JsonProperty @ApiModelProperty("Visible only for Administrator with Special Permission")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean test_c_program_edit_permission() {
        try {
            if (_BaseController.person().has_permission(Permission.HardwareType_test_c_program_edit_permission.name())) return true;
            return null;
        } catch (_Base_Result_Exception e){
            return null;
        }
    }

    @JsonProperty @ApiModelProperty("Visible only for Administrator with Special Permission")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean batch_register_permission() {
        try {
            if (_BaseController.person().has_permission(Permission.HardwareType_test_c_program_edit_permission.name())) return true;
            return null;
        } catch (_Base_Result_Exception e){
            return null;
        }
    }




    public enum Permission { HardwareType_create, HardwareType_edit, HardwareType_update, HardwareType_delete, HardwareType_register_new_device, HardwareType_bootloader,  HardwareType_c_program_edit_permission, HardwareType_test_c_program_edit_permission }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(Model_HardwareType.class)
    public static Cache<UUID, Model_HardwareType> cache;

    public static Model_HardwareType getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }
    
    public static Model_HardwareType getById(UUID id) throws _Base_Result_Exception {

        Model_HardwareType hardwareType = cache.get(id);
        if (hardwareType == null) {

            hardwareType = Model_HardwareType.find.byId(id);
            if (hardwareType == null) throw new Result_Error_NotFound(Model_HardwareType.class);

            cache.put(id, hardwareType);
        }
        // Check Permission
        if(hardwareType.its_person_operation()) {
            hardwareType.check_read_permission();
        }

        return hardwareType;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_HardwareType> find = new Finder<>(Model_HardwareType.class);
}