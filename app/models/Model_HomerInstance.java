package models;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.data.Form;
import play.i18n.Lang;
import play.libs.Json;
import utilities.Server;
import utilities.cache.helps_objects.TyrionCachedList;
import utilities.enums.*;
import utilities.errors.ErrorCode;
import utilities.logger.Class_Logger;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.swagger.outboundClass.Swagger_Instance_Short_Detail;
import web_socket.message_objects.homer_hardware_with_tyrion.*;
import web_socket.message_objects.homer_instance_with_tyrion.verification.WS_Message_Grid_token_verification;
import web_socket.message_objects.homer_instance_with_tyrion.verification.WS_Message_WebView_token_verification;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Online_Change_status;
import web_socket.services.WS_HomerServer;
import web_socket.message_objects.homer_instance_with_tyrion.*;

import javax.persistence.*;
import java.util.*;


@Entity
@ApiModel(description = "Model of HomerInstance", value = "HomerInstance")
@Table(name="HomerInstance")
public class Model_HomerInstance extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    public static final Class_Logger terminal_logger = new Class_Logger(Model_HomerInstance.class);
    
/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                  @Id               public UUID id;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)  public Model_HomerServer cloud_homer_server_default;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)  public Model_HomerServer cloud_homer_server_backup;
                                                                    public String name;
    @Column(columnDefinition = "TEXT") public String description;

    public String project_id; // Shortcuts Reference

    // todo odstranit
    // @JsonIgnore @OneToOne(mappedBy="instance",cascade=CascadeType.ALL, fetch = FetchType.LAZY) public Model_BProgram b_program;                   //LAZY!! - přes Getter!! // BLocko program ke kterému se Homer Instance váže

    @OneToOne(mappedBy="actual_running_instance", cascade=CascadeType.ALL)  public String actual_instance_id;  // Aktuálně běžící instnace na Serveru (Pokud není null má běžet- má běžet na serveru)
    @OneToMany(mappedBy="main_instance_history", cascade=CascadeType.ALL)
    @OrderBy("planed_when DESC") public List<Model_HomerInstanceRecord> instance_history = new ArrayList<>(); // Setříděné pořadí různě nasazovaných verzí Blocko programu

    // TODO odstranit public Enum_Homer_instance_type instance_type;
    @JsonIgnore public boolean removed_by_user; // Defaultně false - když true - tak se to nemá uživateli vracet!

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @TyrionCachedList private String cache_bprogram_id;
    @JsonIgnore @Transient @TyrionCachedList private String cache_actual_instance_id;
    @JsonIgnore @Transient @TyrionCachedList private String cache_server_name;
    @JsonIgnore @Transient @TyrionCachedList private String cache_server_main_id;
    @JsonIgnore @Transient @TyrionCachedList private String cache_server_backup_id;
    @JsonIgnore @Transient @TyrionCachedList private String cache_project_id;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public String b_program_id()             {

        if (cache_bprogram_id == null) {
            Model_BProgram b_program = Model_BProgram.find.where().eq("instance.id", id).select("id").findUnique();
            cache_bprogram_id = b_program.id;
        }

        return cache_bprogram_id;
    }

    @JsonProperty @ApiModelProperty(required = true)
    public String b_program_name()           {
        try {

            return Model_BProgram.get_byId(b_program_id()).name;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true)
    public String b_program_description() {
        try {

            return Model_BProgram.get_byId(b_program_id()).description;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true)
    public String server_name()              {

        try {

        if (cache_server_name != null) {
                return cache_server_name;
            }

            cache_server_name = Model_HomerServer.get_byId(server_id()).personal_server_name;
            return cache_server_name;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true)
    public String server_id()                {
        try {

            if (cache_server_id == null) {
                Model_HomerServer homer_server = Model_HomerServer.find.where().eq("cloud_instances.id", id).select("id").findUnique();

                if (homer_server == null) {
                    terminal_logger.warn("server_id - instance has not set default server");

                    this.cloud_homer_server = Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.main_server.name()).findUnique();
                    if (this.cloud_homer_server == null) {
                        throw new Exception("Main server is not set! Cannot set Default Server to instance!");
                    }
                    this.update();

                    // For Cache * next lines of codes
                    homer_server = this.cloud_homer_server;
                }

                cache_server_id = homer_server.id.toString();
            }
            return cache_server_id;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true)
    public Enum_Online_status online_state() {

        // Pokud Tyrion nezná server ID - to znamená deska se ještě nikdy nepřihlásila - chrání to proti stavu "během výroby"
        // i stavy při vývoji kdy se tvoří zběsile nové desky na dev serverech
        if (get_actual_instance() == null) {

            if (instance_history.isEmpty()) {
                return Enum_Online_status.not_yet_first_connected;
            } else {
                return Enum_Online_status.shut_down;
            }
        }

        // Pokud je server offline - tyrion si nemuže být jistý stavem hardwaru - ten teoreticky muže být online
        // nebo také né - proto se vrací stav Enum_Online_status - na to reaguje parameter latest_online(),
        // který následně vrací latest know online
        try {

            if ( (Model_HomerServer.get_byId(server_id()) != null) && Model_HomerServer.get_byId(server_id()).online_state() == Enum_Online_status.online) {

                if (cache_status.containsKey(id)) {
                    return cache_status.get(id) ? Enum_Online_status.online : Enum_Online_status.offline;
                }

                //else {
                    // Začnu zjišťovat stav - v separátním vlákně! Po strátě spojení se serverem nebo po načítání se někde opomělo změnit stav na online proto
                    // je podmínka zakomentovaná aby se to ověřovalo vždy
                    // TODO ASAP ošetřit!!!!
                    new Thread(() -> {
                        try {

                            WS_Message_Instance_status status = get_instance_status();

                            if (status.status.equals("success")) cache_status.put(id, status.get_status(id).status);
                            WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_HomerInstance.class, this.id, status.get_status(id).status, project_id);

                        } catch (Exception e) {
                            terminal_logger.internalServerError(e);
                        }
                    }).start();

                    return Enum_Online_status.synchronization_in_progress;
                //}

            } else {
                return Enum_Online_status.unknown_lost_connection_with_server;
            }
        } catch (Exception e) {
            
            // Záměrný Exception - Občas se nedosynchronizuje Cach - ale system stejnak po zvalidování dorovná stav
           return Enum_Online_status.unknown_lost_connection_with_server;
        }
    }

    @JsonProperty @ApiModelProperty(required = true)
    public Enum_Online_status server_online_state() {
        return Model_HomerServer.get_byId(server_id()).online_state();
    }

    @JsonProperty @ApiModelProperty(required = true)
    public String instance_remote_url() {
        try {

            if (get_actual_instance() != null) {

                if (Server.server_mode  == Enum_Tyrion_Server_mode.developer) {
                    return "ws://" + Model_HomerServer.get_byId(server_id()).server_url + ":" + Model_HomerServer.get_byId(server_id()).web_view_port + "/" + id + "/#token";
                } else {
                    return "wss://" + Model_HomerServer.get_byId(server_id()).server_url + ":" + Model_HomerServer.get_byId(server_id()).web_view_port + "/" + id + "/#token";
                }
            }

            return null;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

/* GET Variable short type of objects ----------------------------------------------------------------------------------*/

    @JsonIgnore
    public Swagger_Instance_Short_Detail get_instance_short_detail() {
        try {

            Swagger_Instance_Short_Detail help = new Swagger_Instance_Short_Detail();
            help.id = id;
            help.name = name;
            help.description = description;

            if (b_program_id() != null) {
                help.b_program_id = b_program_id();
                help.b_program_name = b_program_name();
                help.b_program_description = b_program_description();
            }

            if (get_actual_instance()!= null) {
                help.b_program_version_id = get_actual_instance().b_program_version_id();
                help.b_program_version_name = get_actual_instance().b_program_version_name();
            }

            help.server_name = server_name();
            help.server_id = server_id();
            help.instance_online_state = online_state();
            help.server_online_state = server_online_state();
            help.update_permission = Model_BProgram.get_byId(b_program_id()).update_permission();
            help.edit_permission = Model_BProgram.get_byId(b_program_id()).edit_permission();

            return help;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_Project get_project() {

        return Model_Project.get_byId(get_project_id());
    }

    @JsonIgnore
    public Model_HomerInstanceRecord get_actual_instance() {

        if (cache_actual_instance_id == null) {
            Model_HomerInstanceRecord record_not_cached = Model_HomerInstanceRecord.find.where().eq("actual_running_instance.id", this.id).select("id").findUnique();
            if (record_not_cached != null) {
                this.cache_actual_instance_id = record_not_cached.id;
                return get_actual_instance();
            } else {
                return null;
            }
        } else {
            return Model_HomerInstanceRecord.get_byId(cache_actual_instance_id);
        }
    }

    @JsonIgnore
    public String get_project_id() {

        if (cache_project_id == null) {
            Model_Project project = Model_Project.find.where().eq("b_programs.id", b_program_id()).select("id").findUnique();
            cache_project_id = project.id;
        }

        return cache_project_id;
    }

    @JsonIgnore
    public List<String> get_boards_id_required_by_record() {
        return get_actual_instance().get_boards_required_by_record();
    }

    @JsonIgnore
    public Model_BProgram getB_program() {
        return Model_BProgram.get_byId(b_program_id());
    }

/* JSON Override  Method -----------------------------------------------------------------------------------------*/

    @Override
    public void save() {
        
        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_HomerInstance.find.where().eq("id", id).findUnique() == null) break;
        }

        terminal_logger.debug("save - saving to database, id: {}", this.id);

        super.save();

        if (project_id != null) {
            Model_Project.get_byId(project_id).cache_list_instance_ids.add(id);
        }

        cache.put(this.id, this);
    }

    @Override
    public void update() {

        terminal_logger.debug("update - updating in database, id: {}",  this.id);

        super.update();

        if (cache.containsKey(this.id)) {
            cache.replace(this.id, this);
        } else {
            cache.put(this.id, this);
        }
    }
    
    @Override
    public void delete() {

        terminal_logger.debug("delete - deleting from database, id: {} ", this.id);
        
        this.removed_by_user = true;
        super.update();

        if (project_id != null) {
            Model_Project.get_byId(project_id).cache_list_instance_ids.remove(id);
        }

        if (cache.containsKey(this.id)) {
            cache.remove(this.id);
        }
    }

    public void cache_refresh() {
        this.refresh();
        this.cache_actual_instance_id = null;
        if (cache.containsKey(this.id)) {
            cache.replace(this.id, this);
        } else {
            cache.put(this.id, this);
        }
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void notification_instance_start_upload() {
        try {

        new Model_Notification()
                .setImportance(Enum_Notification_importance.low)
                .setLevel(Enum_Notification_level.info)
                .setText( new Notification_Text().setText("Server started creating new Blocko Instance of Blocko Version "))
                .setText( new Notification_Text().setText(this.get_actual_instance().get_b_program_version().get_b_program().name).setBoldText())
                .setObject(this.get_actual_instance().get_b_program_version())
                .setText( new Notification_Text().setText(" from Blocko program "))
                .setObject(this.get_actual_instance().get_b_program_version().get_b_program())
                .send(Controller_Security.get_person());

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void notification_instance_successful_upload() {
        try {

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.low)
                    .setLevel(Enum_Notification_level.success)
                    .setText(new Notification_Text().setText("Server successfully created the instance of Blocko Version "))
                    .setObject(this.get_actual_instance().get_b_program_version())
                    .setText(new Notification_Text().setText(" from Blocko program "))
                    .setObject(this.get_actual_instance().get_b_program_version().get_b_program())
                    .send_under_project(get_project_id());

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void notification_instance_unsuccessful_upload(String reason) {
        try {

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.low)
                    .setLevel(Enum_Notification_level.warning)
                    .setText( new Notification_Text().setText("Server did not upload instance to cloud on Blocko Version "))
                    .setText( new Notification_Text().setText(this.get_actual_instance().get_b_program_version().version_name ).setBoldText())
                    .setText( new Notification_Text().setText(" from Blocko program "))
                    .setText( new Notification_Text().setText(this.b_program.name).setBoldText())
                    .setText( new Notification_Text().setText(" for reason: ").setBoldText() )
                    .setText( new Notification_Text().setText(reason + " ").setBoldText())
                    .setObject(this.get_actual_instance().get_b_program_version())
                    .setText( new Notification_Text().setText(" from Blocko program "))
                    .setObject(this.getB_program())
                    .setText( new Notification_Text().setText(". Server will try to do that as soon as possible."))
                    .send_under_project(get_project_id());

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void notification_new_actualization_request_instance() {
        try {

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.low)
                    .setLevel(Enum_Notification_level.info)
                    .setText( new Notification_Text().setText("New actualization task was added to Task Queue on Version "))
                    .setObject(this.get_actual_instance().get_b_program_version())
                    .send_under_project(get_project_id());

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

/* INSTANCE WEBSOCKET CONTROLLING ON HOMER SERVER-----------------------------------------------------------------------*/

    public static final String CHANNEL = "instance";

    //-- Messenger - Parsarer of Incoming Messages -- //
    @JsonIgnore
    public static void Messages(WS_HomerServer homer, ObjectNode json) {
        new Thread(() -> {
            try {

                switch (json.get("message_type").asText()) {

                    case WS_Message_Grid_token_verification.message_type: {

                        final Form<WS_Message_Grid_token_verification> form = Form.form(WS_Message_Grid_token_verification.class).bind(json);
                        if (form.hasErrors()) throw new Exception("WS_Message_Grid_token_verification: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                        WS_Message_Grid_token_verification help = form.get();
                        help.get_instance().cloud_verification_token_GRID(homer, help );
                        return;
                    }

                    case WS_Message_WebView_token_verification.messageType: {

                        final Form<WS_Message_WebView_token_verification> form = Form.form(WS_Message_WebView_token_verification.class).bind(json);
                        if (form.hasErrors()) throw new Exception("token_webView_verification: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                        WS_Message_WebView_token_verification help = form.get();
                        help.get_instance().cloud_verification_token_WEBVIEW(homer, help);
                        return;
                    }

                    // Ochrana proti zacyklení
                    case WS_Message_Instance_device_set_snap.message_type           : {terminal_logger.warn("WS_Message_Instance_device_set_snap: A message with a very high delay has arrived.");return;}
                    case WS_Message_Instance_status.message_type                    : {terminal_logger.warn("WS_Message_Instance_status: A message with a very high delay has arrived.");return;}
                    case WS_Message_Instance_terminal_set_snap.message_type         : {terminal_logger.warn("WS_Message_Instance_terminal_set_snap: A message with a very high delay has arrived.");return;}
                    case WS_Message_Instance_upload_blocko_program.message_type     : {terminal_logger.warn("WS_Message_Instance_upload_blocko_program: A message with a very high delay has arrived.");return;}

                    default: {

                        terminal_logger.warn("Incoming Message not recognized::" + json.toString());

                        // Zarážka proti nevadliní odpovědi a zacyklení
                        if (json.has("status") && json.get("status").asText().equals("error")) {
                            return;
                        }

                        homer.write_without_confirmation(json.put("error_message", "message_type not recognized").put("error_code", 400));
                    }
                }

            } catch (Exception e) {

                if (!json.has("message_type")) {
                    homer.write_without_confirmation(json.put("error_message", "Your message not contains message_type").put("error_code", 400));
                    return;
                } else {
                    terminal_logger.internalServerError(e);
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
            request.put("message_channel", Model_HomerInstance.CHANNEL);
            request.put("error_code", ErrorCode.HOMER_SERVER_NOT_SET_FOR_INSTANCE.error_code());
            request.put("error_message", ErrorCode.HOMER_SERVER_NOT_SET_FOR_INSTANCE.error_message());
            request.put("message_id", json.has("message_id") ? json.get("message_id").asText() : "unknown");
            request.put("websocket_identificator", "unknown");

            return request;
        }

        Model_HomerServer server = Model_HomerServer.get_byId(this.server_id());
        if (server == null) {

            terminal_logger.internalServerError(new Exception("write_with_confirmation:: Instance " + id + ". Server id " + this.server_id() + " not exist!"));

            ObjectNode request = Json.newObject();
            request.put("message_type", json.get("message_type").asText());
            request.put("message_channel", Model_HomerInstance.CHANNEL);
            request.put("error_code", ErrorCode.HOMER_NOT_EXIST.error_code());
            request.put("error_message", ErrorCode.HOMER_NOT_EXIST.error_message());
            request.put("message_id", json.has("message_id") ? json.get("message_id").asText() : "unknown");
            request.put("websocket_identificator", "unknown");

            return request;
        }

        json.put("instance_id", id);
        return server.write_with_confirmation(json, time, delay, number_of_retries);
    }

    // Metoda překontroluje odeslání a pak předává objektu - zpráva plave skrze program
    @JsonIgnore
    private void write_without_confirmation(ObjectNode json) {

        Model_HomerServer server = Model_HomerServer.get_byId(this.server_id());
        if (server == null) {

            terminal_logger.internalServerError(new Exception("write_without_confirmation:: Instance " + id + " server not found"));

            return;
        }

        json.put("instance_id", id);
        server.write_without_confirmation(json);

    }

    // Metoda překontroluje odeslání a pak předává objektu - zpráva plave skrze program
    @JsonIgnore
    public void write_without_confirmation(String message_id, ObjectNode json) {

        if (this.server_id() == null) {
            return;
        }

        Model_HomerServer server = Model_HomerServer.get_byId(this.server_id());

        if (server == null) {
            terminal_logger.internalServerError(new Exception("write_without_confirmation:: Instance " + id + " server not found"));
            return;
        }

        json.put("instance_id", id);
        server.write_without_confirmation(message_id, json);
    }

    //-- Instance Status -- //
    @JsonIgnore
    public WS_Message_Instance_status get_instance_status() {
        try {

            JsonNode node = write_with_confirmation(new WS_Message_Instance_status().make_request(Collections.singletonList(id)), 1000 * 3, 0, 2);

            final Form<WS_Message_Instance_status> form = Form.form(WS_Message_Instance_status.class).bind(node);
            if (form.hasErrors()) throw new Exception("WS_Message_Instance_status: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

           return form.get();

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return new WS_Message_Instance_status();
        }
    }

    //-- Device IO operations -- //
    @JsonIgnore
    public WS_Message_Instance_device_set_snap set_device_to_instance(List<String> device_ids) {
        try {

            JsonNode node = this.write_with_confirmation(new WS_Message_Instance_device_set_snap().make_request(device_ids), 1000*3, 0, 4);

            final Form<WS_Message_Instance_device_set_snap> form = Form.form(WS_Message_Instance_device_set_snap.class).bind(node);
            if (form.hasErrors()) throw new Exception("WS_Message_Hardware_add: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

            return form.get();

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return new WS_Message_Instance_device_set_snap();
        }
    }

    //-- Terminal IO operations -- //
    @JsonIgnore
    public WS_Message_Instance_terminal_set_snap setTerminals(List<String> terminalIds) {
        try {

            JsonNode node = this.write_with_confirmation(new WS_Message_Instance_terminal_set_snap().make_request(terminalIds), 1000*3, 0, 4);

            final Form<WS_Message_Instance_terminal_set_snap> form = Form.form(WS_Message_Instance_terminal_set_snap.class).bind(node);
            if (form.hasErrors()) throw new Exception("WS_Message_Instance_terminal_set_snap: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

            return form.get();

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return new WS_Message_Instance_terminal_set_snap();
        }
    }

    //-- Instance Summary Information --//
    @JsonIgnore
    public WS_Message_Hardware_overview get_hardware_overview() {
        try {

            ObjectNode node = this.write_with_confirmation( new WS_Message_Hardware_overview().make_request(this.get_boards_id_required_by_record()), 1000*5, 0, 1);

            final Form<WS_Message_Hardware_overview> form = Form.form(WS_Message_Hardware_overview.class).bind(node);
            if (form.hasErrors()) throw new Exception("WS_Help_Hardware_overview: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

            return form.get();

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return new WS_Message_Hardware_overview();
        }
    }

    //-- Helper Commands --//
    @JsonIgnore
    public void upload_to_cloud() {
        get_actual_instance().put_record_into_cloud();
    }

    @JsonIgnore
    public void remove_from_cloud() {

        cache_status.put(this.id, false);
        WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_HomerInstance.class, this.id, true, project_id);

        if (get_actual_instance() != null) {

            get_actual_instance().actual_running_instance = null;
            get_actual_instance().update();

            get_actual_instance().refresh();

            actual_instance = null;
            this.update();
        }

        actual_instance = null;
        this.update();

        Model_HomerServer server = Model_HomerServer.get_byId(server_id());
        if (server == null) {
            return;
        }

        server.remove_instance(Collections.singletonList(id));
    }

    //-- Verification --//
    @JsonIgnore
    public void cloud_verification_token_GRID(WS_HomerServer homer, WS_Message_Grid_token_verification help) {
        try {

            terminal_logger.debug("cloud_verification_token_GRID::  Checking Token");
            terminal_logger.debug("cloud_verification_token_GRID::  Token:: {} ", help.token);
            terminal_logger.debug("cloud_verification_token_GRID::  Instance ID:: {} ", help.instance_id);
            terminal_logger.debug("cloud_verification_token_GRID::  App ID:: {}", help.snapshot_id);

            Model_GridTerminal terminal = Model_GridTerminal.find.where().eq("terminal_token", help.token).findUnique();

            // Pokud je terminall null - nikdy se uživatel nepřihlásit a nevytvořil se o tom záznam - ale to stále neznamená že není možno povolit přístup
            if (terminal == null) {

                System.out.println("terminal == null");
                Model_MProgramInstanceParameter parameter = Model_MProgramInstanceParameter.find.where()
                        .eq("connection_token", help.token)
                        .isNotNull("m_project_program_snapshot.instance_versions.instance_record.actual_running_instance")
                        .findUnique();

                if(parameter == null) {
                    terminal_logger.error("cloud_verification_token_GRID:: Model_MProgramInstanceParameter parameter is null");
                    homer.write_without_confirmation(help.get_result(false));
                    return;
                }
                Enum_MProgram_SnapShot_settings settings = parameter.snapshot_settings();
                terminal_logger.debug("Enum_MProgram_SnapShot_settings {}", settings.name());

                switch (settings) {

                    case absolutely_public: {
                        System.out.println("Je to plnohodnotně public");
                        homer.write_without_confirmation(help.get_result(true));
                        return;
                    }

                    case only_for_project_members: {
                        System.out.println("Pouze pro registrované v projektu ale jekilož neexistuje přihlášení nelze připojit???");
                        homer.write_without_confirmation(help.get_result(false));
                        return;
                    }

                    case not_in_instance:{
                        System.out.println("Grid se snaží připojit na něco co není instancí!");
                        homer.write_without_confirmation(help.get_result(false));
                        return;
                    }

                    default: {
                        System.out.println("parameter.snapshot_settings() default parameter!! return FALSE");
                        homer.write_without_confirmation(help.get_result(false));
                        return;
                    }
                }
            }else {

                System.out.println("terminal != null");
                terminal_logger.debug("cloud_verification_token_GRID::  Person id:: {}", terminal.person.id);
                terminal_logger.debug("cloud_verification_token_GRID::  Person mail:: {}", terminal.person.mail);
                terminal_logger.debug("cloud_verification_token_GRID::  Instance ID:: {} ", help.instance_id);
                terminal_logger.debug("cloud_verification_token_GRID::  App ID:: {}", help.snapshot_id);


                if (terminal.person == null) {
                   System.out.println("Person is null");
                   terminal_logger.debug("cloud_verification_token:: Grid_Terminal object has not own Person - its probably public - Trying to find Instance");

                   if( Model_HomerInstance.find.where().eq("id", help.instance_id).eq("actual_instance.version_object.public_version", true).findRowCount() > 0) {
                       System.out.println("Permission found");
                       homer.write_without_confirmation(help.get_result(true));
                       return;
                   }else {
                       System.out.println("Permission not found");
                       homer.write_without_confirmation(help.get_result(false));
                       return;
                   }

                } else {
                    System.out.println("Person is not null!");
                    terminal_logger.debug("cloud_verification_token:: Grid_Terminal object has  own Person - its probably private or it can be public - Trying to find Instance with user ID and public value");
                    if( Model_HomerInstance.find.where().eq("id", help.instance_id)
                            .or(Expr.eq("b_program.project.participants.person.id", terminal.person.id), Expr.eq("actual_instance.version_object.public_version", true))
                            .findRowCount() > 0) {
                        System.out.println("Permission found");
                        homer.write_without_confirmation(help.get_result(true));
                        return;
                    }else {
                        System.out.println("Permission not found");
                        homer.write_without_confirmation(help.get_result(false));
                        return;
                    }
                }
            }

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void cloud_verification_token_WEBVIEW(WS_HomerServer homer, WS_Message_WebView_token_verification help) {
        try {

            terminal_logger.debug("cloud_verification_token:: WebView  Checking Token");

            Model_FloatingPersonToken floatingPersonToken = Model_FloatingPersonToken.find.where().eq("authToken", help.token).findUnique();

            if (floatingPersonToken == null) {
                terminal_logger.warn("cloud_verification_token:: FloatingPersonToken not found!");
                homer.write_without_confirmation(help.get_result(false));
                return;
            }

            terminal_logger.debug("Cloud_Homer_server:: cloud_verification_token:: WebView FloatingPersonToken Token found and user have permission");


            // Kontola operávnění ke konkrétní instanci??
            if (Model_HomerInstance.find.where().eq("id", help.instance_id).eq("b_program.project.participants.person.id", floatingPersonToken.person.id).findRowCount() > 0) {
                homer.write_without_confirmation(help.get_result(true));
            } else {
                homer.write_without_confirmation(help.get_result(false));
            }

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   public boolean create_permission()  {  return  getB_program().read_permission()   || Controller_Security.get_person().has_permission("B_Program_create");  }
    @JsonProperty public boolean update_permission()  {  return  getB_program().update_permission() || Controller_Security.get_person().has_permission("Instance_update");  }
    @JsonIgnore   public boolean read_permission()    {  return  getB_program().read_permission()   || Controller_Security.get_person().has_permission("Instance_read");   }
    @JsonProperty public boolean edit_permission()    {  return  getB_program().edit_permission()   || Controller_Security.get_person().has_permission("Instance_edit");    }
    //@JsonProperty @Transient public boolean delete_permission()  {  return  getB_program().delete_permission() || Controller_Security.get_person().has_permission("Instance_delete");  }

    public enum permissions{ Instance_create, Instance_update, Instance_read, Instance_edit , Instance_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String, Model_HomerInstance> find = new Finder<>(Model_HomerInstance.class);

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE        = Model_HomerInstance.class.getSimpleName();
    public static final String CACHE_STATUS = Model_HomerInstance.class.getSimpleName() + "_STATUS";

    public static Cache<String, Model_HomerInstance> cache = null; // Server_cache Override during server initialization
    public static Cache<String, Boolean> cache_status = null; // Server_cache Override during server initialization

    public static Model_HomerInstance get_byId(String id) {

        Model_HomerInstance instance = cache.get(id);
        if (instance == null) {

            instance = Model_HomerInstance.find.where().idEq(id).eq("removed_by_user", false).findUnique();
            if (instance == null) return null;
            
            cache.put(id, instance);
        }

        return instance;
    }
}