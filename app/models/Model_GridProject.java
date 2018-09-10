package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
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
    @JsonIgnore @OneToMany(mappedBy = "grid_project", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_BProgramVersionSnapGridProject> snapshots = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy = "grid_project", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_GridProgram> grid_programs = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true) public List<Model_GridProgram> m_programs() {
        try {
            return getGridPrograms();
        } catch (_Base_Result_Exception e) {
             //nothing
            return null;
        } catch (Exception e) {
             logger.internalServerError(e);
             return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* GET SQL PARAMETER - CACHE OBJECTS ------------------------------------------------------------------------------------*/

    @JsonIgnore
    public List<UUID> getGrid_programs_ids() {
        if (idCache().gets(Model_GridProgram.class) == null) {
            idCache().add(Model_GridProgram.class, Model_GridProgram.find.query().where().eq("deleted", false).eq("grid_project.id", id).orderBy("UPPER(name) ASC").select("id").findSingleAttributeList());
        }

        return idCache().gets(Model_GridProgram.class) != null ?  idCache().gets(Model_GridProgram.class) : new ArrayList<>();
    }

    @JsonIgnore public List<Model_GridProgram> getGridPrograms() {
    try {

            List<Model_GridProgram> gridPrograms  = new ArrayList<>();

            for (UUID version_id : getGrid_programs_ids()) {
                gridPrograms.add(Model_GridProgram.find.byId(version_id));
            }

            return gridPrograms;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore @Transient public UUID get_project_id() throws _Base_Result_Exception {

        if (idCache().get(Model_Project.class) == null) {
            idCache().add(Model_Project.class, (UUID) Model_Project.find.query().where().eq("grid_projects.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_Project.class);
    }

    @JsonIgnore @Transient public Model_Project get_project() throws _Base_Result_Exception  {
        return  Model_Project.find.byId(get_project_id());
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        // Save Object
        super.save();

        // If Object Contains Project - add id to cache
        if (project != null) {
            project.idCache().add(this.getClass(), id);
        }

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

        try {
            get_project().idCache().remove(this.getClass(), id);
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

    @JsonIgnore @Deprecated
    private String blob_link;  // TODO SMAZAT z DATAB8ZE

    @JsonIgnore
    public String get_path() {

        // FOR OLD already created objects is still using blob_link, but its @Deprecated
        if(blob_link != null) return blob_link;
        return get_project().getPath() + "/grid-projects/" + this.id;
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
        if ( Model_GridProject.find.query().where().eq("project.participants.person.id", _BaseController.personId()).eq("id", id).findCount() > 0) {
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
        if ( Model_GridProject.find.query().where().eq("project.participants.person.id", _BaseController.personId()).eq("id", id).findCount() > 0) {
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
        if ( Model_GridProject.find.query().where().eq("project.participants.person.id", _BaseController.personId()).eq("id", id).findCount() > 0) {
            _BaseController.person().cache_permission("grid_project_delete_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        _BaseController.person().cache_permission("grid_project_delete_" + id, false);
        throw new Result_Error_PermissionDenied();

    }

    public enum Permission { GridProject_create, GridProject_read, GridProject_update, GridProject_edit, GridProject_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_GridProject.class)
    public static CacheFinder<Model_GridProject> find = new CacheFinder<>(Model_GridProject.class);
}

