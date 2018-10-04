package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
import utilities.swagger.input.Swagger_Library_File_Load;
import utilities.swagger.input.Swagger_Library_Record;
import utilities.swagger.output.Swagger_Short_Reference;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "LibraryVersion", description = "Model of LibraryVersion")
@Table(name="LibraryVersion")
public class Model_LibraryVersion extends VersionModel implements Permissible, UnderProject {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_LibraryVersion.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne  public Model_Library library;
    @JsonIgnore @OneToMany(mappedBy = "example_library", cascade = CascadeType.ALL)  public List<Model_CProgram> examples = new ArrayList<>();

/* JSON PROPERTY VALUES -------------------------------------------------------------------------------------------------*/

    // TODO Cache - Performeance [TZ]! LEVEL: HARD  TIME: LONGTERM
    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_Short_Reference> c_rpogram_examples(){
        try {
            List<Swagger_Short_Reference> pairs = new ArrayList<>();
            for (Model_CProgram cProgram : examples) {
                pairs.add(new Swagger_Short_Reference(cProgram.id, cProgram.name, cProgram.description));
            }
            return pairs;
        } catch (_Base_Result_Exception e){
            //nothing
            return null;
        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    // TODO Cache - Performeance [TZ]! LEVEL: HARD  TIME: LONGTERM
    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_Library_Record> files(){
        try {

            JsonNode json = Json.parse(file.downloadString());
            return formFromJsonWithValidation(Swagger_Library_File_Load.class, json).files;


        }catch (_Base_Result_Exception e){
            //nothing
            return null;

        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID getLibraryId() throws _Base_Result_Exception {
        return this.getLibrary().id;
    }

    @JsonIgnore
    public Model_Library getLibrary() throws _Base_Result_Exception {
        return isLoaded("library") ? library : Model_Library.find.query().where().eq("versions.id", id).findOne();
    }

    @JsonIgnore @Override
    public Model_Project getProject() {
        return getLibrary().getProject();
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        super.save();

        // Add to Cache
        if(getLibrary() != null) {
            getLibrary().getVersionIds();
            getLibrary().idCache().add(this.getClass(), id);
        }

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Widget.class, getLibrary().getProjectId(), getLibraryId()));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();
    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update::Update object Id: {}",  this.id);
        super.update();

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Library.class, getLibrary().getProjectId(), getLibraryId()));
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
            getLibrary().idCache().remove(this.getClass(), id);
        } catch (_Base_Result_Exception e) {
            // Nothing
        }

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Library.class, getLibrary().getProjectId(), getLibraryId()));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

        return false;
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public String get_path() {
        if(library != null) {
            return library.get_path() + "/version/" + this.id;
        }else {
            return getLibrary().get_path() + "/version/" + this.id;
        }
    }


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.LIBRARY_VERSION;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_LibraryVersion.class)
    public static CacheFinder<Model_LibraryVersion> find = new CacheFinder<>(Model_LibraryVersion.class);
}
