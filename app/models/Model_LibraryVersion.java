package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.libs.Json;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.VersionModel;
import utilities.models_update_echo.EchoHandler;
import utilities.swagger.input.Swagger_Library_File_Load;
import utilities.swagger.input.Swagger_Library_Library_Version_pair;
import utilities.swagger.input.Swagger_Library_Record;
import utilities.swagger.output.Swagger_Short_Reference;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "LibraryVersion", description = "Model of LibraryVersion")
@Table(name="LibraryVersion")
public class Model_LibraryVersion extends VersionModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_LibraryVersion.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne  public Model_Library library;
    @JsonIgnore @OneToMany(mappedBy = "example_library", cascade = CascadeType.ALL)  public List<Model_CProgram> examples = new ArrayList<>();


/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_library_id;


/* JSON PROPERTY VALUES -------------------------------------------------------------------------------------------------*/

    // TODO Cache - Performeance [TZ]!
    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_Short_Reference> c_rpogram_examples(){
        try {
            List<Swagger_Short_Reference> pairs = new ArrayList<>();
            for (Model_CProgram cProgram : examples) {
                pairs.add(new Swagger_Short_Reference(cProgram.id, cProgram.name, cProgram.description));
            }
            return pairs;
        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    // TODO Cache - Performeance [TZ]!
    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_Library_Record> files(){
        try {

            JsonNode json = Json.parse(file.get_fileRecord_from_Azure_inString());
            return baseFormFactory.formFromJsonWithValidation(Swagger_Library_File_Load.class, json).files;


        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID get_library_id() throws _Base_Result_Exception {

        if (cache_library_id == null) {

            Model_Library library = Model_Library.find.query().where().eq("versions.id", id).select("id").findOne();
            if (library != null) {
                cache_library_id = library.id;
            } else {
                throw new Result_Error_NotFound(Model_Library.class);
            }
        }

        return cache_library_id;
    }

    @JsonIgnore
    public Model_Library get_library() throws _Base_Result_Exception {

        if (cache_library_id == null) {
           return Model_Library.getById(get_library_id());
        }
        return Model_Library.getById(cache_library_id);
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        logger.debug("save :: Creating new Object");

        super.save();

        new Thread(() -> {
            EchoHandler.addToQueue(new WSM_Echo(Model_Library.class, library.get_project_id(), library.id));
        }).start();

        // Add to Cache
        if (library != null) {
            library.cache_version_ids.add(0, id);
        }
    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update::Update object Id: {}",  this.id);
        super.update();

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Library.class, get_library().get_project_id(), get_library_id()));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

    }

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("delete::Delete object Id: {}",  this.id);

        this.deleted = true;
        super.update();

        // Remove from Cache
        try {
            get_library().cache_version_ids.remove(id);
        } catch (_Base_Result_Exception e) {
            // Nothing
        }

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Library.class, get_library().get_project_id(), get_library_id()));
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

    @JsonIgnore @Override public void check_create_permission() throws _Base_Result_Exception { library.check_update_permission();} // You have to access library directly, because get_library() finds the library by id of the version which is not yet created
    @JsonIgnore @Override public void check_read_permission()   throws _Base_Result_Exception { get_library().check_read_permission();}
    @JsonIgnore @Override public void check_update_permission() throws _Base_Result_Exception { get_library().check_update_permission();}
    @JsonIgnore @Override public void check_delete_permission() throws _Base_Result_Exception { get_library().check_update_permission();}

    public enum Permission {} // Not Required here

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_LibraryVersion.class, duration = 600)
    public static Cache<UUID, Model_LibraryVersion> cache;

    public static Model_LibraryVersion getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }

    public static Model_LibraryVersion getById(UUID id) throws _Base_Result_Exception {

        Model_LibraryVersion grid_widget_version = cache.get(id);

        if (grid_widget_version == null) {

            grid_widget_version = Model_LibraryVersion.find.byId(id);
            if (grid_widget_version == null) throw new Result_Error_NotFound(Model_LibraryVersion.class);

            cache.put(id, grid_widget_version);
        }

        // Check Permission
        grid_widget_version.check_read_permission();
        return grid_widget_version;
    }

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_LibraryVersion> find = new Finder<>(Model_LibraryVersion.class);
}
