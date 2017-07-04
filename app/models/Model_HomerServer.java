package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.documentdb.DocumentClientException;
import controllers.Controller_Security;
import controllers.Controller_WebSocket;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.data.Form;
import play.i18n.Lang;
import utilities.Server;
import utilities.document_db.document_objects.DM_HomerServer_Connect;
import utilities.document_db.document_objects.DM_HomerServer_Disconnect;
import utilities.enums.Enum_Cloud_HomerServer_type;
import utilities.enums.Enum_Log_level;
import utilities.enums.Enum_Tyrion_Server_mode;
import utilities.independent_threads.homer_server.Synchronize_Homer_Synchronize_Settings;
import utilities.logger.Class_Logger;
import utilities.swagger.outboundClass.Swagger_UpdatePlan_brief_for_homer;
import web_socket.message_objects.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Command;
import web_socket.message_objects.homer_with_tyrion.*;
import web_socket.message_objects.homer_with_tyrion.WS_Message_Homer_Instance_add;
import web_socket.message_objects.homer_with_tyrion.WS_Message_Homer_Instance_destroy;
import web_socket.message_objects.homer_with_tyrion.verification.WS_Message_Check_homer_server_permission;
import web_socket.message_objects.homer_with_tyrion.verification.WS_Message_Homer_Verification_result;
import web_socket.services.WS_HomerServer;
import web_socket.services.WS_Interface_type;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.TimeoutException;

@Entity
@ApiModel(description = "Model of HomerServer",
        value = "HomerServer")
public class Model_HomerServer extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_HomerServer.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                       @Id                      public String unique_identificator;
                                       @JsonIgnore              public String hash_certificate;

    @JsonIgnore                                                 public String personal_server_name;
    @Column(columnDefinition = "TEXT")                          public String json_additional_parameter;

    @ApiModelProperty(required = true, readOnly = true)         public Integer mqtt_port;              // Přidává se destination_address + "/" mqtt_port
    @ApiModelProperty(required = true, readOnly = true)         public String mqtt_username;
    @ApiModelProperty(required = true, readOnly = true)         public String mqtt_password;


    @ApiModelProperty(required = true, readOnly = true)         public Integer grid_port;              // Přidává se destination_address + "/" grid_ulr
    @ApiModelProperty(required = true, readOnly = true)         public Integer web_view_port;           // Přidává se destination_address + "/" web_view_port
    @ApiModelProperty(required = true, readOnly = true)         public Integer server_remote_port;     // Přidává se destination_address + "/" web_view_port

    @ApiModelProperty(required = true, readOnly = true)         public String server_url;  // Může být i IP adresa

                                        @JsonIgnore             public Enum_Cloud_HomerServer_type server_type;  // Určující typ serveru
                                                                public Date time_stamp_configuration;

                                                                public Integer days_in_archive;
                                                                public boolean logging;
                                                                public boolean interactive;
                                                                public Enum_Log_level log_level;



    @JsonIgnore @OneToMany(mappedBy="cloud_homer_server", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_HomerInstance> cloud_instances  = new ArrayList<>();

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true) @JsonProperty @Transient  public boolean server_is_online(){ return Controller_WebSocket.homer_servers.containsKey(this.unique_identificator);}

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/
  
    @JsonIgnore @Override public void save() {

        terminal_logger.debug("save :: Creating new Object");
        
        this.time_stamp_configuration = new Date();

        if(hash_certificate == null)  // Určeno pro možnost vytvořit testovací server - manuální doplnění hash_certificate
        while(true){ // I need Unique Value
            hash_certificate = UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString();
            if (find.where().eq("hash_certificate",hash_certificate).findUnique() == null) break;
        }

        if(unique_identificator == null)    // Určeno pro možnost vytvořit testovací server - manuální doplnění unique_identificator
        while(true){ // I need Unique Value
            unique_identificator = UUID. randomUUID().toString().substring(0,10);
            if (get_byId(unique_identificator) == null) break;
        }

        super.save();

        //Cache Update
        cache.put(this.unique_identificator, this);
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object unique_identificator: {}",  this.unique_identificator);

        //Cache Update
        cache.put(this.unique_identificator, this);

        super.update();
        this.set_new_configuration_on_homer();
    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("delete :: Delete object unique_identificator: {}",  this.unique_identificator);

        //Cache Update
        cache.remove(this.unique_identificator);

        super.delete();
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public WS_HomerServer get_server_webSocket_connection(){
        return Controller_WebSocket.homer_servers.get(this.unique_identificator);
    }

    @JsonIgnore @Transient public static Model_HomerServer get_destination_server(){


        String unique_identificator = null;
        Integer count = null;
        

        if(Server.server_mode == Enum_Tyrion_Server_mode.production){

            terminal_logger.debug("get_destination_server:: Creating new instance in production mode on production server");

            for (Object unique_identificator_help : Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.public_server).findIds()) {

                Integer actual_Server_count = Model_HomerInstance.find.where().eq("cloud_homer_server.unique_identificator", unique_identificator).findRowCount();

                if (actual_Server_count == 0) {
                    unique_identificator = unique_identificator_help.toString();
                    break;
                } else if (unique_identificator == null) {

                    unique_identificator = unique_identificator_help.toString();
                    count = actual_Server_count;

                } else if (actual_Server_count < count) {
                    unique_identificator = unique_identificator_help.toString();
                    count = actual_Server_count;
                }
            }

            terminal_logger.debug("get_destination_server:: Detination server is " + unique_identificator);
            return Model_HomerServer.find.byId(unique_identificator);

        }


        /*
            Pro stage server platí komplikovanější vyjímka - v případě že má stejné možnosti jako production server se chová jako production,
            to jest přiděluje instance na public servery. Pokud nejsou k dispozici - registrují se všechny na main.
        */
        if(Server.server_mode == Enum_Tyrion_Server_mode.stage){


            if(Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.public_server).findRowCount() < 1){

                return  Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.main_server).findUnique();

            }else {

                for (Object unique_identificator_help : Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.public_server).findIds()) {

                    Integer actual_Server_count = Model_HomerInstance.find.where().eq("cloud_homer_server.unique_identificator", unique_identificator).findRowCount();

                    if (actual_Server_count == 0) {
                        unique_identificator = unique_identificator_help.toString();
                        break;
                    } else if (unique_identificator == null) {

                        unique_identificator = unique_identificator_help.toString();
                        count = actual_Server_count;

                    } else if (actual_Server_count < count) {
                        unique_identificator = unique_identificator_help.toString();
                        count = actual_Server_count;

                    }
                }

                return Model_HomerServer.find.byId(unique_identificator);
            }
        }

        /*
                V Developer režimu se instance a vše další přiděluje na Test server, který je vytvořen pomocí demodat. Spoléhá se na to,
                že se vytváří jen jeden server.
         */
        if(Server.server_mode == Enum_Tyrion_Server_mode.developer) {
            return Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.test_server).setMaxRows(1).findUnique();
        }

        return null;

    }


/* SERVER WEBSOCKET CONTROLLING OF HOMER SERVER--------------------------------------------------------------------------*/

    @JsonIgnore @Transient  public static final String CHANNEL = "homer-server";
    
    @JsonIgnore @Transient  public static void Messages(WS_HomerServer homer, ObjectNode json){

        new Thread(() -> {

            try {

                switch (json.get("message_type").asText()) {

                    case WS_Message_Check_homer_server_permission.message_type: {

                        final Form<WS_Message_Check_homer_server_permission> form = Form.form(WS_Message_Check_homer_server_permission.class).bind(json);
                        if (form.hasErrors()) throw new Exception("WS_Message_Check_homer_server_person_permission: Incoming Json from Homer Server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                        aprove_validation_for_homer_server(homer, form.get());
                        return;
                    }

                    default: {
                        terminal_logger.internalServerError(new Exception("Chanel homer-server: message_type not recognized ->" + json.get("message_type").asText()));

                        if (!Model_HomerServer.get_byId(homer.identifikator).server_is_online())
                            throw new Exception("Chanel homer-server: Prerequisite invalidly terminate of connection.");
                    }
                }
            } catch (Exception e) {
                terminal_logger.internalServerError("Messages:", e);
            }

        }).start();
    }

    @JsonIgnore @Transient  public WS_Interface_type sender(){
        return Controller_WebSocket.homer_servers.get(this.unique_identificator);
    }


    // Permission

    @JsonIgnore @Transient public static void aprove_validation_for_homer_server(WS_HomerServer ws_homer_server, WS_Message_Check_homer_server_permission message){
        try{


            if(message.hash_token.equals( Model_HomerServer.find.byId(ws_homer_server.identifikator).hash_certificate)){

                ws_homer_server.approve_server_verification(message.message_id);

                ws_homer_server.write_without_confirmation(message.message_id, new WS_Message_Homer_Verification_result().make_request(true, ws_homer_server.rest_api_token));
                return;

            }else{

                ws_homer_server.security_token_confirm = false;
                ws_homer_server.rest_api_token =  null;

                ws_homer_server.reject_server_verification(message.message_id);
                return;
            }

        }catch (Exception e){
            ws_homer_server.reject_server_verification(UUID.randomUUID().toString());
            terminal_logger.internalServerError(e);
        }
    }


    // Settings

    @JsonIgnore @Transient  public void set_new_configuration_on_homer(){
        try{

            if(!server_is_online()) return;

            Synchronize_Homer_Synchronize_Settings check = new Synchronize_Homer_Synchronize_Settings(get_server_webSocket_connection());
            check.start();

        }catch (Exception e) {
            terminal_logger.internalServerError("set_new_configuration_on_homer:", e);
        }
    }


    // Get Data

    @JsonIgnore @Transient  public WS_Message_Homer_Instance_list get_homer_server_list_od_instance(){
        try {
            if(!server_is_online()) throw new InterruptedException();
            JsonNode node = get_server_webSocket_connection().write_with_confirmation(new WS_Message_Homer_Instance_list().make_request(), 1000 * 4, 0, 3);
            final Form<WS_Message_Homer_Instance_list> form = Form.form(WS_Message_Homer_Instance_list.class).bind(node);
            if(form.hasErrors()) throw new Exception("WS_Message_Homer_Instance_list: Incoming Json for Yoda has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

            return form.get();

        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Homer_Instance_list();
        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new WS_Message_Homer_Instance_list();
        }
    }

    @JsonIgnore @Transient  public WS_Message_Homer_Hardware_list get_homer_server_list_of_hardware(){
        try {
            if(!server_is_online()) throw new InterruptedException();
            JsonNode node = get_server_webSocket_connection().write_with_confirmation(new WS_Message_Homer_Hardware_list().make_request(), 1000 * 10, 0, 2);
            final Form<WS_Message_Homer_Hardware_list> form = Form.form(WS_Message_Homer_Hardware_list.class).bind(node);
            if(form.hasErrors()) throw new Exception("WS_Message_Homer_Hardware_list: Incoming Json for Yoda has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

            return form.get();

        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Homer_Hardware_list();
        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new WS_Message_Homer_Hardware_list();
        }
    }

    @JsonIgnore @Transient  public WS_Message_Homer_Instance_number get_homer_server_number_of_instance(){
        try {
            if(!server_is_online()) throw new InterruptedException();
            JsonNode node = get_server_webSocket_connection().write_with_confirmation(new WS_Message_Homer_Instance_number().make_request(), 1000 * 4, 0, 3);
            final Form<WS_Message_Homer_Instance_number> form = Form.form(WS_Message_Homer_Instance_number.class).bind(node);
            if(form.hasErrors()) throw new Exception("WS_Message_Homer_Instance_number: Incoming Json for Yoda has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

            return form.get();
        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Homer_Instance_number();
        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new WS_Message_Homer_Instance_number();
        }
    }


    // Add & Remove Instance

    @JsonIgnore @Transient  public WS_Message_Homer_Instance_add add_instance(Model_HomerInstance instance){
        try {

            if(!server_is_online()) throw new InterruptedException();
            JsonNode node = get_server_webSocket_connection().write_with_confirmation( new WS_Message_Homer_Instance_add().make_request(instance.id), 1000 * 5, 0, 3);

            final Form<WS_Message_Homer_Instance_add> form = Form.form(WS_Message_Homer_Instance_add.class).bind(node);
            if(form.hasErrors()) throw new Exception("WS_Message_Homer_Instance_add: Incoming Json for Yoda has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

            return form.get();

        }catch (InterruptedException|TimeoutException e){
            terminal_logger.warn("Cloud Homer server", personal_server_name, " " , unique_identificator, " is offline!");
        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }

        return new WS_Message_Homer_Instance_add();
    }

    @JsonIgnore @Transient  public WS_Message_Homer_Instance_destroy remove_instance(List<String> instance_ids) {
        try {

            if(!server_is_online()) throw new InterruptedException();
            JsonNode node =  get_server_webSocket_connection().write_with_confirmation( new WS_Message_Homer_Instance_destroy().make_request(instance_ids), 1000 * 5, 0, 3);

            final Form<WS_Message_Homer_Instance_destroy> form = Form.form(WS_Message_Homer_Instance_destroy.class).bind(node);

            return form.get();

        }catch (InterruptedException|TimeoutException e){
            terminal_logger.warn("Cloud Homer server", personal_server_name, " " , unique_identificator, " is offline!");
        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }

        return new WS_Message_Homer_Instance_destroy();
    }


    // Updates

    @JsonIgnore @Transient public WS_Message_Hardware_UpdateProcedure_Command update_devices_firmware(List<Swagger_UpdatePlan_brief_for_homer> tasks){
        try {

            JsonNode node = sender().write_with_confirmation(new WS_Message_Hardware_UpdateProcedure_Command().make_request(tasks), 1000 * 60, 0, 2);

            final Form<WS_Message_Hardware_UpdateProcedure_Command> form = Form.form(WS_Message_Hardware_UpdateProcedure_Command.class).bind(node);
            if(form.hasErrors()) throw new Exception("WS_Message_Hardware_UpdateProcedure_Command: Incoming Json for Yoda has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

            return form.get();

        }catch (TimeoutException e){
            terminal_logger.warn("set_auto_backup: Timeout");
            return new WS_Message_Hardware_UpdateProcedure_Command();
        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new WS_Message_Hardware_UpdateProcedure_Command();
        }
    }


    // Supported

    @JsonIgnore @Transient  public void is_disconnect(){
        terminal_logger.debug("is_disconnect:: Tyrion lost connection with Homer server: " + unique_identificator);
        make_log_disconnect();
    }

    @JsonIgnore @Transient  public WS_Message_Homer_ping ping(){
        try {

            if(!server_is_online()) throw new InterruptedException();
            JsonNode node = get_server_webSocket_connection().write_with_confirmation(new WS_Message_Homer_ping().make_request(), 1000 * 2, 0, 2);

            final Form<WS_Message_Homer_ping> form = Form.form(WS_Message_Homer_ping.class).bind(node);
            if(form.hasErrors()){terminal_logger.error("WS_Add_new_instance:: Incoming Json for Yoda has not right Form:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString()); return new WS_Message_Homer_ping();}

            return form.get();

        }catch (InterruptedException|TimeoutException e){
            return new WS_Message_Homer_ping();
        }catch (Exception e){
            terminal_logger.warn("Cloud Homer server {} Id {} is offline!" , personal_server_name , unique_identificator);
            return new WS_Message_Homer_ping();
        }
    }



/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    public void make_log_connect(){
        new Thread( () -> {
            try {
                Server.documentClient.createDocument(Server.online_status_collection.getSelfLink(), DM_HomerServer_Connect.make_request(this.unique_identificator), null, true);
            } catch (DocumentClientException e) {
                terminal_logger.internalServerError("make_log_connect:", e);
            }
        }).start();
    }

    public void make_log_disconnect(){
        new Thread( () -> {
            try {
                Server.documentClient.createDocument(Server.online_status_collection.getSelfLink(), DM_HomerServer_Disconnect.make_request(this.unique_identificator), null, true);
            } catch (DocumentClientException e) {
                terminal_logger.internalServerError("make_log_disconnect:", e);
            }
        }).start();
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/


    @JsonIgnore @Transient public static final String read_permission_docs   = "read: User (Admin with privileges) can read public servers, User (Customer) can read own private servers";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: User (Admin with privileges) can create public cloud cloud_blocko_server where the system uniformly creating Blocko instantiates or (Customer) can create private cloud_blocko_server for own projects";

                                                                      // TODO oprávnění bude komplikovanější až se budou podporovat lokální servery
    @JsonIgnore                                                       @Transient public boolean create_permission()  {  return Controller_Security.get_person().has_permission("Cloud_Homer_Server_create");  }
    @JsonIgnore                                                       @Transient public boolean read_permission()    {  return Controller_Security.get_person().has_permission("Cloud_Homer_Server_read");    }
    @ApiModelProperty(required = true, readOnly = true) @JsonProperty @Transient public boolean edit_permission()    {  return Controller_Security.get_person().has_permission("Cloud_Homer_Server_edit");    }
    @ApiModelProperty(required = true, readOnly = true) @JsonProperty @Transient public boolean delete_permission()  {  return Controller_Security.get_person().has_permission("Cloud_Homer_Server_delete");  }

    // Speciální řízení oprávnění z důvodů ověřování identit na homer serveru
    // Tyrion v contextu nemá Http.Context.current().args.get("person"); podle kterého se běžně všude ověřuje identita!!!
    @JsonIgnore @Transient public boolean create_permission(Model_Person person)  {  return person.has_permission("Cloud_Homer_Server_create");  }
    @JsonIgnore @Transient public boolean read_permission(Model_Person person)    {  return person.has_permission("Cloud_Homer_Server_read");    }
    @JsonIgnore @Transient public boolean edit_permission(Model_Person person)    {  return person.has_permission("Cloud_Homer_Server_edit");    }
    @JsonIgnore @Transient public boolean delete_permission(Model_Person person)  {  return person.has_permission("Cloud_Homer_Server_delete");  }

    public enum permissions{Cloud_Homer_Server_create, Cloud_Homer_Server_read, Cloud_Homer_Server_edit, Cloud_Homer_Server_delete}


/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE = Model_HomerServer.class.getName() + "_MODEL";

    public static Cache<String, Model_HomerServer> cache = null; // Server_cache Override during server initialization

    public static Model_HomerServer get_byId(String id){

        Model_HomerServer server = cache.get(id);
        if(server == null){

            server = find.byId(id);
            if (server == null) return null;

            cache.put(id, server);
        }

        return server;
    }

    public static List<Model_HomerServer> get_all(){
        return Model_HomerServer.find.all();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_HomerServer> find = new Model.Finder<>(Model_HomerServer.class);
}