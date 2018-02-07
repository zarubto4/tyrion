package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.libs.Json;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.enums.ProgramType;
import utilities.logger.Logger;
import utilities.model.NamedModel;
import utilities.models_update_echo.EchoHandler;
import utilities.swagger.input.Swagger_Library_File_Load;
import utilities.swagger.output.Swagger_Library_Version;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "Library", description = "Model of Library")
@Table(name="Library")
public class Model_Library extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Library.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY) public Model_Project project;

    public ProgramType publish_type;

    @ManyToMany(fetch = FetchType.LAZY) public List<Model_TypeOfBoard>  type_of_boards  = new ArrayList<>();

    @JsonIgnore @OneToMany(mappedBy = "library", cascade = CascadeType.ALL, fetch = FetchType.LAZY) @OrderBy("created DESC") public List<Model_Version> versions = new ArrayList<>();

    @ManyToMany public List<Model_Tag> tags = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached public List<UUID> cache_version_ids;
    @JsonIgnore @Transient @Cached public List<UUID> cache_type_of_board_ids;
    @JsonIgnore @Transient @Cached private UUID cache_project_id;
    @JsonIgnore @Transient @Cached private String cache_project_name;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty
    public UUID project_id()           {

        if (cache_project_id == null) {
            Model_Project project = Model_Project.find.query().where().eq("libraries.id", id).select("id").findOne();
            if (project == null) return null;
            cache_project_id = project.id;
        }

        return cache_project_id;
    }

    @JsonProperty
    public String project_name()         {
        try {

            if (cache_project_name == null) {
                if (project_id() == null) return null;
                cache_project_name = Model_Project.getById(project_id()).name;
            }

            return cache_project_name;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }
    @JsonProperty  @Transient public List<Swagger_Library_Version> versions() {

        List<Swagger_Library_Version> versions = new ArrayList<>();
        for (Model_Version version : this.getVersions()) {
            versions.add(this.library_version(version));
        }

        return versions;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public List<Model_Version> getVersions() {
        try {

            if (cache_version_ids.isEmpty()) {

                List<Model_Version> versions =  Model_Version.find.query().where().eq("library.id", id).eq("deleted", false).order().desc("created").select("id").findList();

                // Získání seznamu
                for (Model_Version version : versions) {
                    cache_version_ids.add(version.id);
                }
            }

            List<Model_Version> versions  = new ArrayList<>();

            for (UUID version_id : cache_version_ids) {
                versions.add(Model_Version.getById(version_id));
            }

            return versions;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<Model_Version>();
        }
    }

    @JsonIgnore
    public List<Model_TypeOfBoard> getType_of_Boards() {

        try {

            if (cache_type_of_board_ids == null) {

                cache_type_of_board_ids = new ArrayList<>();

                List<Model_TypeOfBoard> boards =  Model_TypeOfBoard.find.query().where().eq("libraries.id", id).eq("deleted", false).orderBy("UPPER(name) ASC").select("id").findList();

                // Získání seznamu
                for (Model_TypeOfBoard board : boards) {
                    cache_type_of_board_ids.add(board.id);
                }

            }

            List<Model_TypeOfBoard> boards  = new ArrayList<>();

            for (UUID board_id : cache_type_of_board_ids) {
                boards.add(Model_TypeOfBoard.getById(board_id));
            }

            return boards;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<Model_TypeOfBoard>();
        }
    }

    @JsonIgnore
    public Swagger_Library_Version library_version(Model_Version version) {
        try {

            Swagger_Library_Version help = new Swagger_Library_Version();

            help.id = version.id;
            help.name = version.name;
            help.description = version.description;
            help.delete_permission = delete_permission();
            help.update_permission = update_permission();
            help.author = version.author();

            for (Model_CProgram cProgram : version.examples) {
                help.examples.add(cProgram);
            }

            for (Model_Blob file : version.files) {

                JsonNode json = Json.parse(file.get_fileRecord_from_Azure_inString());

                Swagger_Library_File_Load form = Json.fromJson(json, Swagger_Library_File_Load.class);
                help.files.addAll(form.files);
            }

            return help;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {
        
        this.azure_library_link = "libraries/"  + UUID.randomUUID().toString();

        super.save();

        if (project != null) {
            Model_Project.getById(project.id).cache_library_ids.add(id);
        }

        cache.put(id, this);
    }

    @Override
    public void update() {

        logger.debug("update :: Update object Id: " + this.id);

        //Cache Update
        if (cache.get(this.id) != null) {
            cache.replace(this.id, this);
        } else cache.put(this.id, this);

        if (project_id() != null) new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_Library.class, project_id(), this.id))).start();

        //Database Update
        super.update();
    }

    @JsonIgnore @Override public boolean delete() {

        logger.debug("remove :: Update (hide) object Id: " + this.id);

        deleted = true;

        if (project_id() != null) {
            Model_Project.getById(project_id()).cache_library_ids.remove(id);
        }

        cache.remove(id);

        //Database Update
        super.update();
        return false;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore     private String azure_library_link;
    @JsonIgnore     public String get_path() {
        return azure_library_link;
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient public boolean create_permission() {
        try {
            if (project != null) return Model_Project.getById(project.id).update_permission(); return BaseController.person().has_permission("Library_create");
        } catch (NullPointerException exception) {
            logger.warn("create_permission null pointer exception - project is not probably in cache");
            return false;
        }
    }
    @JsonIgnore   @Transient public boolean read_permission()  {  return true; }
    @JsonProperty @Transient public boolean edit_permission()  {
        try {
          
            if (project_id() != null) return Model_Project.getById(project_id()).update_permission();
            return BaseController.person().has_permission("Library_edit");

        } catch (NullPointerException exception) {
            logger.warn("edit_permission null pointer exception - project is not probably in cache");
            return false;
        }
    }
    @JsonProperty @Transient public boolean delete_permission() {
        try {

             if (project_id() != null) return Model_Project.getById(project_id()).update_permission();
             return BaseController.person().has_permission("Library_delete");

        } catch (NullPointerException exception) {
            logger.warn("delete_permission null pointer exception - project is not probably in cache");
            return false;
        }
    }
    @JsonProperty @Transient public boolean update_permission() {
        try {

            if (project_id() != null) return Model_Project.getById(project_id()).update_permission();
            return BaseController.person().has_permission("Library_update");

        } catch (NullPointerException exception) {
            logger.warn("update_permission null pointer exception - project is not probably in cache");
            return false;
        }
   }
    @JsonProperty @Transient  @ApiModelProperty(required = false, value = "Visible only for Administrator with Permission") @JsonInclude(JsonInclude.Include.NON_NULL) public Boolean community_publishing_permission()  {
        // Cache už Obsahuje Klíč a tak vracím hodnotu
        return BaseController.person().has_permission(Model_CProgram.Permission.C_Program_community_publishing_permission.name());
    }

    public enum Permission { Library_create, Library_edit, Library_update, Library_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_Library.class, timeToIdle = 600)
    public static Cache<UUID, Model_Library> cache;

    public static Model_Library getById(String id) {
        return getById(UUID.fromString(id));
    }

    public static Model_Library getById(UUID id) {

        Model_Library library = cache.get(id);
        if (library == null) {

            library = Model_Library.find.byId(id);
            if (library == null) return null;

            cache.put(id, library);
        }

        return library;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Library> find = new Finder<>(Model_Library.class);
}