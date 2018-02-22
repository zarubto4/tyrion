package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.storage.StorageException;
import controllers.Controller_WebSocket;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.io.FileExistsException;
import org.ehcache.Cache;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import responses.*;
import utilities.Server;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.enums.CompilationStatus;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.VersionModel;
import utilities.models_update_echo.EchoHandler;
import utilities.swagger.input.Swagger_C_Program_Version_Update;
import utilities.swagger.input.Swagger_Library_File_Load;
import utilities.swagger.input.Swagger_Library_Record;
import websocket.messages.compilator_with_tyrion.WS_Message_Make_compilation;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_autobackup_made;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.net.ConnectException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

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
    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_CProgram> examples(){
        return  examples;
    }

    // TODO Cache - Performeance [TZ]!
    @ApiModelProperty(required = true, readOnly = true)
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

    @JsonIgnore @Override public void check_create_permission() throws _Base_Result_Exception { get_library().check_update_permission();}
    @JsonIgnore @Override public void check_read_permission()   throws _Base_Result_Exception { get_library().check_read_permission();}
    @JsonIgnore @Override public void check_update_permission() throws _Base_Result_Exception { get_library().check_update_permission();}
    @JsonIgnore @Override public void check_delete_permission() throws _Base_Result_Exception { get_library().check_update_permission();}

    public enum Permission {} // Not Required here

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_LibraryVersion.class, timeToIdle = 600)
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
