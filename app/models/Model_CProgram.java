package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.enums.ProgramType;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.models_update_echo.EchoHandler;
import utilities.swagger.output.Swagger_Short_Reference;
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
    @JsonIgnore @OneToOne(fetch = FetchType.LAZY) public Model_HardwareType hardware_type_test;     // Vazba pokud je tento C Program výchozím testovacím programem desky

    @JsonIgnore @OneToOne(mappedBy = "default_program", cascade = CascadeType.ALL) public Model_CProgramVersion default_main_version;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                                 public Model_LibraryVersion example_library;          // Program je příklad pro použití knihovny

    @JsonIgnore public UUID original_id; // KDyž se vytvoří kopie nebo se publikuje program, zde se uloží původní ID pro pozdější párování

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public Swagger_Short_Reference hardware_type(){
        try {
            Model_HardwareType type = getHardwareType();
            return new Swagger_Short_Reference(type.id, type.name, type.description);
        } catch (_Base_Result_Exception e) {
            // nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty public List<Model_CProgramVersion> program_versions() {
        try {

            return getVersions();

        } catch (_Base_Result_Exception e){
            //nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonProperty @ApiModelProperty(value = "Visible only for Administrators with permission - its default version of Main Program of each hardware type", required = false)
    public Model_CProgramVersion default_version() {
        try {

            if (getProjectId() != null) return null;
            check_update_permission();

            return default_main_version;

        }catch (_Base_Result_Exception e){
            //nothing
            return null;
        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }


/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public UUID getProjectId() throws _Base_Result_Exception  {

        if(publish_type == ProgramType.PRIVATE) {

            if (idCache().get(Model_Project.class) == null) {
                idCache().add(Model_Project.class, (UUID) Model_Project.find.query().where().eq("c_programs.id", id).select("id").findSingleAttribute());
            }

            return idCache().get(Model_Project.class);

        } else {
            return null;
        }
    }

    @JsonIgnore @Transient public Model_Project getProject() throws _Base_Result_Exception {
        try {
            return Model_Project.find.byId(getProjectId());
        }catch (Exception e) {
            // Řízená chyba
            return null;
        }
    }

    @JsonIgnore @Transient public List<UUID> getVersionsId() {

        if (idCache().gets(Model_CProgramVersion.class) == null) {
            idCache().add(Model_CProgramVersion.class, Model_CProgramVersion.find.query().where().eq("c_program.id", id).ne("deleted", true).order().desc("created").select("id").findSingleAttributeList());
        }

        return idCache().gets(Model_CProgramVersion.class) != null ?  idCache().gets(Model_CProgramVersion.class) : new ArrayList<>();
    }


    @JsonIgnore
    public void sort_Model_Model_CProgramVersion_ids() {

        List<Model_CProgramVersion> versions = getVersions();
        this.idCache().removeAll(Model_CProgramVersion.class);
        versions.stream().sorted((element1, element2) -> element2.created.compareTo(element1.created)).collect(Collectors.toList())
                .forEach(o -> this.idCache().add(Model_CProgramVersion.class, o.id));

    }


    @JsonIgnore @Transient public List<Model_CProgramVersion> getVersions() {
        try {

            List<Model_CProgramVersion> list = new ArrayList<>();

            for (UUID id : getVersionsId()) {
                list.add(Model_CProgramVersion.find.byId(id));
            }

            return list;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore @Transient public UUID getHardwareTypeId()     {

        if (idCache().get(Model_HardwareType.class) == null) {
            idCache().add(Model_HardwareType.class,  (UUID) Model_HardwareType.find.query().where().eq("c_programs.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_HardwareType.class);

    }

    @JsonIgnore @Transient public Model_HardwareType getHardwareType()     {
        try {
            return Model_HardwareType.find.byId(getHardwareTypeId());
        }catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        logger.debug("save :: Creating new Object");

        super.save();

        Model_Project project = getProject();

        // Call notification about project update
        if (project != null) new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_Project.class, project.id, project.id))).start();
    }

    @JsonIgnore @Override public void update() {

        logger.debug("update :: Update object Id: {}",  this.id);

        // Call notification about model update
        if(publish_type == ProgramType.PRIVATE) {
            new Thread(() -> {
                try {
                    EchoHandler.addToQueue(new WSM_Echo(Model_CProgram.class, getProjectId(), this.id));
                } catch (_Base_Result_Exception e) {
                    // Nothing
                }
            }).start();
        }

        super.update();
    }

    @JsonIgnore @Override public boolean delete() {

        logger.debug("delete :: Delete object Id: {} ", this.id);
        super.delete();

        // Remove from Project Cache
        if(publish_type == ProgramType.PRIVATE) {

            try {
                getProject().idCache().remove(this.getClass(), id);
            } catch (Exception e) {
                // Nothing
            }

            new Thread(() -> {
                try {
                    EchoHandler.addToQueue(new WSM_Echo(Model_Project.class, getProjectId(), getProjectId()));
                } catch (Exception e) {
                    // Nothing
                }
            }).start();
        }
        
        return false;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BlOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public String get_path() {

        // C_Program is Private registred under Project
        if (getProject() != null) {
            return getProject().getPath() + "/c-programs/" + this.id;
        } else {

            if(getProjectId() == null) {
                logger.debug("save :: is a public Program");
                return "public-c-programs/" + this.id;
            }else {
               return getProject().getPath() + "/c-programs/" + this.id;
            }
        }
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.CProgram_create.name())) return;

        if(this.project == null) {
            throw new Result_Error_PermissionDenied();
        }
        this.project.check_update_permission();
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        try {

            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_update_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_update_" + id);
                return;
            }

            if (_BaseController.person().has_permission(Permission.CProgram_update.name())) return;

            if(publish_type == ProgramType.PUBLIC || publish_type == ProgramType.DEFAULT_MAIN || publish_type == ProgramType.DEFAULT_TEST) {
                throw new Result_Error_PermissionDenied();
            }

            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
            this.getProject().check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_update_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_update_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception {
        try {

            if (publish_type == ProgramType.PUBLIC || publish_type == ProgramType.DEFAULT_MAIN || publish_type == ProgramType.DEFAULT_TEST) return;

            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_read_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_read_" + id);
                return;
            }

            if (_BaseController.person().has_permission(Permission.CProgram_read.name())) return;


            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
            this.getProject().check_read_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_read_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_read_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        try {

            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_delete_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_delete_" + id);
            }
            if (_BaseController.person().has_permission(Permission.CProgram_delete.name())) return;

            if(publish_type == ProgramType.PUBLIC || publish_type == ProgramType.DEFAULT_MAIN || publish_type == ProgramType.DEFAULT_TEST) {
                throw new Result_Error_PermissionDenied();
            }
            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
            this.getProject().check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }

    @JsonProperty @Transient @ApiModelProperty(required = false, value = "Visible only for Administrator with Permission") @JsonInclude(JsonInclude.Include.NON_NULL) public Boolean community_publishing_permission()  {
        try {
            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(Permission.C_Program_community_publishing_permission.name())) {
                return true;
            }
            return null;
        }catch (_Base_Result_Exception e){
            return null;
        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    public enum Permission { CProgram_create, CProgram_read, CProgram_update, CProgram_delete, C_Program_community_publishing_permission }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_CProgram.class)
    public static CacheFinder<Model_CProgram> find = new CacheFinder<>(Model_CProgram.class);
}

