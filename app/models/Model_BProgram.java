package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import org.ehcache.Cache;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.models_update_echo.EchoHandler;
import utilities.swagger.output.Swagger_B_Program_Version;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@ApiModel(value = "BProgram", description = "Model of BProgram")
@Table(name="BProgram")
public class Model_BProgram extends TaggedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_BProgram.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Project project;
    @JsonIgnore @OneToMany(mappedBy="b_program", cascade=CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_Version> versions = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached public List<UUID> cache_version_ids = new ArrayList<>();
    @JsonIgnore @Transient @Cached private UUID cache_project_id;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty
    public UUID project_id() {

        if (cache_project_id == null) {
            Model_Project project = Model_Project.find.query().where().eq("b_programs.id", id).select("id").findOne();
            cache_project_id = project.id;
        }

        return cache_project_id;
    }

    @JsonProperty
    public List<Swagger_B_Program_Version> program_versions() {
        try {

            List<Swagger_B_Program_Version> versions = new ArrayList<>();

            for (Model_Version version : getVersions().stream().sorted((element1, element2) -> element2.created.compareTo(element1.created)).collect(Collectors.toList())) {
                versions.add(this.program_version(version));
            }

            return versions;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    // TODO bude asi v instanci
    /*@JsonProperty @Transient public Swagger_B_Program_State instance_details() {
        try {

            Swagger_B_Program_State state = new Swagger_B_Program_State();

            state.online_state = Model_HomerInstance.getById(instance_id()).online_state();

            if (Server.server_mode == Enum_Tyrion_Server_mode.developer && instance().getCurrentSnapshot() != null) {
                // /#token - frontend pouze nahradí substring - můžeme tedy do budoucna za adresu přidávat další parametry
                state.instance_remote_url = "ws://" + Model_HomerServer.getById(instance().server_id()).get_WebView_APP_URL() + instance_id() + "/#token";
            } else {
                state.instance_remote_url = "wss://" + Model_HomerServer.getById(instance().server_id()).get_WebView_APP_URL()  + instance_id() + "/#token";
            }

            if (instance().getCurrentSnapshot() != null) {
                // Jaká verze Blocko Programu je aktuální?
                state.version_id = instance().getCurrentSnapshot().get_b_program_version().id;
                state.name = instance().getCurrentSnapshot().get_b_program_version().name;

                // Vracím naposledy použitou - Becki si to vyřeší sama
            } else if (!instance().instance_history.isEmpty()) {
                state.version_id = instance().instance_history.get(0).get_b_program_version().id;
                state.name = instance().instance_history.get(0).get_b_program_version().name;
            }

            // Instnace ID
            state.instance_id = instance_id();

            // Informace o Serveru
            state.server_id = instance().server_id();
            state.server_name = instance().server_name();
            state.server_online_state = instance().server_online_state();

            return state;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }*/

/* GET Variable short type of objects ----------------------------------------------------------------------------------*/

    // Objekt určený k vracení verze
    @JsonIgnore
    public Swagger_B_Program_Version program_version(Model_Version version) {

        Swagger_B_Program_Version b_program_version = new Swagger_B_Program_Version();

        b_program_version.version           = version;

        b_program_version.remove_permission = delete_permission();
        b_program_version.edit_permission   = edit_permission();

        b_program_version.m_project_program_snapshots = version.b_program_version_snapshots;

        Model_Blob blob = Model_Blob.find.query().where().eq("version.id", version.id).eq("name", "blocko.json").findOne();
        if (blob != null) b_program_version.program = blob.get_fileRecord_from_Azure_inString();

        return b_program_version;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public List<Model_Version> getVersions() {
        try {

            if (cache_version_ids.isEmpty()) {

                List<Model_Version> versions =  Model_Version.find.query().where().eq("b_program.id", id).order().desc("created").select("id").findList();

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

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {
        
        this.azure_b_program_link = project.getPath() + "/b-programs/"  + UUID.randomUUID().toString();

        project.cache_b_program_ids.add(this.id);

        super.save();

        cache.put(this.id, this);

        if (project_id() != null) {
            new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_Project.class, project_id(), project_id()))).start();
        }
    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update - Update object Id: {}",  this.id);

        super.update();

        if (project_id() != null) {
            new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_BProgram.class, project_id(), id))).start();
        }
    }

    @JsonIgnore @Override
    public boolean delete() {

        if (project_id() != null) {
            Model_Project.getById(project_id()).cache_b_program_ids.remove(id);
        }

        cache.remove(id);

        super.update();

        if (project_id() != null) {
            //new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_BProgram.class, project_id(), project_id()))).start();
        }
        return false;
    }

/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore private String azure_b_program_link;

    @JsonIgnore
    public String get_path() {
        return azure_b_program_link;
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_BProgram.class)
    public static Cache<UUID, Model_BProgram> cache;

    public static Model_BProgram getById(String id) {
        return getById(UUID.fromString(id));
    }

    public static Model_BProgram getById(UUID id) {

        Model_BProgram b_program = cache.get(id);
        if (b_program == null) {

            b_program = find.byId(id);
            if (b_program == null) return null;

            cache.put(id, b_program);
        }

        return b_program;
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public boolean create_permission() {

        if (BaseController.person().has_permission("BProgram_create")) return true;
        return project != null && project.update_permission();
    }

    @JsonProperty
    public boolean update_permission() {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("b_program_update_" + id)) return BaseController.person().has_permission("b_program_update_"+ id);
        if (BaseController.isPermitted("BProgram_update")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if (Model_BProgram.find.query().where().eq("project.participants.person.id", BaseController.personId()).eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("b_program_update_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("update_" + id, false);
        return false;
    }

    @JsonIgnore
    public boolean read_permission() {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("b_program_read_" + id)) return BaseController.person().has_permission("b_program_read_"+ id);
        if (BaseController.person().has_permission("BProgram_read")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
        if (Model_BProgram.find.query().where().eq("project.participants.person.id", BaseController.personId()).eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("b_program_read_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("read_" + id, false);
        return false;

    }
    @JsonProperty
    public boolean edit_permission() {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("b_program_edit_" + id)) return BaseController.person().has_permission("b_program_edit_"+ id);
        if (BaseController.person().has_permission("BProgram_edit")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if (Model_BProgram.find.query().where().eq("project.participants.person.id", BaseController.personId()).eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("b_program_edit_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("edit_" + id, false);
        return false;
    }

    @JsonProperty
    public boolean delete_permission() {
        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("b_program_delete_" + id)) return BaseController.person().has_permission("b_program_delete_"+ id);
        if (BaseController.person().has_permission("BProgram_delete")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if (Model_BProgram.find.query().where().eq("project.participants.person.id", BaseController.personId()).eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("b_program_delete_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("b_program_delete_" + id, false);
        return false;
     }

     // Statické univerzální klíče
    public enum Permission { BProgram_create, BProgram_read, BProgram_update, BProgram_edit, BProgram_delete }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
     
    public static Finder<UUID, Model_BProgram> find = new Finder<>(Model_BProgram.class);
}

