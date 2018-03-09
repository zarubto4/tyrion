package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.CacheField;
import utilities.cache.Cached;
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
@ApiModel( value = "GridProject", description = "Model of GridProject")
@Table(name="GridProject")
public class Model_GridProject extends TaggedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_GridProject.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                          @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Project project;
    @JsonIgnore @OneToMany(mappedBy = "grid_project", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_BProgramVersionSnapGridProject> snapShots = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy = "grid_project", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_GridProgram> grid_programs = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_project_id;
    @JsonIgnore @Transient @Cached public List<UUID> grid_programs_ids = new ArrayList<>();

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true) public List<Model_GridProgram> m_programs() { return getGridPrograms();}

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* GET SQL PARAMETER - CACHE OBJECTS ------------------------------------------------------------------------------------*/

    @JsonIgnore public List<Model_GridProgram> getGridPrograms() {
        try {

            if (grid_programs_ids.isEmpty()) {

                List<UUID> uuids =  Model_GridProgram.find.query().where().eq("grid_project.id", id).orderBy("UPPER(name) ASC").findIds();

                // Získání seznamu
                for (UUID uuid : uuids) {
                    grid_programs_ids.add(uuid);
                }
            }

            List<Model_GridProgram> gridPrograms  = new ArrayList<>();

            for (UUID version_id : grid_programs_ids) {
                gridPrograms.add(Model_GridProgram.getById(version_id));
            }

            return gridPrograms;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore @Transient public UUID get_project_id() throws _Base_Result_Exception {

        if (cache_project_id == null) {
            Model_Project project = Model_Project.find.query().where().eq("grid_projects.id", id).select("id").findOne();
            if (project == null) return null;
            cache_project_id = project.id;
        }

        return cache_project_id;
    }

    @JsonIgnore @Transient public Model_Project get_project() throws _Base_Result_Exception  {
        return  Model_Project.getById(get_project_id());
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        // Create Link for Path for File - Like folder structures
        this.blob_link = project.getPath()  + "/grid-projects/"  + UUID.randomUUID().toString();

        // Save Object
        super.save();

        // If Object Contains Project - add id to cache
        if (project != null) {
            project.cache_grid_project_ids.add(id);
        }

        // Add to General Cache
        cache.put(id, this);

        // Inform All clients independently
        if (project != null ) new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Project.class, get_project_id(), get_project_id()));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update :: Update object Id: {}",  this.id);

        super.update();

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo( Model_GridProject.class, get_project_id(), id));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

    }

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("update :: Delete object Id: {} ", this.id);

        super.delete();
        cache.remove(id);

        try {
            Model_Project.getById(get_project_id()).cache_grid_project_ids.remove(id);
        }catch (_Base_Result_Exception e){
            // Nothing
        }

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Project.class, get_project_id(), get_project_id()));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

        return false;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    private String blob_link;

    @JsonIgnore
    public String get_path() {
        return blob_link;
    }


/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if (_BaseController.person().has_permission(Permission.GridProject_create.name())) return;
        project.check_update_permission();
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (_BaseController.person().has_permission("grid_project_update_" + id)) _BaseController.person().valid_permission("grid_project_update_" + id);
        if (_BaseController.person().has_permission(Permission.GridProject_update.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_GridProject.find.query().where().where().eq("project.participants.person.id", _BaseController.personId()).where().eq("id", id).findCount() > 0) {
            _BaseController.person().cache_permission("grid_project_update_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        _BaseController.person().cache_permission("grid_project_update_" + id, false);
        throw new Result_Error_PermissionDenied();

    }
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (_BaseController.person().has_permission("grid_project_read_" + id)) _BaseController.person().valid_permission("grid_project_read_" + id);
        if (_BaseController.person().has_permission(Permission.GridProject_read.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_GridProject.find.query().where().where().eq("project.participants.person.id", _BaseController.personId()).where().eq("id", id).findCount() > 0) {
            _BaseController.person().cache_permission("grid_project_read_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        _BaseController.person().cache_permission("grid_project_read_" + id, false);
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_delete_permission()  throws _Base_Result_Exception {
        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (_BaseController.person().has_permission("grid_project_delete_" + id)) _BaseController.person().valid_permission("grid_project_delete_" + id);
        if (_BaseController.person().has_permission(Permission.GridProject_delete.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_GridProject.find.query().where().where().eq("project.participants.person.id", _BaseController.personId()).where().eq("id", id).findCount() > 0) {
            _BaseController.person().cache_permission("grid_project_delete_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        _BaseController.person().cache_permission("grid_project_delete_" + id, false);
        throw new Result_Error_PermissionDenied();

    }

    public enum Permission { GridProject_create, GridProject_read, GridProject_update, GridProject_edit, GridProject_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(Model_GridProject.class)
    public static Cache<UUID, Model_GridProject> cache;

    public static Model_GridProject getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }
    
    public static Model_GridProject getById(UUID id) throws _Base_Result_Exception {

        Model_GridProject grid_project= cache.get(id);
        if (grid_project == null) {

            grid_project = Model_GridProject.find.byId(id);
            if (grid_project == null) throw new Result_Error_NotFound(Model_GridProject.class);

            cache.put(id, grid_project);
        }

        grid_project.check_read_permission();
        return grid_project;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    
    public static Finder<UUID, Model_GridProject> find = new Finder<>(Model_GridProject.class);
}

