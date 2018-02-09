package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.models_update_echo.EchoHandler;
import utilities.swagger.output.Swagger_GridProgramVersion;
import utilities.swagger.output.Swagger_M_Program_Version_Interface;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@ApiModel(value = "GridProgram", description = "Model of GridProgram")
@Table(name = "GridProgram")
public class Model_GridProgram extends TaggedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_GridProgram.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                                                      public Model_GridProject grid_project;
    @JsonIgnore @OneToMany(mappedBy="grid_program", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_Version> versions = new ArrayList<>();

/* JSON PROPERTY VALUES ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public UUID grid_project_id() {
        return grid_project.id;
    }

    @JsonProperty @ApiModelProperty(required = true)
    public List<Swagger_GridProgramVersion> program_versions() {

        List<Swagger_GridProgramVersion> versions = new ArrayList<>();

        for (Model_Version v : getVersions().stream().sorted((element1, element2) -> element2.created.compareTo(element1.created)).collect(Collectors.toList())) {
            versions.add(program_version(v));
        }

        return versions;
    }

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached public List<UUID> cache_version_ids = new ArrayList<>();

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public List<Model_Version> getVersions() {
        try {

            if (cache_version_ids.isEmpty()) {

                List<Model_Version> versions =  Model_Version.find.query().where().eq("grid_program.id", this.id).order().desc("created").select("id").findList();

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
            return new ArrayList<>();
        }
    }
    
    public static Swagger_GridProgramVersion program_version(Model_Version version) {
        try {

            Swagger_GridProgramVersion grid_version = new Swagger_GridProgramVersion();

            grid_version.version = version;
            grid_version.public_mode = version.public_version;

            grid_version.virtual_input_output = version.m_program_virtual_input_output;

            Model_Blob fileRecord = Model_Blob.find.query().where().eq("version.id", version.id).eq("name", "grid_program.json").findOne();

            if (fileRecord != null) {

                JsonNode json = Json.parse(fileRecord.get_fileRecord_from_Azure_inString());
                grid_version.m_code = json.get("m_code").asText();

            }

            return grid_version;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public static JsonNode get_m_code(Model_Version version) {
        try {

            Model_Blob fileRecord = Model_Blob.find.query().where().eq("version.id", version.id).eq("name", "grid_program.json").findOne();

            if (fileRecord != null) {
                JsonNode json = Json.parse(fileRecord.get_fileRecord_from_Azure_inString());
                return json.get("m_code");
            }

            return Json.newObject();

        } catch (Exception e) {
            logger.internalServerError(e);
            return Json.newObject();
        }
    }

    @JsonIgnore
    public List<Swagger_M_Program_Version_Interface> program_versions_interface() {
        try {

            List<Swagger_M_Program_Version_Interface> versions = new ArrayList<>();

            for (Model_Version v : getVersions()) {
                Swagger_M_Program_Version_Interface help = new Swagger_M_Program_Version_Interface();
                help.version = v;
                help.virtual_input_output = v.m_program_virtual_input_output;
                versions.add(help);
            }
            return versions;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        this.blob_link = grid_project.get_path()  + "/grid-programs/"  + UUID.randomUUID();

        if (grid_project.project_id() != null) new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_GridProject.class, grid_project.project_id(), grid_project.id))).start();

        super.save();

        if (grid_project != null) {
            grid_project.grid_programs_ids.add(id);
        }

        cache.put(this.id, this);
    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update :: Update object Id: {}",  this.id);

        super.update();

        if (grid_project.project != null) new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_GridProgram.class, grid_project.project_id(), id))).start();
    }


    @JsonIgnore @Override
    public boolean delete() {
        logger.debug("update :: Delete object Id: {} ", this.id);

        deleted = true;
        super.update();

        if (grid_project_id() != null) {
            Model_GridProject.getById(grid_project_id()).grid_programs_ids.remove(id);
        }

        cache.remove(id);

        if (grid_project.project != null) new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_GridProject.class, grid_project.project_id(), grid_project.id))).start();

        return false;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    private String blob_link;

    @JsonIgnore
    public String get_path() {
        return blob_link;
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs              = "read: If user have M_Project.read_permission = true, you can create M_program on this M_Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs            = "create: If user have M_Project.update_permission = true, you can create M_Program on this M_Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String read_qr_token_permission_docs     = "read: Private settings for M_Program";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public boolean create_permission() {

        if (BaseController.person().has_permission("GridProgram_create")) return true;
        return grid_project != null && grid_project.update_permission();
    }

    @JsonProperty
    public boolean update_permission() {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("grid_program_update_" + id)) return BaseController.person().has_permission("grid_program_update_"+ id);
        if (BaseController.person().has_permission("M_Program_update")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_GridProgram.find.query().where().eq("grid_project.project.participants.person.id", BaseController.person().id).eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("grid_program_update_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("grid_program_update_" + id, false);
        return false;
    }

    @JsonIgnore
    public boolean read_permission() {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("grid_program_read_" + id)) return BaseController.person().has_permission("grid_program_read_"+ id);
        if (BaseController.person().has_permission("GridProgram_read")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
        if (Model_GridProgram.find.query().where().eq("grid_project.project.participants.person.id", BaseController.person().id).eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("grid_program_read_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("grid_program_read_" + id, false);
        return false;
    }

    @JsonProperty
    public boolean edit_permission() {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("grid_program_edit_" + id)) return BaseController.person().has_permission("grid_program_edit_"+ id);
        if (BaseController.person().has_permission("GridProgram_edit")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_GridProgram.find.query().where().eq("grid_project.project.participants.person.id", BaseController.person().id).eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("grid_program_edit_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("edit_" + id, false);
        return false;
    }

    @JsonProperty
    public boolean delete_permission() {
        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("grid_program_delete_" + id)) return BaseController.person().has_permission("grid_program_delete_"+ id);
        if (BaseController.person().has_permission("GridProgram_delete")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_GridProgram.find.query().where().eq("grid_project.project.participants.person.id", BaseController.person().id).eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("grid_program_delete_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("grid_program_delete_" + id, false);
        return false;
    }

    public enum Permission {GridProgram_create, GridProgram_read, GridProgram_edit, GridProgram_delete}


/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(Model_GridProgram.class)
    public static Cache<UUID, Model_GridProgram> cache;

    public static Model_GridProgram getById(String id) {
        return getById(UUID.fromString(id));
    }
    
    public static Model_GridProgram getById(UUID id) {

        Model_GridProgram m_program = cache.get(id);
        if (m_program == null) {

            m_program = Model_GridProgram.find.byId(id);
            if (m_program == null) return null;

            cache.put(id, m_program);
        }

        return m_program;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_GridProgram> find = new Finder<>(Model_GridProgram.class);
}