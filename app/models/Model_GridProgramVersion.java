package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import org.ehcache.Cache;
import play.libs.Json;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.errors.Exceptions.Result_Error_NotFound;
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
@ApiModel( value = "GridProgramVersion", description = "Model of GridProgramVersion")
@Table(name="GridProgramVersion")
public class Model_GridProgramVersion extends VersionModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_GridProgramVersion.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY) public Model_GridProgram grid_program;
    @JsonProperty @Column(columnDefinition = "TEXT")  public String m_program_virtual_input_output;

    @JsonIgnore @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "grid_program_version") public List<Model_MProgramInstanceParameter> m_program_instance_parameters = new ArrayList<>();

    public boolean public_access;

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_grid_program_id;

/* JSON PROPERTY VALUES -------------------------------------------------------------------------------------------------*/



    @JsonProperty @Transient public String program_version() {
        try {

            Model_Blob fileRecord = Model_Blob.find.query().where().eq("grid_program_version.id", id).eq("name", "grid_program.json").findOne();

            if (fileRecord != null) {

                JsonNode json = Json.parse(fileRecord.get_fileRecord_from_Azure_inString());
               return json.get("m_code").asText();

            }

            return  null;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID get_grid_program_id() throws _Base_Result_Exception {

        if (cache_grid_program_id == null) {

            Model_GridProgram grid_program = Model_GridProgram.find.query().where().eq("versions.id", id).select("id").findOne();
            if (grid_program != null) {
                cache_grid_program_id = grid_program.id;
            } else {
                throw new Result_Error_NotFound(Model_Library.class);
            }
        }

        return cache_grid_program_id;
    }

    @JsonIgnore
    public Model_GridProgram get_grid_program() throws _Base_Result_Exception {

        if (cache_grid_program_id == null) {
           return Model_GridProgram.getById(get_grid_program_id());
        }
        return Model_GridProgram.getById(cache_grid_program_id);
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {
        logger.debug("save::Creating new Object");

        super.save();

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Project.class, grid_program.get_grid_project().get_project_id(), grid_program.id));
            } catch (_Base_Result_Exception e) {
               // Nothing
            }
        }).start();

        grid_program.cache_version_ids.add(0, id);

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
            get_grid_program().cache_version_ids.remove(id);
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

/* Services --------------------------------------------------------------------------------------------------------*/


/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void check_create_permission() throws _Base_Result_Exception { grid_program.check_update_permission();} // You have to access grid_program directly, because get_grid_program() finds the grid_program by id of the version which is not yet created
    @JsonIgnore @Override public void check_read_permission()   throws _Base_Result_Exception { get_grid_program().check_read_permission();}
    @JsonIgnore @Override public void check_update_permission() throws _Base_Result_Exception { get_grid_program().check_update_permission();}
    @JsonIgnore @Override public void check_delete_permission() throws _Base_Result_Exception { get_grid_program().check_update_permission();}

    public enum Permission {} // Not Required here

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_GridProgramVersion.class, duration = 600)
    public static Cache<UUID, Model_GridProgramVersion> cache;

    public static Model_GridProgramVersion getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }

    public static Model_GridProgramVersion getById(UUID id) throws _Base_Result_Exception {

        Model_GridProgramVersion version = cache.get(id);

        if (version == null) {

            version = find.byId(id);
            if (version == null) throw new Result_Error_NotFound(Model_GridProgramVersion.class);

            cache.put(id, version);
        }

        version.check_read_permission();
        return version;
    }

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_GridProgramVersion> find = new Finder<>(Model_GridProgramVersion.class);
}
