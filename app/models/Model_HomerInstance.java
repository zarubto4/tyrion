package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_Security;
import controllers.Controller_WebSocket;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.data.Form;
import play.i18n.Lang;
import utilities.Server;
import utilities.cache.helps_objects.TyrionCachedList;
import utilities.enums.*;
import utilities.logger.Class_Logger;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.swagger.outboundClass.Swagger_Instance_Short_Detail;
import web_socket.message_objects.homer_hardware_with_tyrion.*;
import web_socket.message_objects.homer_instance_with_tyrion.verification.WS_Message_Grid_token_verification;
import web_socket.message_objects.homer_instance_with_tyrion.verification.WS_Message_WebView_token_verification;
import web_socket.message_objects.homer_with_tyrion.WS_Message_Homer_Instance_destroy;
import web_socket.services.WS_HomerServer;
import web_socket.message_objects.homer_instance_with_tyrion.*;

import javax.persistence.*;
import java.nio.channels.ClosedChannelException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


@Entity
@ApiModel(description = "Model of HomerInstance",
        value = "HomerInstance")
public class Model_HomerInstance extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    public static final Class_Logger terminal_logger = new Class_Logger(Model_HomerInstance.class);
    
/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                  @Id               public String id;
                    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)  public Model_HomerServer cloud_homer_server;
                                                                    public String name;
                                 @Column(columnDefinition = "TEXT") public String description;

                                                       @JsonIgnore  public String project_id; // Předpřipravené pro další implementaci

    @JsonIgnore @OneToOne(mappedBy="instance",cascade=CascadeType.ALL, fetch = FetchType.LAZY) public Model_BProgram b_program;                   //LAZY!! - přes Getter!! // BLocko program ke kterému se Homer Instance váže

                @OneToOne(mappedBy="actual_running_instance", cascade=CascadeType.ALL)         public Model_HomerInstanceRecord actual_instance;  // Aktuálně běžící instnace na Serveru (Pokud není null má běžet- má běžet na serveru)

                @OneToMany(mappedBy="main_instance_history", cascade=CascadeType.ALL) @OrderBy("planed_when DESC") public List<Model_HomerInstanceRecord> instance_history = new ArrayList<>(); // Setříděné pořadí různě nasazovaných verzí Blocko programu


                 public Enum_Homer_instance_type instance_type;

     @JsonIgnore public boolean removed_by_user; // Defaultně false - když true - tak se to nemá uživateli vracet!


/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @TyrionCachedList private String cache_bprogram_id;
    @JsonIgnore @Transient @TyrionCachedList private String cache_bprogram_name;
    @JsonIgnore @Transient @TyrionCachedList private String cache_bprogram_description;
    @JsonIgnore @Transient @TyrionCachedList private String cache_actual_instance_id;
    @JsonIgnore @Transient @TyrionCachedList private String cache_server_name;
    @JsonIgnore @Transient @TyrionCachedList private String cache_server_id;
    @JsonIgnore @Transient @TyrionCachedList private String cache_project_id;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/


    @Transient @JsonProperty @ApiModelProperty(required = true) public  String b_program_id()             {

        if(cache_bprogram_id == null){
            Model_BProgram b_program = Model_BProgram.find.where().eq("instance.id", id).select("id").findUnique();
            cache_bprogram_id = b_program.id;
        }

        return cache_bprogram_id;

    }
    @Transient @JsonProperty @ApiModelProperty(required = true) public  String b_program_name()           {
        try{

            if(cache_bprogram_name != null) {
                return cache_bprogram_name;
            }

            cache_bprogram_name = Model_BProgram.get_byId(b_program_id()).name;
            return cache_bprogram_name;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }
    @Transient @JsonProperty @ApiModelProperty(required = true) public  String b_program_description() {
        try {

            if (cache_bprogram_description != null) {
                return cache_bprogram_description;
            }

            cache_bprogram_description = Model_BProgram.get_byId(b_program_id()).description;
            return cache_bprogram_description;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }
    @Transient @JsonProperty @ApiModelProperty(required = true) public  String server_name()              {

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
    @Transient @JsonProperty @ApiModelProperty(required = true) public  String server_id()                {
        try {

            if (cache_server_id == null) {
                Model_HomerServer homer_server = Model_HomerServer.find.where().eq("cloud_instances.id", id).select("unique_identificator").findUnique();
                cache_server_id = homer_server.unique_identificator;
            }

            return cache_server_id;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }
    @Transient @JsonProperty @ApiModelProperty(required = true) public  Enum_Online_status instance_status() {

        // Pokud Tyrion nezná server ID - to znamená deska se ještě nikdy nepřihlásila - chrání to proti stavu "během výroby"
        // i stavy při vývoji kdy se tvoří zběsile nové desky na dev serverech
        if(actual_instance == null){
            return Enum_Online_status.not_yet_first_connected;
        }

        // Pokud je server offline - tyrion si nemuže být jistý stavem hardwaru - ten teoreticky muže být online
        // nebo také né - proto se vrací stav Enum_Online_status - na to reaguje parameter latest_online(),
        // který následně vrací latest know online
        if(Model_HomerServer.get_byId(server_id()).server_is_online()){

            if(cache_status.containsKey(id)){
                return cache_status.get(id) ? Enum_Online_status.online : Enum_Online_status.offline;
            }else {
                // Začnu zjišťovat stav - v separátním vlákně!
                new Thread( () -> {
                    try {

                        WS_Message_Instance_status status = get_instance_status();
                        cache_status.put(id, status.get_status(id).online_status);

                    } catch (Exception e) {
                        terminal_logger.internalServerError("notification_board_connect:", e);
                    }
                }).start();

                return Enum_Online_status.synchronization_in_progress;

            }
        } else {
            return Enum_Online_status.unknown_lost_connection_with_server;
        }
        // return this.online_state();
    }
    @Transient @JsonProperty @ApiModelProperty(required = true) public boolean server_is_online()         {  return Model_HomerServer.get_byId(server_id()).server_is_online();}

    @Transient @JsonProperty @ApiModelProperty(required = true) public String instance_remote_url(){
        try {

            if(actual_instance != null) {

                if(Server.server_mode  == Enum_Tyrion_Server_mode.developer) {
                    return "ws://" + Model_HomerServer.get_byId(server_id()).server_url + ":" + Model_HomerServer.get_byId(server_id()).web_view_port + "/" + id + "/#token";
                }else{
                    return "wss://" + Model_HomerServer.get_byId(server_id()).server_url + ":" + Model_HomerServer.get_byId(server_id()).web_view_port + "/" + id + "/#token";
                }

            }

            return null;

        }catch (Exception e){
            terminal_logger.internalServerError("instance_remote_url", e);
            return null;
        }
    }

/* GET Variable short type of objects ----------------------------------------------------------------------------------*/

    @Transient @JsonIgnore public Swagger_Instance_Short_Detail get_instance_short_detail(){
        try {

            Swagger_Instance_Short_Detail help = new Swagger_Instance_Short_Detail();
            help.id = id;
            help.name = name;
            help.description = description;
            help.b_program_id = b_program_id();
            help.b_program_name = b_program_name();
            help.b_program_description = b_program_description();
            help.b_program_version_id = actual_instance.b_program_version_id();
            help.b_program_version_name = actual_instance.b_program_version_name();

            help.server_name = server_id();
            help.server_id = server_id();
            help.instance_status = instance_status();
            help.server_is_online = server_is_online();
            help.update_permission = Model_BProgram.get_byId(b_program_id()).update_permission();
            help.edit_permission = Model_BProgram.get_byId(b_program_id()).edit_permission();

            return help;

        }catch (Exception e){
            terminal_logger.internalServerError("get_instance_short_detail", e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


    @JsonIgnore @Transient  public Model_Project get_project() {

        return Model_Project.get_byId(get_project_id());

    }

    @JsonIgnore @Transient  public String get_project_id() {

        if(cache_project_id == null) {
            Model_Project project = Model_Project.find.where().eq("b_programs.id", b_program.id).select("id").findUnique();
            cache_project_id = project.id;
        }

        return cache_project_id;

    }


    @JsonIgnore @Transient  public List<String> get_boards_id_required_by_record() {
        return actual_instance.get_boards_required_by_record();
    }


    @JsonIgnore @Transient  public Model_BProgram getB_program(){
        return Model_BProgram.get_byId(b_program_id());
    }

/* JSON Override  Method -----------------------------------------------------------------------------------------*/

    @Override
    public void save(){

        terminal_logger.debug("save :: Creating new Object");
        
        while(true){ // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_HomerInstance.find.where().eq("id", id).findUnique() == null) break;
        }

        super.save();


        if(project_id != null){
            Model_Project.get_byId(project_id).instance_ids.add(id);
        }

        cache.put(this.id, this);
    }

    @Override
    public void update(){

        terminal_logger.debug("update :: Update object id: {}",  this.id);

        super.update();
        cache.put(this.id, this);
    }
    
    @Override
    public void delete(){

        terminal_logger.debug("update :: Delete object id: {} ", this.id);
        
        this.removed_by_user = true;
        super.update();

        if(project_id != null){
            Model_Project.get_byId(project_id).instance_ids.remove(id);
        }

        cache.put(id, this);

    }


/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/


/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void notification_instance_start_upload(){
        try {

        new Model_Notification()
                .setImportance(Enum_Notification_importance.low)
                .setLevel(Enum_Notification_level.info)
                .setText( new Notification_Text().setText("Server started creating new Blocko Instance of Blocko Version "))
                .setText( new Notification_Text().setText(this.actual_instance.version_object.b_program.name).setBoldText())
                .setObject(this.actual_instance.version_object)
                .setText( new Notification_Text().setText(" from Blocko program "))
                .setObject(this.actual_instance.version_object.b_program)
                .send(Controller_Security.get_person());

        }catch (Exception e){
            terminal_logger.internalServerError("notification_instance_start_upload:", e);
        }
    }

    @JsonIgnore @Transient
    public void notification_instance_successful_upload(){
        try {

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.low)
                    .setLevel(Enum_Notification_level.success)
                    .setText(new Notification_Text().setText("Server successfully created the instance of Blocko Version "))
                    .setObject(this.actual_instance.version_object)
                    .setText(new Notification_Text().setText(" from Blocko program "))
                    .setObject(this.actual_instance.version_object.b_program)
                    .send_under_project(get_project_id());

        }catch (Exception e){
            terminal_logger.internalServerError("notification_instance_successful_upload:", e);
        }
    }

    @JsonIgnore @Transient
    public void notification_instance_unsuccessful_upload(String reason){
        try {

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.low)
                    .setLevel(Enum_Notification_level.warning)
                    .setText( new Notification_Text().setText("Server did not upload instance to cloud on Blocko Version "))
                    .setText( new Notification_Text().setText(this.actual_instance.version_object.version_name ).setBoldText())
                    .setText( new Notification_Text().setText(" from Blocko program "))
                    .setText( new Notification_Text().setText(this.b_program.name).setBoldText())
                    .setText( new Notification_Text().setText(" for reason: ").setBoldText() )
                    .setText( new Notification_Text().setText(reason + " ").setBoldText())
                    .setObject(this.actual_instance.version_object)
                    .setText( new Notification_Text().setText(" from Blocko program "))
                    .setObject(this.b_program)
                    .setText( new Notification_Text().setText(". Server will try to do that as soon as possible."))
                    .send_under_project(get_project_id());

        }catch (Exception e){
            terminal_logger.internalServerError("notification_instance_unsuccessful_upload:", e);
        }
    }

    @JsonIgnore @Transient
    public void notification_new_actualization_request_instance(){
        try {

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.low)
                    .setLevel(Enum_Notification_level.info)
                    .setText( new Notification_Text().setText("New actualization task was added to Task Queue on Version "))
                    .setObject(this.actual_instance.version_object)
                    .send_under_project(get_project_id());

        }catch (Exception e){
            terminal_logger.internalServerError("notification_new_actualization_request_instance:", e);
        }
    }



/* INSTANCE WEBSOCKET CONTROLLING ON HOMER SERVER-----------------------------------------------------------------------*/

    public static final String CHANNEL = "instance";


    //-- Messenger - Parsarer of Incoming Messages -- //
    @JsonIgnore @Transient
    public static void Messages(WS_HomerServer homer, ObjectNode json){
        new Thread(() -> {
            try {
                switch (json.get("message_type").asText()) {

                    case WS_Message_Grid_token_verification.message_type: {

                        final Form<WS_Message_Grid_token_verification> form = Form.form(WS_Message_Grid_token_verification.class).bind(json);
                        if (form.hasErrors()) throw new Exception("WS_Message_Grid_token_verification: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                        WS_Message_Grid_token_verification help = form.get();
                        help.get_instance().cloud_verification_token_GRID(help);
                        return;
                    }

                    case WS_Message_WebView_token_verification.messageType: {

                        final Form<WS_Message_WebView_token_verification> form = Form.form(WS_Message_WebView_token_verification.class).bind(json);
                        if (form.hasErrors()) throw new Exception("token_webView_verification: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                        WS_Message_WebView_token_verification help = form.get();
                        help.get_instance().cloud_verification_token_WEBVIEW(help);
                        return;
                    }

                    default: throw new Exception("Incoming message, chanel tyrion: message_type not recognized -> " + json.get("message_type").asText());
                }

            } catch (Exception e) {
                terminal_logger.internalServerError("Messages:", e);
            }
        }).start();
    }


    //--  Sender -- //
    @JsonIgnore @Transient
    public ObjectNode write_with_confirmation(ObjectNode json, Integer time, Integer delay, Integer number_of_retries) throws TimeoutException, ClosedChannelException, ExecutionException, InterruptedException{
       json.put("instance_id", id);
       return Controller_WebSocket.homer_servers.get(server_id()).write_with_confirmation(json, time, delay, number_of_retries);
    }

    @JsonIgnore @Transient
    public void write_without_confirmation(ObjectNode json) throws TimeoutException, ClosedChannelException, ExecutionException, InterruptedException{
        json.put("instance_id", id);
        Controller_WebSocket.homer_servers.get(server_id()).write_without_confirmation(json);
    }



    //-- Instance Status -- //
    @JsonIgnore @Transient
    public WS_Message_Instance_status get_instance_status(){

        List<Model_HomerInstance> instances = new ArrayList<>();
        instances.add(this);
        return get_instance_status(instances);

    }
    @JsonIgnore @Transient
    public static WS_Message_Instance_status get_instance_status(List<Model_HomerInstance> instances){

        HashMap<String, List<String>> server_map = new HashMap<>();

        for(Model_HomerInstance instance : instances){

            if(!server_map.containsKey(instance.server_id())){
                server_map.put(instance.server_id(), new ArrayList<String>());
            }

            server_map.get(instance.server_id()).add(instance.id);
        }


        WS_Message_Instance_status status = new WS_Message_Instance_status();

        for(String unique_identificator : server_map.keySet()){
            try{

                JsonNode node = Model_HomerServer.get_byId(unique_identificator).sender().write_with_confirmation( new WS_Message_Instance_status().make_request(server_map.get(unique_identificator)), 1000*3, 0, 2);

                final Form<WS_Message_Instance_status> form = Form.form(WS_Message_Instance_status.class).bind(node);
                if(form.hasErrors()) throw new Exception("WS_Message_Instance_status: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                status.instance_list.addAll(form.get().instance_list);

            }catch (InterruptedException|TimeoutException e){
                return new WS_Message_Instance_status();
            }catch (Exception e){
                Model_HomerInstance.terminal_logger.internalServerError(e);
                return new WS_Message_Instance_status();
            }
        }


        return status;

    }



    //-- Device IO operations -- //
    @JsonIgnore @Transient
    public WS_Message_Instance_device_set_snap set_device_to_instance(List<String> device_ids){
        try{

            if(!this.server_is_online()) throw new InterruptedException();
            JsonNode node = this.write_with_confirmation(new WS_Message_Instance_device_set_snap().make_request(device_ids), 1000*3, 0, 4);

            final Form<WS_Message_Instance_device_set_snap> form = Form.form(WS_Message_Instance_device_set_snap.class).bind(node);
            if(form.hasErrors()) throw new Exception("WS_Message_Hardware_add: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

            return form.get();

        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Instance_device_set_snap();
        }catch (Exception e){
            Model_HomerInstance.terminal_logger.internalServerError(e);
            return new WS_Message_Instance_device_set_snap();
        }
    }


    //-- Instance Summary Information --//

    @JsonIgnore @Transient public WS_Message_Hardware_overview get_hardware_overview(){
        try {

            if(!this.server_is_online()) throw new InterruptedException();
            ObjectNode node = this.write_with_confirmation( new WS_Message_Hardware_overview().make_request(this.get_boards_id_required_by_record()), 1000*5, 0, 1);

            final Form<WS_Message_Hardware_overview> form = Form.form(WS_Message_Hardware_overview.class).bind(node);
            if (form.hasErrors()) throw new Exception("WS_Help_Hardware_overview: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

            return form.get();

        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Hardware_overview();
        }catch (Exception e){
            Model_HomerInstance.terminal_logger.internalServerError("get_hardware_list:", e);
            return new WS_Message_Hardware_overview();
        }
    }

    //-- Helper Commands --//
    @JsonIgnore @Transient
    public void upload_to_cloud(){
        actual_instance.set_record_into_cloud();
    }


    @JsonIgnore @Transient
    public WS_Message_Homer_Instance_destroy remove_from_cloud(){

        List<String> instances = new ArrayList<>();
        instances.add(id);

       return cloud_homer_server.remove_instance(instances);
    }


    //-- Verification --//
    @JsonIgnore @Transient
    public void cloud_verification_token_GRID(WS_Message_Grid_token_verification help){
        try {

            terminal_logger.debug("cloud_GRID verification_token::  Checking Token");
            WS_HomerServer server = Controller_WebSocket.homer_servers.get(server_id());


            Model_GridTerminal terminal = null;
            Model_MProgramInstanceParameter parameter =null;

            terminal = Model_GridTerminal.find.where().eq("terminal_token", help.token).findUnique();
            if(terminal == null) parameter = Model_MProgramInstanceParameter.find.where()
                    .eq("connection_token", help.token)
                    .isNotNull("m_project_program_snapshot.instance_versions.instance_record.actual_running_instance")
                    .findUnique();


            // Terminal is not null - Ita clasic terminal connection
            if(terminal != null){

                // TODO TOM - nechybí tu "terminal ="
                Model_GridTerminal.find.where().eq("terminal_token", help.token).findUnique();

                if(terminal == null){
                    terminal_logger.warn("cloud_verification_token:: Grid_Terminal object not found!");
                    Controller_WebSocket.homer_servers.get(server_id()).write_without_confirmation(help.get_result(false));
                    return;
                }

                Integer size;

                if(terminal.person == null) {
                    terminal_logger.debug("cloud_verification_token:: Grid_Terminal object has not own Person - its probably public - Trying to find Instance");
                    size = Model_HomerInstance.find.where().eq("id", help.instanceId).eq("actual_instance.version_object.public_version", true).findRowCount();
                }else {
                    terminal_logger.debug("cloud_verification_token:: Grid_Terminal object has  own Person - its probably private or it can be public - Trying to find Instance with user ID and public value");
                    size = Model_HomerInstance.find.where().eq("id", help.instanceId)
                            .disjunction()
                            .eq("b_program.project.participants.person.id", terminal.person.id)
                            .eq("actual_instance.version_object.public_version", true)
                            .findRowCount();
                }

                if(size == 0){
                    terminal_logger.warn("cloud_verification_token:: Token found but this user has not permission!");
                    Controller_WebSocket.homer_servers.get(server_id()).write_without_confirmation(help.get_result(false));
                    return;
                }

                terminal_logger.debug("Cloud_Homer_server:: cloud_verification_token:: Token found and user have permission");
                Controller_WebSocket.homer_servers.get(server_id()).write_without_confirmation(help.get_result(true));
                return;
            }

            // Terminal is not null - Its a parameter connection
            if(parameter != null){
                Controller_WebSocket.homer_servers.get(server_id()).write_without_confirmation(help.get_result(parameter.verify_token_for_homer_grid_connection(help)));
            }

            terminal_logger.warn("cloud_verification_token_GRID - Token not recognized!");
            Controller_WebSocket.homer_servers.get(server_id()).write_without_confirmation(help.get_result(false));

        }catch (Exception e){
            terminal_logger.internalServerError("cloud_verification_token_GRID:", e);
        }
    }

    @JsonIgnore @Transient
    public void cloud_verification_token_WEBVIEW(WS_Message_WebView_token_verification help){
        try {

            terminal_logger.debug("cloud_verification_token:: WebView  Checking Token");

            Model_FloatingPersonToken floatingPersonToken = Model_FloatingPersonToken.find.where().eq("authToken", help.token).findUnique();

            if(floatingPersonToken == null){
                terminal_logger.warn("cloud_verification_token:: FloatingPersonToken not found!");
                Controller_WebSocket.homer_servers.get(server_id()).write_without_confirmation(help.get_result(false));
                return;
            }

            terminal_logger.debug("Cloud_Homer_server:: cloud_verification_token:: WebView FloatingPersonToken Token found and user have permission");
            Controller_WebSocket.homer_servers.get(server_id()).write_without_confirmation(help.get_result(true));
            return;

        }catch (Exception e){
            terminal_logger.internalServerError("cloud_verification_token_WEBVIEW:", e);
        }
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient public boolean create_permission()  {  return  getB_program().read_permission()   || Controller_Security.get_person().permissions_keys.containsKey("B_Program_create");  }
    @JsonProperty @Transient public boolean update_permission()  {  return  getB_program().update_permission() || Controller_Security.get_person().permissions_keys.containsKey("Instance_update");  }
    @JsonIgnore   @Transient public boolean read_permission()    {  return  getB_program().read_permission()   || Controller_Security.get_person().permissions_keys.containsKey("Instance_read");   }
    @JsonProperty @Transient public boolean edit_permission()    {  return  getB_program().edit_permission()   || Controller_Security.get_person().permissions_keys.containsKey("Instance_edit");    }
    @JsonProperty @Transient public boolean delete_permission()  {  return  getB_program().delete_permission() || Controller_Security.get_person().permissions_keys.containsKey("Instance_delete");  }

    public enum permissions{ Instance_create, Instance_update, Instance_read, Instance_edit , Instance_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String, Model_HomerInstance> find = new Finder<>(Model_HomerInstance.class);

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE        = Model_HomerInstance.class.getSimpleName();
    public static final String CACHE_STATUS = Model_HomerInstance.class.getSimpleName() + "_STATUS";

    public static Cache<String, Model_HomerInstance> cache = null; // Server_cache Override during server initialization
    public static Cache<String, Boolean> cache_status = null; // Server_cache Override during server initialization

    public static Model_HomerInstance get_byId(String id){

        Model_HomerInstance instance = cache.get(id);
        if(instance == null){

            instance = Model_HomerInstance.find.byId(id);
            if (instance == null) return  null;
            
            cache.put(id, instance);
        }

        return instance;
    }

}
