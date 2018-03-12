package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers._BaseController;
import io.ebean.Expr;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.libs.Json;
import utilities.Server;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.enums.*;
import utilities.errors.ErrorCode;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.swagger.input.Swagger_InstanceSnapShotConfiguration;
import utilities.swagger.input.Swagger_InstanceSnapShotConfigurationFile;
import utilities.swagger.input.Swagger_InstanceSnapShotConfigurationProgram;
import utilities.swagger.output.Swagger_Short_Reference;
import websocket.interfaces.WS_Homer;
import websocket.messages.homer_hardware_with_tyrion.*;
import websocket.messages.homer_instance_with_tyrion.verification.WS_Message_Grid_token_verification;
import websocket.messages.homer_instance_with_tyrion.verification.WS_Message_WebView_token_verification;
import websocket.messages.tyrion_with_becki.WS_Message_Online_Change_status;
import websocket.messages.homer_instance_with_tyrion.*;

import javax.persistence.*;
import java.util.*;

@Entity
@ApiModel(description = "Model of Instance", value = "Instance")
@Table(name="Instance")
public class Model_Instance extends TaggedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    public static final Logger logger = new Logger(Model_Instance.class);
    
/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore public UUID current_snapshot_id;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_HomerServer server_main;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_HomerServer server_backup;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Project project;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_BProgram b_program; // Only first reference!

    @OneToMany(mappedBy = "instance", cascade = CascadeType.ALL) @OrderBy("created DESC")
    public List<Model_InstanceSnapshot> snapshots = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private String cache_server_name;
    @JsonIgnore @Transient @Cached private UUID cache_server_main_id;
    @JsonIgnore @Transient @Cached private UUID cache_server_backup_id;
    @JsonIgnore @Transient @Cached private UUID cache_project_id;
    @JsonIgnore @Transient @Cached private UUID cache_b_program_id;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/


    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public Swagger_Short_Reference b_program(){
        try {

            Model_BProgram b_rpogram = Model_BProgram.getById(get_b_program_id());
            return new Swagger_Short_Reference(b_rpogram.id, b_rpogram.name, b_rpogram.description);

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public Model_HomerServer server(){
        try {

            Model_HomerServer server = Model_HomerServer.getById(server_id());
            return server;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public Model_InstanceSnapshot current_snapshot() throws _Base_Result_Exception  {

        if (this.current_snapshot_id != null) {
            Model_InstanceSnapshot snapshot = Model_InstanceSnapshot.getById(this.current_snapshot_id);
            if (snapshot != null) {
                return snapshot;
            }
        }
        return null;
    }


    @JsonProperty @ApiModelProperty(required = true)
    public NetworkStatus online_state() {

        // Pokud Tyrion nezná server ID - to znamená deska se ještě nikdy nepřihlásila - chrání to proti stavu "během výroby"
        // i stavy při vývoji kdy se tvoří zběsile nové desky na dev serverech
        if (current_snapshot() == null) {

            if (snapshots.isEmpty()) {
                return NetworkStatus.NOT_YET_FIRST_CONNECTED;
            } else {
                return NetworkStatus.SHUT_DOWN;
            }
        }

        // Pokud je server offline - tyrion si nemuže být jistý stavem hardwaru - ten teoreticky muže být online
        // nebo také né - proto se vrací stav Enum_Online_status - na to reaguje parameter latest_online(),
        // který následně vrací latest know online
        try {

            if ((Model_HomerServer.getById(server_id()) != null) && Model_HomerServer.getById(server_id()).online_state() == NetworkStatus.ONLINE) {

                if (cache_status.containsKey(id)) {
                    return cache_status.get(id) ? NetworkStatus.ONLINE : NetworkStatus.OFFLINE;
                }

                //else {
                    // Začnu zjišťovat stav - v separátním vlákně! Po strátě spojení se serverem nebo po načítání se někde opomělo změnit stav na online proto
                    // je podmínka zakomentovaná aby se to ověřovalo vždy
                    // TODO ASAP ošetřit!!!!
                    new Thread(() -> {
                        try {

                            WS_Message_Instance_status status = get_instance_status();

                            if (status.status.equals("success")) cache_status.put(id, status.get_status(id).status);
                            WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Instance.class, this.id, status.get_status(id).status, get_project_id());

                        } catch (Exception e) {
                            logger.internalServerError(e);
                        }
                    }).start();

                    return NetworkStatus.SYNCHRONIZATION_IN_PROGRESS;
                //}

            } else {
                return NetworkStatus.UNKNOWN_LOST_CONNECTION_WITH_SERVER;
            }
        } catch (Exception e) {
            
            // Záměrný Exception - Občas se nedosynchronizuje Cach - ale system stejnak po zvalidování dorovná stav
           return NetworkStatus.UNKNOWN_LOST_CONNECTION_WITH_SERVER;
        }
    }

    @JsonProperty @ApiModelProperty(required = true)
    public NetworkStatus server_online_state() throws _Base_Result_Exception  {
        return Model_HomerServer.getById(server_id()).online_state();
    }

    @JsonProperty @ApiModelProperty(required = true)
    public String instance_remote_url() {
        try {

            if (current_snapshot() != null) {

                if (Server.mode == ServerMode.DEVELOPER) {
                    return "ws://" + Model_HomerServer.getById(server_id()).server_url + ":" + Model_HomerServer.getById(server_id()).web_view_port + "/" + id + "/#token";
                } else {
                    return "wss://" + Model_HomerServer.getById(server_id()).server_url + ":" + Model_HomerServer.getById(server_id()).web_view_port + "/" + id + "/#token";
                }
            }
            return null;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* GET Variable short type of objects ----------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


    @JsonIgnore
    public UUID get_project_id() throws _Base_Result_Exception {

        if (cache_project_id == null) {

            Model_Project project = Model_Project.find.query().where().eq("instances.id", id).select("id").findOne();
            if (project == null) throw new Result_Error_NotFound(Model_Project.class);

            cache_project_id = project.id;
            return project.id;
        }

        return cache_project_id;
    }


    @JsonIgnore
    public UUID get_b_program_id() throws _Base_Result_Exception {

        if(cache_b_program_id == null) {

            cache_b_program_id  = Model_BProgram.find.query().where().eq("instances.id", id).select("id").findSingleAttribute();
            if (cache_b_program_id == null) throw new Result_Error_NotFound(Model_BProgram.class);

            return  cache_b_program_id;
        }

        return cache_b_program_id;
    }

    @JsonIgnore
    public Model_Project get_project() throws _Base_Result_Exception {

        if (cache_project_id == null) {
            return Model_Project.getById(get_project_id());
        }else {
            return Model_Project.getById(cache_project_id);
        }
    }



    @JsonIgnore
    public List<String> getHardwareFullIds() throws _Base_Result_Exception  {

        return current_snapshot().getHardwareFullIds();
    }

    @JsonIgnore
    public UUID server_id()                {
        try {

            if (cache_server_main_id == null) {
                Model_HomerServer homer_server = Model_HomerServer.find.query().where().eq("instances.id", id).select("id").findOne();

                if (homer_server == null) {
                    logger.warn("server_id - instance has not set default server");

                    this.server_main = Model_HomerServer.find.query().where().eq("server_type", HomerType.MAIN.name()).findOne();
                    if (this.server_main == null) {
                        throw new Exception("Main server is not set! Cannot set Default Server to instance!");
                    }
                    this.update();

                    // For Cache * next lines of codes
                    homer_server = this.server_main;
                }

                cache_server_main_id = homer_server.id;
            }
            return cache_server_main_id;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON Override  Method -----------------------------------------------------------------------------------------*/

    @Override
    public void save() {

        logger.debug("save - saving to database, id: {}", this.id);

        super.save();

        if (project != null) {
            try {
                Model_Project.getById(this.get_project_id()).cache_instance_ids.add(id);
            }catch (Exception e) {
                // Nothing
            }
        }

        cache.put(this.id, this);
    }

    @Override
    public void update() {

        logger.debug("update - updating in database, id: {}",  this.id);

        super.update();

        if (cache.containsKey(this.id)) {
            cache.replace(this.id, this);
        } else {
            cache.put(this.id, this);
        }
    }
    
    @Override
    public boolean delete() {

        logger.debug("delete - deleting from database, id: {} ", this.id);
        
        this.deleted = true;
        super.update();


        try {
            Model_Project.getById(get_project_id()).cache_instance_ids.remove(id);
        } catch (Exception e) {
            // Nothing
        }

        if (cache.containsKey(this.id)) {
            cache.remove(this.id);
        }

        return false;
    }

    public void cache_refresh() {
        this.refresh();
        if (cache.containsKey(this.id)) {
            cache.replace(this.id, this);
        } else {
            cache.put(this.id, this);
        }
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* INSTANCE WEBSOCKET CONTROLLING ON HOMER SERVER-----------------------------------------------------------------------*/

    public static final String CHANNEL = "instance";

    //-- Messenger - Parsarer of Incoming Messages -- //
    @JsonIgnore
    public static void Messages(WS_Homer homer, ObjectNode json) {
        new Thread(() -> {
            try {

                switch (json.get("message_type").asText()) {

                    case WS_Message_Grid_token_verification.message_type: {

                        WS_Message_Grid_token_verification help = baseFormFactory.formFromJsonWithValidation(WS_Message_Grid_token_verification.class, json);
                        help.get_instance().cloud_verification_token_GRID(homer, help);
                        return;
                    }

                    case WS_Message_WebView_token_verification.messageType: {

                        WS_Message_WebView_token_verification help = baseFormFactory.formFromJsonWithValidation(WS_Message_WebView_token_verification.class, json);
                        help.get_instance().cloud_verification_token_WEBVIEW(homer, help);
                        return;
                    }

                    // Ochrana proti zacyklení
                    case WS_Message_Instance_set_hardware.message_type           : {logger.warn("WS_Message_Instance_device_set_snap: A message with a very high delay has arrived.");return;}
                    case WS_Message_Instance_status.message_type                    : {logger.warn("WS_Message_Instance_status: A message with a very high delay has arrived.");return;}
                    case WS_Message_Instance_set_terminals.message_type         : {logger.warn("WS_Message_Instance_terminal_set_snap: A message with a very high delay has arrived.");return;}
                    case WS_Message_Instance_set_program.message_type     : {logger.warn("WS_Message_Instance_upload_blocko_program: A message with a very high delay has arrived.");return;}

                    default: {

                        logger.warn("Incoming Message not recognized::" + json.toString());

                        // Zarážka proti nevadliní odpovědi a zacyklení
                        if (json.has("status") && json.get("status").asText().equals("error")) {
                            return;
                        }

                        homer.send(json.put("error_message", "message_type not recognized").put("error_code", 400));
                    }
                }

            } catch (Exception e) {

                if (!json.has("message_type")) {
                    homer.send(json.put("error_message", "Your message not contains message_type").put("error_code", 400));
                    return;
                } else {
                    logger.internalServerError(e);
                }
            }
        }).start();
    }

    // Odesílání zprávy harwaru jde skrze serve, zde je metoda, která pokud to nejde odeslat naplní objekt a vrácí ho
    @JsonIgnore
    public ObjectNode write_with_confirmation(ObjectNode json, Integer time, Integer delay, Integer number_of_retries) {

        // Response with Error Message
        if (this.server_id() == null) {

            ObjectNode request = Json.newObject();
            request.put("message_type", json.get("message_type").asText());
            request.put("message_channel", Model_Instance.CHANNEL);
            request.put("error_code", ErrorCode.HOMER_SERVER_NOT_SET_FOR_INSTANCE.error_code());
            request.put("error_message", ErrorCode.HOMER_SERVER_NOT_SET_FOR_INSTANCE.error_message());
            request.put("message_id", json.has("message_id") ? json.get("message_id").asText() : "unknown");
            request.put("websocket_identificator", "00000000-0000-4000-A000-000000000000");

            return request;
        }

        Model_HomerServer server = Model_HomerServer.getById(this.server_id());
        if (server == null) {

            logger.internalServerError(new Exception("write_with_confirmation:: Instance " + id + ". Server id " + this.server_id() + " not exist!"));

            ObjectNode request = Json.newObject();
            request.put("message_type", json.get("message_type").asText());
            request.put("message_channel", Model_Instance.CHANNEL);
            request.put("error_code", ErrorCode.HOMER_NOT_EXIST.error_code());
            request.put("error_message", ErrorCode.HOMER_NOT_EXIST.error_message());
            request.put("message_id", json.has("message_id") ? json.get("message_id").asText() : "unknown");
            request.put("websocket_identificator", "00000000-0000-4000-A000-000000000000");

            return request;
        }

        json.put("instance_id", id.toString());
        return server.write_with_confirmation(json, time, delay, number_of_retries);
    }

    // Metoda překontroluje odeslání a pak předává objektu - zpráva plave skrze program
    @JsonIgnore
    private void write_without_confirmation(ObjectNode json) {

        Model_HomerServer server = Model_HomerServer.getById(this.server_id());
        if (server == null) {

            logger.internalServerError(new Exception("write_without_confirmation:: Instance " + id + " server not found"));

            return;
        }

        json.put("instance_id", id.toString());
        server.write_without_confirmation(json);

    }

    // Metoda překontroluje odeslání a pak předává objektu - zpráva plave skrze program
    @JsonIgnore
    public void write_without_confirmation(String message_id, ObjectNode json) {

        if (this.server_id() == null) {
            return;
        }

        Model_HomerServer server = Model_HomerServer.getById(this.server_id());

        if (server == null) {
            logger.internalServerError(new Exception("write_without_confirmation:: Instance " + id + " server not found"));
            return;
        }

        json.put("instance_id", id.toString());
        server.write_without_confirmation(message_id, json);
    }

    //-- Instance Status -- //
    @JsonIgnore
    public WS_Message_Instance_status get_instance_status() {
        try {

            JsonNode json = write_with_confirmation(new WS_Message_Instance_status().make_request(Collections.singletonList(id.toString())), 1000 * 3, 0, 2);

           return baseFormFactory.formFromJsonWithValidation(WS_Message_Instance_status.class, json);

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Instance_status();
        }
    }

    //-- Device IO operations -- //
    @JsonIgnore
    public WS_Message_Instance_set_hardware set_device_to_instance(List<String> full_ids) {
        try {

            JsonNode json = this.write_with_confirmation(new WS_Message_Instance_set_hardware().make_request(full_ids), 1000*3, 0, 4);
            return baseFormFactory.formFromJsonWithValidation(WS_Message_Instance_set_hardware.class, json);

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Instance_set_hardware();
        }
    }

    //-- Terminal IO operations -- //
    @JsonIgnore
    public WS_Message_Instance_set_terminals setTerminals(List<UUID> terminalIds) {
        try {

            JsonNode json = this.write_with_confirmation(new WS_Message_Instance_set_terminals().make_request(terminalIds), 1000*3, 0, 4);

            return baseFormFactory.formFromJsonWithValidation(WS_Message_Instance_set_terminals.class, json);

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Instance_set_terminals();
        }
    }

    //-- Instance Summary Information --//
    @JsonIgnore
    public WS_Message_Hardware_overview get_hardware_overview() {
        try {

            ObjectNode json = this.write_with_confirmation( new WS_Message_Hardware_overview().make_request(this.getHardwareFullIds() ), 1000*5, 0, 1);

            return baseFormFactory.formFromJsonWithValidation(WS_Message_Hardware_overview.class, json);

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Hardware_overview();
        }
    }

    //-- Helper Commands --//
    @JsonIgnore
    public void deploy() {
        current_snapshot().deploy();
    }

    @JsonIgnore
    public void stop() {

        cache_status.put(this.id, false);
        WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Instance.class, this.id, true, get_project_id());

        if (current_snapshot() != null) {
            current_snapshot().stop();
            this.current_snapshot_id = null;
            this.update();
        }

        Model_HomerServer server = Model_HomerServer.getById(server_id());
        if (server == null) {
            return;
        }

        server.remove_instance(Collections.singletonList(id));
    }

    //-- Verification --//
    @JsonIgnore
    public void cloud_verification_token_GRID(WS_Homer homer, WS_Message_Grid_token_verification help) {
        try {

            logger.debug("cloud_verification_token_GRID::  Checking Token");
            logger.debug("cloud_verification_token_GRID::  Token:: {} ", help.token);
            logger.debug("cloud_verification_token_GRID::  Instance ID:: {} ", help.instance_id);
            logger.debug("cloud_verification_token_GRID::  App ID:: {}", help.grid_app_id);

            Model_GridTerminal terminal = Model_GridTerminal.find.query().where().eq("terminal_token", help.token).findOne();

            // Pokud je terminall null - nikdy se uživatel nepřihlásit a nevytvořil se o tom záznam - ale to stále neznamená že není možno povolit přístup
            if (terminal == null) {

                System.out.println("terminal == null");
                // Najít c configuráku token

                Swagger_InstanceSnapShotConfiguration settings = this.current_snapshot().settings();
                Swagger_InstanceSnapShotConfigurationFile collection = null;
                Swagger_InstanceSnapShotConfigurationProgram program = null;


                if(settings == null){
                    logger.error("SnapShotConfiguration is missing return null");
                    throw new Result_Error_NotFound(Swagger_InstanceSnapShotConfiguration.class);
                }

                for(Swagger_InstanceSnapShotConfigurationFile grids_collection : settings.grids_collections){
                    for(Swagger_InstanceSnapShotConfigurationProgram grids_program : collection.grid_programs){
                        if(grids_program.grid_program_id == help.grid_app_id){
                            collection = grids_collection;
                            program = grids_program;
                            break;
                        }
                    }
                }

                if(collection == null){
                    logger.error("SnapShotConfigurationFile is missing return null");
                    throw new Result_Error_NotFound(Swagger_InstanceSnapShotConfigurationFile.class);
                }

                if(program == null){
                    logger.error("SnapShotConfigurationFileProgram is missing return null");
                    throw new Result_Error_NotFound(Swagger_InstanceSnapShotConfigurationProgram.class);
                }


                logger.debug("Enum_MProgram_SnapShot_settings: {}", program.snapshot_settings);

                switch (program.snapshot_settings) {

                    case PUBLIC: {
                        System.out.println("Je to plnohodnotně public");
                        homer.send(help.get_result(true));
                        return;
                    }

                    case PROJECT: {
                        System.out.println("Pouze pro registrované v projektu ale jekilož neexistuje přihlášení nelze připojit???");
                        homer.send(help.get_result(false));
                        return;
                    }

                    case TESTING:{
                        System.out.println("Grid se snaží připojit na něco co není instancí!");
                        homer.send(help.get_result(false));
                        return;
                    }

                    default: {
                        System.out.println("parameter.snapshot_settings() default parameter!! return FALSE");
                        homer.send(help.get_result(false));
                    }
                }
            } else {

                System.out.println("terminal != null");
                logger.debug("cloud_verification_token_GRID::  Person id:: {}", terminal.person.id);
                logger.debug("cloud_verification_token_GRID::  Person mail:: {}", terminal.person.email);
                logger.debug("cloud_verification_token_GRID::  Instance ID:: {} ", help.instance_id);
                logger.debug("cloud_verification_token_GRID::  App ID:: {}", help.grid_app_id);


                if (terminal.person == null) {
                   System.out.println("Person is null");
                   logger.debug("cloud_verification_token:: Grid_Terminal object has not own Person - its probably public - Trying to find Instance");

                   if ( Model_Instance.find.query().where().eq("id", help.instance_id).eq("actual_instance.version.public_version", true).findCount() > 0) {
                       System.out.println("Permission found");
                       homer.send(help.get_result(true));
                   } else {
                       System.out.println("Permission not found");
                       homer.send(help.get_result(false));
                   }

                } else {
                    System.out.println("Person is not null!");
                    logger.debug("cloud_verification_token:: Grid_Terminal object has  own Person - its probably private or it can be public - Trying to find Instance with user ID and public value");
                    if ( Model_Instance.find.query().where().eq("id", help.instance_id)
                            .or(Expr.eq("b_program.project.participants.person.id", terminal.person.id), Expr.eq("actual_instance.version.public_version", true))
                            .findCount() > 0) {
                        System.out.println("Permission found");
                        homer.send(help.get_result(true));
                    } else {
                        System.out.println("Permission not found");
                        homer.send(help.get_result(false));
                    }
                }
            }

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void cloud_verification_token_WEBVIEW(WS_Homer homer, WS_Message_WebView_token_verification help) {
        try {

            logger.debug("cloud_verification_token:: WebView  Checking Token");

            Model_AuthorizationToken floatingPersonToken = Model_AuthorizationToken.find.query().where().eq("token", help.token).findOne();

            if (floatingPersonToken == null) {
                logger.warn("cloud_verification_token:: FloatingPersonToken not found!");
                homer.send(help.get_result(false));
                return;
            }

            logger.debug("Cloud_Homer_server:: cloud_verification_token:: WebView FloatingPersonToken Token found and user have permission");


            // Kontola operávnění ke konkrétní instanci??
            if (Model_Instance.find.query().where().eq("id", help.instance_id).eq("b_program.project.participants.person.id", floatingPersonToken.get_person_id()).findCount() > 0) {
                homer.send(help.get_result(true));
            } else {
                homer.send(help.get_result(false));
            }

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public String get_path() {
        return get_project().getPath() + "/instances/" + this.id;
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Instance_create.name())) return;
        this.project.check_update_permission();
    }
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Instance_read.name())) return;
        get_project().check_read_permission();
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Instance_update.name())) return;
        get_project().check_update_permission();
    }

    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Instance_delete.name())) return;
        get_project().check_delete_permission();
    }

    public enum Permission { Instance_create, Instance_read, Instance_update, Instance_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(Model_Instance.class)
    public static Cache<UUID, Model_Instance> cache;

    @CacheField(value = Boolean.class, name = "Model_Instance_Status")
    public static Cache<UUID, Boolean> cache_status;


    public static Model_Instance getById(UUID id) throws _Base_Result_Exception {

        Model_Instance instance = cache.get(id);
        if (instance == null) {

            instance = Model_Instance.find.query().where().idEq(id).eq("deleted", false).findOne();
            if (instance == null) throw new Result_Error_NotFound(Model_Instance.class);

            cache.put(id, instance);
        }
        // Check Permission
        if(instance.its_person_operation()) {
            instance.check_read_permission();
        }

        return instance;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Instance> find = new Finder<>(Model_Instance.class);
}