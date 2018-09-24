package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
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
@ApiModel( value = "BProgramVersion", description = "Model of BProgram Version")
@Table(name="BProgramVersion")
public class Model_BProgramVersion extends VersionModel implements Permissible, UnderProject {

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
        try {

            Model_Blob blob = Model_Blob.find.query().where().eq("b_program_version.id", id).eq("name", "blocko.json").findOne();
            return blob.getPublicDownloadLink();

        } catch (_Base_Result_Exception e) {
            // nothing
        } catch (Exception e) {
            logger.internalServerError(e);
        }
        return null;
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

        if (idCache().gets(Model_BProgramVersionSnapGridProject.class) == null) {
            idCache().add(Model_BProgramVersionSnapGridProject.class, Model_BProgramVersionSnapGridProject.find.query().where().eq("b_program_version.id", id).select("id").findSingleAttributeList());
        }

        return idCache().gets(Model_BProgramVersionSnapGridProject.class) != null ?  idCache().gets(Model_BProgramVersionSnapGridProject.class) : new ArrayList<>();

    }

    @JsonIgnore
    public List<Model_BProgramVersionSnapGridProject> get_grid_project_snapshots() {
        try {

            List<Model_BProgramVersionSnapGridProject> list = new ArrayList<>();

            for (UUID id : get_grid_snapshot_ids()) {
                list.add(Model_BProgramVersionSnapGridProject.find.byId(id));
            }

            return list;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }



    @JsonIgnore
    public UUID get_b_program_id() throws _Base_Result_Exception {

        if (idCache().get(Model_Project.class) == null) {
            idCache().add(Model_Project.class, Model_BProgram.find.query().where().eq("versions.id", id).select("id").findSingleAttributeList());
        }

        return idCache().get(Model_Project.class);

    }

    @JsonIgnore
    public Model_BProgram getBProgram() throws _Base_Result_Exception {
        return Model_BProgram.find.query().where().eq("versions.id", id).findOne();
    }

    @Override
    public Model_Project getProject() {
        return this.b_program != null ? this.b_program.getProject() : this.getBProgram().getProject();
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        logger.debug("save :: Creating new Object");

        super.save();

        Model_BProgram program = getBProgram();

        new Thread(() -> {
            EchoHandler.addToQueue(new WSM_Echo(Model_BProgram.class, program.getProjectId(), program.id));
        }).start();

        // Add to Cache
        if (program != null) {
            program.getVersionsIds();
            program.idCache().add(this.getClass(), this.id);
            program.sort_Model_Model_BProgramVersion_ids();
        }
    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update::Update object Id: {}",  this.id);
        super.update();

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_BProgram.class, getBProgram().getProjectId(), get_b_program_id()));
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
            getBProgram().idCache().remove(this.getClass(), id);
        } catch (_Base_Result_Exception e) {
            // Nothing
        }

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_BProgram.class, getBProgram().getProjectId(), get_b_program_id()));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

        super.delete();

        return false;
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public String get_path() {
        return getBProgram().get_path() + "/version/" + this.id;
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @Override
    public EntityType getEntityType() {
        return EntityType.BLOCKO_PROGRAM_VERSION;
    }

    @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_BProgramVersion.class)
    public static CacheFinder<Model_BProgramVersion> find = new CacheFinder<>(Model_BProgramVersion.class);
}
