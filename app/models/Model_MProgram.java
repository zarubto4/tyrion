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
import utilities.swagger.output.Swagger_M_Program_Version;
import utilities.swagger.output.Swagger_M_Program_Version_Interface;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@ApiModel(value = "MProgram", description = "Model of M_Program")
@Table(name = "MProgram")
public class Model_MProgram extends TaggedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_MProgram.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                                                  public Model_MProject m_project;
    @JsonIgnore @OneToMany(mappedBy="m_program", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_Version> versions = new ArrayList<>();

/* JSON PROPERTY VALUES ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public UUID m_project_id() {
        return m_project.id;
    }

    @JsonProperty @ApiModelProperty(required = true)
    public List<Swagger_M_Program_Version> program_versions() {

        List<Swagger_M_Program_Version> versions = new ArrayList<>();

        for (Model_Version v : getVersions_not_removed_by_person().stream().sorted((element1, element2) -> element2.created.compareTo(element1.created)).collect(Collectors.toList())) {
            versions.add(program_version(v));
        }

        return versions;
    }

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached public List<UUID> cache_version_ids = new ArrayList<>();

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public List<Model_Version> getVersions_not_removed_by_person() {
        try {

            if (cache_version_ids.isEmpty()) {

                List<Model_Version> versions =  Model_Version.find.query().where().eq("m_program.id", this.id).eq("deleted", false).order().desc("created").select("id").findList();

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
    
    public static Swagger_M_Program_Version program_version(Model_Version version) {
        try {

            Swagger_M_Program_Version m_program_versions = new Swagger_M_Program_Version();

            m_program_versions.version = version;
            m_program_versions.public_mode = version.public_version;

            m_program_versions.virtual_input_output = version.m_program_virtual_input_output;

            Model_Blob fileRecord = Model_Blob.find.query().where().eq("version.id", version.id).eq("name", "m_program.json").findOne();

            if (fileRecord != null) {

                JsonNode json = Json.parse(fileRecord.get_fileRecord_from_Azure_inString());
                m_program_versions.m_code = json.get("m_code").asText();

            }

            return m_program_versions;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public static JsonNode get_m_code(Model_Version version) {
        try {

            Model_Blob fileRecord = Model_Blob.find.query().where().eq("version.id", version.id).eq("name", "m_program.json").findOne();

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

            for (Model_Version v : getVersions_not_removed_by_person()) {
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

        this.azure_m_program_link = m_project.get_path()  + "/m-programs/"  + UUID.randomUUID();

        if (m_project.project_id() != null) new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_MProject.class, m_project.project_id(), m_project.id))).start();

        super.save();

        if (m_project != null) {
            m_project.m_programs_ids.add(id);
        }

        cache.put(this.id, this);
    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update :: Update object Id: {}",  this.id);

        super.update();

        if (m_project.project != null) new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_MProgram.class, m_project.project_id(), id))).start();
    }


    @JsonIgnore @Override
    public boolean delete() {
        logger.debug("update :: Delete object Id: {} ", this.id);

        deleted = true;
        super.update();

        if (m_project_id() != null) {
            Model_MProject.getById(m_project_id()).m_programs_ids.remove(id);
        }

        cache.remove(id);

        if (m_project.project != null) new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_MProject.class, m_project.project_id(), m_project.id))).start();

        return false;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    private String azure_m_program_link;

    @JsonIgnore
    public String get_path() {
        return  azure_m_program_link;
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs              = "read: If user have M_Project.read_permission = true, you can create M_program on this M_Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs            = "create: If user have M_Project.update_permission = true, you can create M_Program on this M_Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String read_qr_token_permission_docs     = "read: Private settings for M_Program";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public boolean create_permission() {

        if (BaseController.person().has_permission("MProgram_create")) return true;
        return m_project != null && m_project.update_permission();
    }

    @JsonProperty
    public boolean update_permission() {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("m_program_update_" + id)) return BaseController.person().has_permission("m_program_update_"+ id);
        if (BaseController.person().has_permission("M_Program_update")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_MProgram.find.query().where().eq("m_project.project.participants.person.id", BaseController.person().id).eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("m_program_update_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("m_program_update_" + id, false);
        return false;
    }

    @JsonIgnore
    public boolean read_permission() {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("m_program_read_" + id)) return BaseController.person().has_permission("m_program_read_"+ id);
        if (BaseController.person().has_permission("MProgram_read")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
        if (Model_MProgram.find.query().where().eq("m_project.project.participants.person.id", BaseController.person().id).eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("m_program_read_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("m_program_read_" + id, false);
        return false;
    }

    @JsonProperty
    public boolean edit_permission() {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("m_program_edit_" + id)) return BaseController.person().has_permission("m_program_edit_"+ id);
        if (BaseController.person().has_permission("MProgram_edit")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_MProgram.find.query().where().eq("m_project.project.participants.person.id", BaseController.person().id).eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("m_program_edit_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("edit_" + id, false);
        return false;
    }

    @JsonProperty
    public boolean delete_permission() {
        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("m_program_delete_" + id)) return BaseController.person().has_permission("m_program_delete_"+ id);
        if (BaseController.person().has_permission("MProgram_delete")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_MProgram.find.query().where().eq("m_project.project.participants.person.id", BaseController.person().id).eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("m_program_delete_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("m_program_delete_" + id, false);
        return false;
    }

    public enum Permission { MProgram_create, MProgram_read, MProgram_edit, MProgram_delete}


/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(Model_MProgram.class)
    public static Cache<UUID, Model_MProgram> cache;

    public static Model_MProgram getById(String id) {
        return getById(UUID.fromString(id));
    }
    
    public static Model_MProgram getById(UUID id) {

        Model_MProgram m_program = cache.get(id);
        if (m_program == null) {

            m_program = Model_MProgram.find.byId(id);
            if (m_program == null) return null;

            cache.put(id, m_program);
        }

        return m_program;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_MProgram> find = new Finder<>(Model_MProgram.class);
}