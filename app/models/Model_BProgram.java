package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import org.ehcache.Cache;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.models_update_echo.EchoHandler;
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
    @JsonIgnore @OneToMany(mappedBy="b_program", cascade=CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_BProgramVersion> versions = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="b_program", cascade=CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_Instance>   instancies    = new ArrayList<>(); // Dont used that, its only short reference fo rnew Instance
/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached public List<UUID> cache_version_ids = new ArrayList<>();
    @JsonIgnore @Transient @Cached private UUID cache_project_id;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty
    public List<Model_BProgramVersion> program_versions() {
        try {

            List<Model_BProgramVersion> versions = new ArrayList<>();

            for (Model_BProgramVersion version : get_versions().stream().sorted((element1, element2) -> element2.created.compareTo(element1.created)).collect(Collectors.toList())) {
                versions.add(version);
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

    @JsonIgnore @Transient public UUID get_project_id() throws _Base_Result_Exception  {

        if (cache_project_id == null) {
            Model_Project project = Model_Project.find.query().where().eq("b_programs.id", id).select("id").findOne();
            if (project == null) return null;
            cache_project_id = project.id;
        }

        return cache_project_id;
    }

    @JsonIgnore @Transient public Model_Project get_project() throws _Base_Result_Exception  {
        return  Model_Project.getById(get_project_id());
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public List<Model_BProgramVersion> get_versions() {
        try {

            if (cache_version_ids.isEmpty()) {

                List<UUID> uuid_ids =  Model_BProgramVersion.find.query().where().eq("b_program.id", id).order().desc("created").findIds();

                // Získání seznamu
                for (UUID uuid_id : uuid_ids) {
                    cache_version_ids.add(uuid_id);
                }
            }

            List<Model_BProgramVersion> versions  = new ArrayList<>();

            for (UUID version_id : cache_version_ids) {
                versions.add(Model_BProgramVersion.getById(version_id));
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

        new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_Project.class, project.id, project.id))).start();

    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update - Update object Id: {}",  this.id);

        super.update();

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo( Model_BProgram.class, get_project_id(), id));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

    }

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("update :: Delete object Id: {} ", this.id);
        super.delete();

        // Remove from Project Cache
        try {
            get_project().cache_b_program_ids.remove(id);
        } catch (_Base_Result_Exception e) {
            // Nothing
        }

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo( Model_Project.class, get_project_id(), get_project_id()));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();


        return false;
    }

/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore private String azure_b_program_link;

    @JsonIgnore
    public String get_path() {
        return azure_b_program_link;
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override @Transient
    public void check_create_permission() throws _Base_Result_Exception {
        if (_BaseController.person().has_permission(Permission.BProgram_create.name())) return;
        project.check_update_permission();
    }

    @JsonIgnore @Override @Transient
    public void check_update_permission() throws _Base_Result_Exception {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (_BaseController.person().has_permission("b_program_update_" + id)) _BaseController.person().valid_permission("b_program_update_" + id);
        if (_BaseController.person().has_permission(Permission.BProgram_update.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if (Model_BProgram.find.query().where().eq("project.participants.person.id", _BaseController.personId()).eq("id", id).findCount() > 0) {
            _BaseController.person().cache_permission("b_program_update_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        _BaseController.person().cache_permission("update_" + id, false);
        throw new Result_Error_PermissionDenied();
    }

    @JsonIgnore @Override @Transient
    public void check_read_permission() throws _Base_Result_Exception  {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (_BaseController.person().has_permission("b_program_read_" + id)) _BaseController.person().valid_permission("b_program_read_" + id);
        if (_BaseController.person().has_permission(Permission.BProgram_read.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
        if (Model_BProgram.find.query().where().eq("project.participants.person.id", _BaseController.personId()).eq("id", id).findCount() > 0) {
            _BaseController.person().cache_permission("b_program_read_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        _BaseController.person().cache_permission("read_" + id, false);
        throw new Result_Error_PermissionDenied();

    }

    @JsonIgnore @Override @Transient
    public void check_delete_permission() throws _Base_Result_Exception {
        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (_BaseController.person().has_permission("b_program_delete_" + id)) _BaseController.person().valid_permission("b_program_delete_" + id);
        if (_BaseController.person().has_permission(Permission.BProgram_delete.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if (Model_BProgram.find.query().where().eq("project.participants.person.id", _BaseController.personId()).eq("id", id).findCount() > 0) {
            _BaseController.person().cache_permission("b_program_delete_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        _BaseController.person().cache_permission("b_program_delete_" + id, false);
        throw new Result_Error_PermissionDenied();
     }

     // Statické univerzální klíče
    public enum Permission { BProgram_create, BProgram_read, BProgram_update, BProgram_edit, BProgram_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_BProgram.class)
    public static Cache<UUID, Model_BProgram> cache;

    public static Model_BProgram getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }

    public static Model_BProgram getById(UUID id) throws _Base_Result_Exception {

        Model_BProgram b_program = cache.get(id);
        if (b_program == null) {

            b_program = find.byId(id);
            if (b_program == null) throw new Result_Error_NotFound(Model_BProgram.class);

            cache.put(id, b_program);
        }

        // Check Permission
        b_program.check_read_permission();
        return b_program;
    }


    /* FINDER --------------------------------------------------------------------------------------------------------------*/
     
    public static Finder<UUID, Model_BProgram> find = new Finder<>(Model_BProgram.class);

}

