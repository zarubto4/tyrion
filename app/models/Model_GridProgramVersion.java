package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import play.libs.Json;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.enums.EntityType;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.UnderProject;
import utilities.model.VersionModel;
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
@ApiModel( value = "GridProgramVersion", description = "Model of GridProgramVersion")
@Table(name="GridProgramVersion")
public class Model_GridProgramVersion extends VersionModel implements Permissible, UnderProject {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_GridProgramVersion.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY) public Model_GridProgram grid_program;
    @JsonProperty @Column(columnDefinition = "TEXT")  public String m_program_virtual_input_output;

    @JsonIgnore @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "grid_program_version") public List<Model_BProgramVersionSnapGridProjectProgram> m_program_instance_parameters = new ArrayList<>();

    public boolean public_access;

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

/* JSON PROPERTY VALUES -------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient public String program_version() {
        try {

            Model_Blob fileRecord = Model_Blob.find.query().where().eq("grid_program_version.id", id).eq("name", "grid_program.json").findOne();

            if (fileRecord != null) {

                JsonNode json = Json.parse(fileRecord.downloadString());
               return json.get("m_code").asText();

            }

            return  null;

        } catch (_Base_Result_Exception e){
            //nothing
            return null;
        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @Override
    public Model_Project getProject() {
        return this.get_grid_program().getProject();
    }

    @JsonIgnore
    public UUID get_grid_program_id() throws _Base_Result_Exception {


        if (idCache().get(Model_GridProgram.class) == null) {
            idCache().add(Model_GridProgram.class, (UUID) Model_GridProgram.find.query().where().eq("versions.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_GridProgram.class);
    }

    @JsonIgnore
    public Model_GridProgram get_grid_program() throws _Base_Result_Exception {
        return this.grid_program != null ? this.grid_program : Model_GridProgram.find.query().where().eq("versions.id", id).findOne();
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {
        logger.debug("save::Creating new Object");

        super.save();

        Model_GridProgram program = get_grid_program();

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Project.class, program.get_grid_project().get_project_id(), program.id));
            } catch (_Base_Result_Exception e) {
               // Nothing
            }
        }).start();

        program.idCache().add(this.getClass(), id);
        program.sort_Model_Model_GridProgramVersion_ids();
    }

    @JsonIgnore @Override
    public void update() {
        logger.debug("update::Update object Id: {}",  this.id);
        super.update();

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_GridProgram.class, get_grid_program().get_grid_project().get_project_id(), id));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

    }

    @JsonIgnore @Override
    public boolean delete() {
        logger.debug("delete::Delete object Id: {}",  this.id);
        super.delete();

        // Remove from Cache
        try {
            get_grid_program().idCache().remove(this.getClass(), id);
        } catch (_Base_Result_Exception e) {
            // Nothing
        }

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_GridProgram.class, get_grid_program().get_grid_project().get_project_id(), get_grid_program_id()));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

        return false;
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public String get_path() {
        if(get_grid_program() != null) {
            return get_grid_program().get_path() + "/version/" + this.id;

        }else {
            return get_grid_program().get_path() + "/version/" + this.id;
        }
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @Override
    public EntityType getEntityType() {
        return EntityType.GRID_PROGRAM_VERSION;
    }

    @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_GridProgramVersion.class)
    public static CacheFinder<Model_GridProgramVersion> find = new CacheFinder<>(Model_GridProgramVersion.class);
}
