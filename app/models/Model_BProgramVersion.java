package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import org.ehcache.Cache;
import utilities.cache.CacheField;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.VersionModel;
import utilities.models_update_echo.EchoHandler;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "BProgramVersion", description = "Model of BProgram Version")
@Table(name="BProgramVersion")
public class Model_BProgramVersion extends VersionModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_BProgramVersion.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne  public Model_Library library;
    @JsonIgnore @OneToMany(mappedBy = "example_library")  public List<Model_CProgram> examples = new ArrayList<>();

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)   public Model_BProgram b_program;

    @JsonIgnore public String additional_json_configuration;

    @JsonIgnore  @OneToMany(mappedBy = "b_program_version", fetch = FetchType.LAZY)
    public List<Model_BProgramVersionSnapGridProject> grid_project_snapshots = new ArrayList<>();    // Vazba kvůli puštěným B_programům

    // B_Program - Instance
    @JsonIgnore @OneToMany(mappedBy="b_program_version", fetch = FetchType.LAZY) public List<Model_InstanceSnapshot> instances = new ArrayList<>();

/* JSON PROPERTY VALUES -------------------------------------------------------------------------------------------------*/

    @JsonProperty
    public String program() {
        // TODO Hodně náročné na stahování do Cahce - Nejlépe takový objekt na linky, že sám sebe zahodí po vypršení platnosti
        // Myslím, že jsem ho někde programoval! Tom
        try {

            Model_Blob blob = Model_Blob.find.query().where().eq("b_program_version.id", id).eq("name", "blocko.json").findOne();
            if (blob != null) return blob.get_fileRecord_from_Azure_inString();
            return null;

        } catch (_Base_Result_Exception e) {
            // nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }
    @JsonProperty @Transient public List<Model_BProgramVersionSnapGridProject> grid_project_snapshots() {
        try {
            return get_grid_project_snapshots();
        } catch (_Base_Result_Exception e) {
            // nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public List<UUID> get_grid_snapshot_ids() throws _Base_Result_Exception {

        if (cache().gets(Model_BProgramVersionSnapGridProject.class) == null) {
            cache().add(Model_BProgramVersionSnapGridProject.class, Model_BProgramVersionSnapGridProject.find.query().where().eq("b_program_version.id", id).select("id").findSingleAttributeList());
        }

        return cache().gets(Model_BProgramVersionSnapGridProject.class) != null ?  cache().gets(Model_BProgramVersionSnapGridProject.class) : new ArrayList<>();

    }

    @JsonIgnore
    public List<Model_BProgramVersionSnapGridProject> get_grid_project_snapshots() {
        try {

            List<Model_BProgramVersionSnapGridProject> list = new ArrayList<>();

            for (UUID id : get_grid_snapshot_ids()) {
                list.add(Model_BProgramVersionSnapGridProject.getById(id));
            }

            return list;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }



    @JsonIgnore
    public UUID get_b_program_id() throws _Base_Result_Exception {

        if (cache().get(Model_Project.class) == null) {
            cache().add(Model_Project.class, Model_BProgram.find.query().where().eq("versions.id", id).select("id").findSingleAttributeList());
        }

        return cache().get(Model_Project.class);

    }

    @JsonIgnore
    public Model_BProgram get_b_program() throws _Base_Result_Exception {
        try {
            return Model_BProgram.getById(get_b_program_id());
        }catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        logger.debug("save :: Creating new Object");

        super.save();


        System.out.println("Uložil jsem BProgram Verzi id: " + this.id);

        Model_BProgram program = get_b_program();

        new Thread(() -> {
            EchoHandler.addToQueue(new WSM_Echo(Model_BProgram.class, program.getProjectId(), program.id));
        }).start();

        // Add to Cache
        if (program != null) {
            program.getVersionsIds();
            program.cache().add(this.getClass(), this.id);
            program.sort_Model_Model_BProgramVersion_ids();
        }
    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update::Update object Id: {}",  this.id);
        super.update();

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_BProgram.class, get_b_program().getProjectId(), get_b_program_id()));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

    }

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("delete::Delete object Id: {}",  this.id);

        // Remove from Cache
        try {
            get_b_program().cache().remove(this.getClass(), id);
        } catch (_Base_Result_Exception e) {
            // Nothing
        }

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_BProgram.class, get_b_program().getProjectId(), get_b_program_id()));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

        super.delete();

        return false;
    }

/* Services --------------------------------------------------------------------------------------------------------*/


/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public String get_path() {
        return get_b_program().get_path() + "/version/" + this.id;
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void check_create_permission() throws _Base_Result_Exception { b_program.check_update_permission();} // You have to access b_program directly, because get_b_program() finds the b_program by id of the version which is not yet created
    @JsonIgnore @Override public void check_read_permission()   throws _Base_Result_Exception {
        try {

            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_read_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_read_" + id);
                return;
            }

            get_b_program().check_read_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_read_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_read_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }
    @JsonIgnore @Override public void check_update_permission() throws _Base_Result_Exception {
        try {

            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_update_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_update_" + id);
                return;
            }

            get_b_program().check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_update_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_update_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }
    @JsonIgnore @Override public void check_delete_permission() throws _Base_Result_Exception {
        try {
            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_delete_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_delete_" + id);
                return;
            }

            get_b_program().check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }

    public enum Permission {} // Not Required here

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(Model_BProgramVersion.class)
    public static Cache<UUID, Model_BProgramVersion> cache;

    public static Model_BProgramVersion getById(UUID id) throws _Base_Result_Exception {

        Model_BProgramVersion b_program_version = cache.get(id);

        if (b_program_version == null) {

            b_program_version = Model_BProgramVersion.find.byId(id);
            if (b_program_version == null) throw new Result_Error_NotFound(Model_BProgramVersion.class);

            cache.put(id, b_program_version);
        }
        // Check Permission
        if(b_program_version.its_person_operation()) {
            b_program_version.check_read_permission();
        }
        return b_program_version;
    }

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_BProgramVersion> find = new Finder<>(Model_BProgramVersion.class);
}
