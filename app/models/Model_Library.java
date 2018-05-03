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

@Entity
@ApiModel(value = "Library", description = "Model of Library")
@Table(name="Library")
public class Model_Library extends TaggedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Library.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY) public Model_Project project;

    public ProgramType publish_type;


    @ManyToMany(fetch = FetchType.LAZY) public List<Model_HardwareType> hardware_types = new ArrayList<>();

    @JsonIgnore @OneToMany(mappedBy = "library", cascade = CascadeType.ALL, fetch = FetchType.LAZY) @OrderBy("created DESC") public List<Model_LibraryVersion> versions = new ArrayList<>();

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/


    @JsonProperty
    public List<Model_LibraryVersion> versions() {
        try {
            List<Model_LibraryVersion> versions = new ArrayList<>();
            for (Model_LibraryVersion version : this.getVersions()) {
                versions.add(version);
            }

            return versions;
        } catch (_Base_Result_Exception e) {
            //nothing
            return null;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID getProjectId() {

        if (cache().get(Model_Project.class) == null) {
            cache().add(Model_Project.class, (UUID) Model_Project.find.query().where().eq("libraries.id", id).select("id").findSingleAttribute());
        }

        return cache().get(Model_Project.class);
    }

    @JsonIgnore
    public Model_Project get_project() throws _Base_Result_Exception {

        try {
            return Model_Project.getById(getProjectId());
        }catch (Exception e) {
            return null;
        }
    }

    @JsonIgnore
    public List<UUID> getVersionIds() {
        if (cache().gets(Model_LibraryVersion.class) == null) {
            cache().add(Model_LibraryVersion.class, Model_LibraryVersion.find.query().where().eq("library.id", id).eq("deleted", false).order().desc("created").select("id").findSingleAttributeList());
        }

        return cache().gets(Model_LibraryVersion.class);
    }

    @JsonIgnore
    public List<Model_LibraryVersion> getVersions() {
        try {

            List<Model_LibraryVersion> versions  = new ArrayList<>();

            for (UUID version_id : getVersionIds()) {
                versions.add(Model_LibraryVersion.getById(version_id));
            }

            return versions;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }


    @JsonIgnore
    public List<UUID> getHardwareTypesId() {
        if (cache().gets(Model_HardwareType.class) == null) {
            cache().add(Model_HardwareType.class, Model_HardwareType.find.query().where().eq("libraries.id", id).eq("deleted", false).orderBy("UPPER(name) ASC").select("id").findSingleAttributeList());
        }

        return cache().gets(Model_HardwareType.class);
    }
    @JsonIgnore
    public List<Model_HardwareType> getHardwareTypes() {
        try {

            List<Model_HardwareType> hardwareTypes  = new ArrayList<>();

            for (UUID hardware_type_id : getHardwareTypesId()) {
                hardwareTypes.add(Model_HardwareType.getById(hardware_type_id));
            }

            return hardwareTypes;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }
    /* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        this.azure_library_link = "libraries/"  + UUID.randomUUID().toString();

        super.save();

        if (project != null) {
            project.cache().add(this.getClass(),id);
        }

        cache.put(id, this);
    }

    @Override
    public void update() {

        logger.debug("update :: Update object Id: " + this.id);

        new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_Library.class, getProjectId(), this.id))).start();

        //Database Update
        super.update();
    }

    @JsonIgnore @Override public boolean delete() {

        logger.debug("remove :: Update (hide) object Id: " + this.id);

        super.delete();

        try{
            get_project().cache().remove(this.getClass(),id);
        }catch (_Base_Result_Exception exception){
            // Nothing
        }

        return false;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore private String azure_library_link;
    @JsonIgnore public String get_path() { return azure_library_link; }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override @Transient public void check_create_permission() throws _Base_Result_Exception  {
        if(_BaseController.person().has_permission(Permission.Library_create.name())) return;
        project.check_update_permission();
    }

    @JsonIgnore @Transient @Override public void check_read_permission() throws _Base_Result_Exception   {
        try {

            if (publish_type == ProgramType.PUBLIC || publish_type == ProgramType.DEFAULT_MAIN) return;

            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_read_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_read_" + id);
            }

            if (_BaseController.person().has_permission(Permission.Library_read.name())) return;


            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
            this.get_project().check_read_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_read_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_read_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        try {

            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_update_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_update_" + id);
            }

            if (_BaseController.person().has_permission(Permission.Library_update.name())) return;

            if(publish_type == ProgramType.PUBLIC) {
                throw new Result_Error_PermissionDenied();
            }

            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
            this.get_project().check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_update_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_update_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception  {
        try {

            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_delete_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_delete_" + id);
            }
            if (_BaseController.person().has_permission(Permission.Library_delete.name())) return;

            if(publish_type == ProgramType.PUBLIC) {
                throw new Result_Error_PermissionDenied();
            }
            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
            this.get_project().check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }

    @JsonProperty @Transient @ApiModelProperty(required = false, value = "Visible only for Administrator with Permission") @JsonInclude(JsonInclude.Include.NON_NULL) public Boolean community_publishing_permission()  {
        try {
            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if(_BaseController.person().has_permission(Model_CProgram.Permission.C_Program_community_publishing_permission.name())) return true;
            return null;
        }catch (_Base_Result_Exception exception){
            return null;
        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    public enum Permission { Library_create, Library_read, Library_edit, Library_update, Library_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_Library.class, duration = 600)
    public static Cache<UUID, Model_Library> cache;

    public static Model_Library getById(UUID id) throws _Base_Result_Exception {

        Model_Library library = cache.get(id);
        if (library == null) {

            library = Model_Library.find.byId(id);
            if (library == null) throw new Result_Error_NotFound(Model_Library.class);

            cache.put(id, library);
        }
        // Check Permission
        if(library.its_person_operation()) {
            library.check_read_permission();
        }
        return library;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Library> find = new Finder<>(Model_Library.class);
}