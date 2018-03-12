package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.enums.ProgramType;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.models_update_echo.EchoHandler;
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

                List<UUID> ids =  Model_CProgramVersion.find.query().where().eq("c_program.id", id).eq("deleted", false).order().desc("created").findIds();

                // Získání seznamu
                for (UUID id : ids) {
                    cache_version_ids.add(id);
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
        super.save();

        // Call notification about project update
        if (project != null) new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_Project.class, project.id, project.id))).start();

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

    @JsonIgnore @Transient
    public String get_path() {

        // C_Program is Private registred under Project
        if (project != null) {
            return project.getPath() + "/c-programs/" + this.id;
        } else {

            if(get_project_id() == null) {
                logger.debug("save :: is a public Program");
                return "public-c-programs/" + this.id;
            }else {
               return get_project().getPath() + "/c-programs/" + this.id;
            }
        }
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.CProgram_create.name())) return;
        if(this.project == null) throw new Result_Error_PermissionDenied();
        this.project.check_update_permission();
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (_BaseController.person().has_permission("c_program_update_" + id)) _BaseController.person().valid_permission("c_program_update_" + id);
        if (_BaseController.person().has_permission(Permission.CProgram_update.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_CProgram.find.query().where().where().eq("project.participants.person.id", _BaseController.person().id ).where().eq("id", id).findCount() > 0) {
            _BaseController.person().cache_permission("c_program_update_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        _BaseController.person().cache_permission("c_program_update_" + id, false);
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
        if (_BaseController.person().has_permission("c_program_read_" + id)) _BaseController.person().valid_permission("c_program_read_" + id);
        if (_BaseController.person().has_permission(Permission.CProgram_read.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_CProgram.find.query().where().where().eq("project.participants.person.id", _BaseController.person().id ).eq("id", id).findCount() > 0) {
            _BaseController.person().cache_permission("c_program_read_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        _BaseController.person().cache_permission("read_" + id, false);
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (_BaseController.person().has_permission("c_program_delete_" + id)) _BaseController.person().valid_permission("c_program_delete_" + id);
        if (_BaseController.person().has_permission(Permission.CProgram_delete.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_CProgram.find.query().where().where().eq("project.participants.person.id", _BaseController.person().id ).where().eq("id", id).findCount() > 0) {
            _BaseController.person().cache_permission("c_program_delete_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        _BaseController.person().cache_permission("c_program_delete_" + id, false);
        throw new Result_Error_PermissionDenied();

    }

    @JsonProperty @Transient @ApiModelProperty(required = false, value = "Visible only for Administrator with Permission") @JsonInclude(JsonInclude.Include.NON_NULL) public Boolean community_publishing_permission()  {
        try {
            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if(_BaseController.person().has_permission(Permission.C_Program_community_publishing_permission.name())) return true;
            return null;
        } catch (Exception e){
            return null;
        }
    }

    public enum Permission { CProgram_create, CProgram_read, CProgram_update, CProgram_delete, C_Program_community_publishing_permission }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/
    
    @CacheField(Model_CProgram.class)
    public static Cache<UUID, Model_CProgram> cache;
    
    public static Model_CProgram getById(UUID id) throws _Base_Result_Exception  {

        Model_CProgram c_program = cache.get(id);
        if (c_program == null) {

            c_program = find.byId(id);
            if (c_program == null) throw new Result_Error_NotFound(Model_Product.class);

            cache.put(id, c_program);
        }
        // Check Permission
        if(c_program.its_person_operation()) {
            c_program.check_read_permission();
        }
        c_program.check_create_permission();
        return c_program;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    
    public static Finder<UUID, Model_CProgram> find = new Finder<>(Model_CProgram.class);
}

