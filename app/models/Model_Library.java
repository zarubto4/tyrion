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

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached public List<UUID> cache_version_ids;
    @JsonIgnore @Transient @Cached public List<UUID> cache_hardware_type_ids;
    @JsonIgnore @Transient @Cached private UUID cache_project_id;
    @JsonIgnore @Transient @Cached private String cache_project_name;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/


    @JsonProperty
    public List<Model_LibraryVersion> versions() {

        List<Model_LibraryVersion> versions = new ArrayList<>();
        for (Model_LibraryVersion version : this.getVersions()) {
            versions.add(version);
        }

        return versions;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID get_project_id() {

        if (cache_project_id == null) {
            Model_Project project = Model_Project.find.query().where().eq("libraries.id", id).select("id").findOne();
            if (project == null) return null;
            cache_project_id = project.id;
        }

        return cache_project_id;
    }

    @JsonIgnore
    public Model_Project get_project() throws _Base_Result_Exception {

        if (cache_project_id == null) {
            return Model_Project.getById(get_project_id());
        }else {
            return Model_Project.getById(cache_project_id);
        }
    }

    @JsonIgnore
    public List<Model_LibraryVersion> getVersions() {
        try {

            if (cache_version_ids == null ||cache_version_ids.isEmpty()) {

                cache_version_ids =  Model_LibraryVersion.find.query().where().eq("library.id", id).eq("deleted", false).order().desc("created").findIds();

            }

            List<Model_LibraryVersion> versions  = new ArrayList<>();

            for (UUID version_id : cache_version_ids) {
                versions.add(Model_LibraryVersion.getById(version_id));
            }

            return versions;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public List<Model_HardwareType> getHardwareTypes() {

        try {

            if (cache_hardware_type_ids == null) {

                cache_hardware_type_ids = new ArrayList<>();

                List<UUID> hardwareTypes_id =  Model_HardwareType.find.query().where().eq("libraries.id", id).orderBy("UPPER(name) ASC").findIds();

                // Získání seznamu
                for (UUID id : hardwareTypes_id) {
                    cache_hardware_type_ids.add(id);
                }

            }

            List<Model_HardwareType> hardwareTypes  = new ArrayList<>();

            for (UUID hardware_type_id : cache_hardware_type_ids) {
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
            project.cache_library_ids.add(id);
        }

        cache.put(id, this);
    }

    @Override
    public void update() {

        logger.debug("update :: Update object Id: " + this.id);

        new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_Library.class, get_project_id(), this.id))).start();

        //Database Update
        super.update();
    }

    @JsonIgnore @Override public boolean delete() {

        logger.debug("remove :: Update (hide) object Id: " + this.id);

        super.delete();

        try{
            Model_Project.getById(get_project_id()).cache_library_ids.remove(id);
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
        if(project != null) project.check_update_permission();
        if(_BaseController.person().has_permission(Permission.Library_create.name())) return;
        get_project().check_update_permission();
    }

    @JsonIgnore @Override  @Transient public void check_read_permission() throws _Base_Result_Exception  {
        if(_BaseController.person().has_permission(Permission.Library_read.name())) return;
        get_project().check_update_permission();

    }

    @JsonIgnore @Override  @Transient public void check_update_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Library_update.name())) return;
        get_project().check_update_permission();
    }

    @JsonIgnore @Override  @Transient public void check_delete_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Library_delete.name())) return;
        get_project().check_update_permission();
    }

    @JsonIgnore @Transient public void check_community_publishing_permission() throws _Base_Result_Exception {
        if(community_publishing_permission()) return;
        throw new Result_Error_PermissionDenied();
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
        library.check_read_permission();
        return library;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Library> find = new Finder<>(Model_Library.class);
}