package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.enums.EntityType;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.model.UnderProject;
import utilities.models_update_echo.EchoHandler;
import utilities.permission.Action;
import utilities.permission.Permissible;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "GridProject", description = "Model of GridProject")
@Table(name="GridProject")
public class Model_GridProject extends TaggedModel implements Permissible, UnderProject {

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

    @JsonIgnore @Override
    public Model_Project getProject() throws _Base_Result_Exception  {
        return isLoaded("project") ? project : Model_Project.find.query().nullable().where().eq("grid_projects.id", id).findOne();
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
            getProject().idCache().remove(this.getClass(), id);
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
        return getProject().getPath() + "/grid-projects/" + this.id;
    }


/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.GRID_PROJECT;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_GridProject.class)
    public static CacheFinder<Model_GridProject> find = new CacheFinder<>(Model_GridProject.class);
}
