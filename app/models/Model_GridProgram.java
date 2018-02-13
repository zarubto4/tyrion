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
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.models_update_echo.EchoHandler;
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
    @JsonIgnore @OneToMany(mappedBy="grid_program", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_GridProgramVersion> versions = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached public List<UUID> cache_version_ids = new ArrayList<>();
    @JsonIgnore @Transient @Cached public UUID cache_grid_project_id = null;

/* JSON PROPERTY VALUES ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true) public List<Model_GridProgramVersion> program_versions() {

        List<Model_GridProgramVersion> versions = new ArrayList<>();

        for (Model_GridProgramVersion v : get_versions().stream().sorted((element1, element2) -> element2.created.compareTo(element1.created)).collect(Collectors.toList())) {
            versions.add(v);
        }

        return versions;
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public UUID get_grid_project_id() throws _Base_Result_Exception {

        if (cache_grid_project_id == null) {
            Model_GridProject project = Model_GridProject.find.query().where().eq("grid_programs.id", id).select("id").findOne();
            if (project == null) return null;
            cache_grid_project_id = project.id;
        }

        return cache_grid_project_id;
    }

    @JsonIgnore @Transient public Model_GridProject get_grid_project() throws _Base_Result_Exception  {
        return  Model_GridProject.getById(get_grid_project_id());
    }

    @JsonIgnore @Transient public List<Model_GridProgramVersion> get_versions() {
        try {

            if (cache_version_ids.isEmpty()) {

                List<Model_GridProgramVersion> versions =  Model_GridProgramVersion.find.query().where().eq("grid_program.id", this.id).order().desc("created").select("id").findList();

                // Získání seznamu
                for (Model_GridProgramVersion version : versions) {
                    cache_version_ids.add(version.id);
                }
            }

            List<Model_GridProgramVersion> versions  = new ArrayList<>();

            for (UUID version_id : cache_version_ids) {
                versions.add(Model_GridProgramVersion.getById(version_id));
            }

            return versions;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }



    @JsonIgnore @Transient public static JsonNode get_m_code(Model_GridProgramVersion version) {
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

    @JsonIgnore @Transient public List<Swagger_M_Program_Version_Interface> program_versions_interface() {
        try {

            List<Swagger_M_Program_Version_Interface> versions = new ArrayList<>();

            for (Model_GridProgramVersion v : get_versions()) {
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

        super.save();

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo( Model_Project.class, grid_project.get_project_id(), grid_project.id));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

        if (grid_project != null) {
            grid_project.grid_programs_ids.add(id);
        }

        cache.put(this.id, this);
    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update :: Update object Id: {}",  this.id);

        super.update();

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo( Model_GridProgram.class, get_grid_project().get_project_id(), id));
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
            get_grid_project().grid_programs_ids.remove(id);
        }catch (_Base_Result_Exception e){
            // Nothing
        }

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo( Model_GridProject.class, get_grid_project().get_project_id(), get_grid_project_id()));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

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

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void  check_create_permission() throws _Base_Result_Exception {
        if (BaseController.person().has_permission(Permission.GridProgram_create.name())) return;
        grid_project.check_update_permission();
    }

    @JsonProperty
    public void check_update_permission() throws _Base_Result_Exception  {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("grid_program_update_" + id)) BaseController.person().valid_permission("grid_program_update_" + id);
        if (BaseController.person().has_permission(Permission.GridProgram_update.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_GridProgram.find.query().where().eq("grid_project.project.participants.person.id", BaseController.person().id).eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("grid_program_update_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("grid_program_update_" + id, false);
        throw new Result_Error_PermissionDenied();
    }

    @JsonIgnore
    public void check_read_permission() throws _Base_Result_Exception {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("grid_program_read_" + id)) BaseController.person().valid_permission("grid_program_read_" + id);
        if (BaseController.person().has_permission(Permission.GridProgram_read.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
        if (Model_GridProgram.find.query().where().eq("grid_project.project.participants.person.id", BaseController.person().id).eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("grid_program_read_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("grid_program_read_" + id, false);
        throw new Result_Error_PermissionDenied();
    }

    @JsonProperty
    public void check_delete_permission() throws _Base_Result_Exception  {
        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("grid_program_delete_" + id)) BaseController.person().valid_permission("grid_program_delete_" + id);
        if (BaseController.person().has_permission(Permission.GridProgram_delete.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_GridProgram.find.query().where().eq("grid_project.project.participants.person.id", BaseController.person().id).eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("grid_program_delete_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("grid_program_delete_" + id, false);
        throw new Result_Error_PermissionDenied();
    }

    public enum Permission {GridProgram_create, GridProgram_update, GridProgram_read, GridProgram_edit, GridProgram_delete}


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