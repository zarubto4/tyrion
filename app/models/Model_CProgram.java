package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.libs.Json;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.enums.ProgramType;
import utilities.enums.CompilationStatus;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.models_update_echo.EchoHandler;
import utilities.swagger.input.Swagger_C_Program_Version_New;
import utilities.swagger.input.Swagger_Library_Library_Version_pair;
import utilities.swagger.output.Swagger_C_Program_Version;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Entity
@ApiModel(value="C_Program", description="Object represented C_Program in database")
@Table(name="CProgram")
public class Model_CProgram extends TaggedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_CProgram.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY) public Model_Project project;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST) public Model_HardwareType hardware_type;
                                                                                  public ProgramType publish_type;

    @JsonIgnore @OneToMany(mappedBy="c_program", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  private List<Model_CProgramVersion> versions = new ArrayList<>();

    @JsonIgnore @OneToOne(fetch = FetchType.LAZY) public Model_HardwareType hardware_type_default;  // Vazba pokud tento C_Program je výchozí program desky
    @JsonIgnore @OneToOne(fetch = FetchType.LAZY) public Model_HardwareType hardware_type_test;     // Vaza pokud je tento C Program výchozím testovacím programem desky

    @JsonIgnore @OneToOne(mappedBy = "default_program", cascade = CascadeType.ALL) public Model_CProgramVersion default_main_version;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                                 public Model_LibraryVersion example_library;          // Program je příklad pro použití knihovny

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached public List<UUID> cache_version_ids = new ArrayList<>();
    @JsonIgnore @Transient @Cached private UUID cache_hardware_type_id;
    @JsonIgnore @Transient @Cached private String cache_hardware_type_name;
    @JsonIgnore @Transient @Cached private UUID cache_project_id;
    @JsonIgnore @Transient @Cached private String cache_project_name;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/


    @JsonProperty public UUID hardware_type_id()     {
        try {

            if (cache_hardware_type_id == null) {
                Model_HardwareType hardwareType = Model_HardwareType.find.query().where().eq("c_programs.id", id).select("id").findOne();
                cache_hardware_type_id = hardwareType.id;
            }

            return cache_hardware_type_id;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }
    @JsonProperty public String hardware_type_name()   {
        try {

            if (cache_hardware_type_name == null) {
                cache_hardware_type_name = Model_HardwareType.getById(hardware_type_id()).name;
            }

            return cache_hardware_type_name;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }

    }
    @JsonProperty public List<Model_CProgramVersion> program_versions() {
        try {

            List<Model_CProgramVersion> versions = new ArrayList<>();

            for (Model_CProgramVersion version : get_versions().stream().sorted((element1, element2) -> element2.created.compareTo(element1.created)).collect(Collectors.toList())) {
                versions.add(version);
            }

            return versions;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }


/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public UUID get_project_id() throws _Base_Result_Exception  {

        if (cache_project_id == null) {
            Model_Project project = Model_Project.find.query().where().eq("c_programs.id", id).select("id").findOne();
            if (project == null) return null;
            cache_project_id = project.id;
        }

        return cache_project_id;
    }

    @JsonIgnore @Transient public Model_Project get_project() throws _Base_Result_Exception  {
        return  Model_Project.getById(get_project_id());
    }

    @JsonIgnore @Transient public List<Model_CProgramVersion> get_versions() {

        try {

            if (cache_version_ids.isEmpty()) {

                List<Model_CProgramVersion> versions =  Model_CProgramVersion.find.query().where().eq("c_program.id", id).eq("deleted", false).order().desc("created").select("id").findList();

                // Získání seznamu
                for (Model_CProgramVersion version : versions) {
                    cache_version_ids.add(version.id);
                }
            }

            List<Model_CProgramVersion> versions  = new ArrayList<>();

            for (UUID version_id : cache_version_ids) {
                versions.add(Model_CProgramVersion.getById(version_id));
            }

            return versions;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore @Transient public List<Model_CProgramVersion> get_version_objects_all_For_Admin() {
        return Model_CProgramVersion.find.query().where().eq("c_program.id", id).order().desc("created").findList();
    }



    @JsonIgnore @Transient public Model_HardwareType get_hardware_type() {
        if (hardware_type_id() == null) return null;
        return Model_HardwareType.getById(hardware_type_id());
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        logger.debug("save :: Creating new Object");

        // C_Program is Private registred under Project
        if (project != null) {
            logger.debug("save :: is a private Program");
            this.azure_c_program_link = project.getPath() + "/c-programs/" + this.id;

        } else {
            // C_Program is public C_Program for every users
            logger.debug("save :: is a public Program");
            this.azure_c_program_link = "public-c-programs/"  + this.id;
        }

        super.save();

        // Call notification about project update
        if (project != null) new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_Project.class, project.id, project.id))).start();

        if (project != null) {
            project.cache_c_program_ids.add(id);
        }

        cache.put(id, this);
    }

    @JsonIgnore @Override public void update() {

        logger.debug("update :: Update object Id: {}",  this.id);

        // Call notification about model update
        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo( Model_CProgram.class, get_project_id(), this.id));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

        super.update();

        cache.put(id, this);
    }

    @JsonIgnore @Override public boolean delete() {

        logger.debug("update :: Delete object Id: {} ", this.id);
        super.delete();

        // Remove from Project Cache
        try {
            get_project().cache_c_program_ids.remove(id);
        } catch (_Base_Result_Exception e) {
            // Nothing
        }


        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo( Model_Project.class, get_project_id(), get_project_id()));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

        
        return false;
    }


    @JsonIgnore @Override public void refresh() {
        logger.debug("update :: Delete object Id: {} ", this.id);

        this.cache_version_ids.clear();
        cache.remove(id);
        super.refresh();

    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BlOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore private String azure_c_program_link; // Link, který je náhodně generovaný pro Azure - a který se připojuje do cesty souborům

    @JsonIgnore @Transient
    public String get_path() {

        return  azure_c_program_link;
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if(BaseController.person().has_permission(Permission.CProgram_create.name())) return;
        if(this.project == null) throw new Result_Error_PermissionDenied();
        this.project.check_update_permission();
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("c_program_update_" + id)) BaseController.person().valid_permission("c_program_update_" + id);
        if (BaseController.person().has_permission(Permission.CProgram_update.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_CProgram.find.query().where().where().eq("project.participants.person.id", BaseController.person().id ).where().eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("c_program_update_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("c_program_update_" + id, false);
        throw new Result_Error_PermissionDenied();

    }
    @JsonIgnore @Transient @Override public void check_read_permission() throws _Base_Result_Exception {

        try{
            // Object project not exist so its public program, and user not need read permission
            get_project();
        }catch (_Base_Result_Exception exception) {
            return;
        }


        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("c_program_read_" + id)) BaseController.person().valid_permission("c_program_read_" + id);
        if (BaseController.person().has_permission(Permission.CProgram_read.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_CProgram.find.query().where().where().eq("project.participants.person.id", BaseController.person().id ).eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("c_program_read_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("read_" + id, false);
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("c_program_delete_" + id)) BaseController.person().valid_permission("c_program_delete_" + id);
        if (BaseController.person().has_permission(Permission.CProgram_delete.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_CProgram.find.query().where().where().eq("project.participants.person.id", BaseController.person().id ).where().eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("c_program_delete_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("c_program_delete_" + id, false);
        throw new Result_Error_PermissionDenied();

    }

    @JsonProperty @Transient @ApiModelProperty(required = false, value = "Visible only for Administrator with Permission") @JsonInclude(JsonInclude.Include.NON_NULL) public Boolean community_publishing_permission()  {
        try {
            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if(BaseController.person().has_permission(Permission.C_Program_community_publishing_permission.name())) return true;
            return null;
        } catch (Exception e){
            return null;
        }
    }

    public enum Permission { CProgram_create, CProgram_read, CProgram_update, CProgram_delete, C_Program_community_publishing_permission }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/
    
    @CacheField(Model_CProgram.class)
    public static Cache<UUID, Model_CProgram> cache;

    public static Model_CProgram getById(String id) throws _Base_Result_Exception  {
        return getById(UUID.fromString(id));
    }
    
    public static Model_CProgram getById(UUID id) throws _Base_Result_Exception  {

        Model_CProgram c_program = cache.get(id);
        if (c_program == null) {

            c_program = Model_CProgram.find.byId(id);
            if (c_program == null) throw new Result_Error_NotFound(Model_Product.class);

            cache.put(id, c_program);
        }

        return c_program;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    
    public static Finder<UUID, Model_CProgram> find = new Finder<>(Model_CProgram.class);
}

