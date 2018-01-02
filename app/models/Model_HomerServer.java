package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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
import play.libs.Json;
import utilities.Server;
import utilities.document_db.document_objects.DM_HomerServer_Connect;
import utilities.document_db.document_objects.DM_HomerServer_Disconnect;
import utilities.enums.Enum_Cloud_HomerServer_type;
import utilities.enums.Enum_Log_level;
import utilities.enums.Enum_Online_status;
import utilities.enums.Enum_Tyrion_Server_mode;
import utilities.errors.ErrorCode;
import utilities.logger.Class_Logger;
import utilities.swagger.outboundClass.Swagger_HomerServer_public_Detail;
import utilities.swagger.outboundClass.Swagger_UpdatePlan_brief_for_homer;
import web_socket.message_objects.common.service_class.WS_Message_Invalid_Message;
import web_socket.message_objects.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Command;
import web_socket.message_objects.homer_with_tyrion.*;
import web_socket.message_objects.homer_with_tyrion.WS_Message_Homer_Instance_add;
import web_socket.message_objects.homer_with_tyrion.WS_Message_Homer_Instance_destroy;
import web_socket.message_objects.homer_with_tyrion.verification.WS_Message_Check_homer_server_permission;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Online_Change_status;
import web_socket.services.WS_HomerServer;

import javax.management.BadAttributeValueExpException;
import javax.persistence.*;
import java.nio.channels.ClosedChannelException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Entity
@ApiModel(description = "Model of HomerServer", value = "HomerServer")
@Table(name="HomerServer")
public class Model_HomerServer extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_HomerServer.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id public UUID id;

    @JsonIgnore public String connection_identificator;
    @JsonIgnore public String hash_certificate;

    @JsonIgnore public Date date_of_create;

    public String personal_server_name;

    @Column(columnDefinition = "TEXT") public String json_additional_parameter;        // DB dokument - smožností rozšíření na cokoliv

    @ApiModelProperty(required = true, readOnly = true) public Integer mqtt_port;                       // Přidává se destination_address + "/" mqtt_port
    @ApiModelProperty(required = true, readOnly = true) public Integer grid_port;                       // Přidává se destination_address + "/" grid_ulr
    @ApiModelProperty(required = true, readOnly = true) public Integer web_view_port;                   // Přidává se destination_address + "/" web_view_port
    @ApiModelProperty(required = true, readOnly = true) public Integer server_remote_port;              // Přidává se destination_address + "/" web_view_port

    @ApiModelProperty(required = true, readOnly = true)
    public String server_url;  // Může být i IP adresa

    public Enum_Cloud_HomerServer_type server_type;  // Určující typ serveru
    public Date time_stamp_configuration;

    public Integer days_in_archive;
    public boolean logging;
    public boolean interactive;
    public Enum_Log_level log_level;


    @JsonIgnore
    @OneToMany(mappedBy = "cloud_homer_server", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Model_HomerInstance> cloud_instances = new ArrayList<>();

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    @JsonProperty
    @Transient
    public Enum_Online_status online_state() {

        return Controller_WebSocket.homer_servers.containsKey(id.toString()) ? Enum_Online_status.online : Enum_Online_status.offline;
    }

    @ApiModelProperty(required = false, readOnly = true)
    @JsonProperty
    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String connection_identificator() {

        if(this.edit_permission()) {
            return connection_identificator;
        }

        return null;
    }

    @ApiModelProperty(required = false, readOnly = true)
    @JsonProperty
    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String hash_certificate() {

        if(this.edit_permission()) {
            return hash_certificate;
        }

        return null;
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    @Transient
    public Swagger_HomerServer_public_Detail get_public_info() {

        Swagger_HomerServer_public_Detail detail = new Swagger_HomerServer_public_Detail();
        detail.id = id.toString();
        detail.personal_server_name = personal_server_name;
        detail.server_type = server_type;
        detail.online_state = online_state();
        detail.edit_permission = this.edit_permission();
        detail.update_permission = false;   // TODO: Doplnit až půjde rekonfigurovat server nadálku - Long term task
        detail.delete_permission = this.delete_permission();
        detail.server_url = server_url;     //
        detail.hardware_log_port = server_remote_port;

        return detail;
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore
    @Override
    public void save() {


        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "testEvent");

        terminal_logger.debug("save :: Creating new Object");

        this.time_stamp_configuration = new Date();

        // TODO - ADD SSH public KEY from USER
        if (hash_certificate == null)
            hash_certificate = UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString();
        if (connection_identificator == null)
            connection_identificator = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        date_of_create = new Date();

        super.save();

        //Cache Update
        cache.put(this.id.toString(), this);
    }

    @JsonIgnore
    @Override
    public void update() {

        terminal_logger.debug("update :: Update object id: {}", this.id.toString());

        //Cache Update
        cache.put(this.id.toString(), this);

        super.update();
        //this.set_new_configuration_on_homer();
    }

    @JsonIgnore
    @Override
    public void delete() {

        terminal_logger.debug("delete :: Delete object id: {}", this.id.toString());

        //Cache Update
        cache.remove(this.id.toString());

        super.delete();
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    @Transient
    public static Model_HomerServer get_destination_server() {


        String server_id = null;
        Integer count = null;

        if (Server.server_mode == Enum_Tyrion_Server_mode.production) {

            terminal_logger.debug("get_destination_server:: Creating new instance in production mode on production server");

            for (Object unique_identificator_help : Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.public_server).findIds()) {

                Integer actual_Server_count = Model_HomerInstance.find.where().eq("cloud_homer_server.id", server_id).findRowCount();

                if (actual_Server_count == 0) {
                    server_id = unique_identificator_help.toString();
                    break;
                } else if (server_id == null) {

                    server_id = unique_identificator_help.toString();
                    count = actual_Server_count;

                } else if (actual_Server_count < count) {
                    server_id = unique_identificator_help.toString();
                    count = actual_Server_count;
                }
            }

            terminal_logger.debug("get_destination_server:: Detination server is " + server_id);
            return Model_HomerServer.get_byId(server_id);

        }


        /*
            Pro stage server platí komplikovanější vyjímka - v případě že má stejné možnosti jako production server se chová jako production,
            to jest přiděluje instance na public servery. Pokud nejsou k dispozici - registrují se všechny na main.
        */
        if (Server.server_mode == Enum_Tyrion_Server_mode.stage) {


            if (Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.public_server).findRowCount() < 1) {

                return Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.main_server).findUnique();

            } else {

                for (Object unique_identificator_help : Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.public_server).findIds()) {

                    Integer actual_Server_count = Model_HomerInstance.find.where().eq("cloud_homer_server.id", server_id).findRowCount();

                    if (actual_Server_count == 0) {
                        server_id = unique_identificator_help.toString();
                        break;
                    } else if (server_id == null) {

                        server_id = unique_identificator_help.toString();
                        count = actual_Server_count;

                    } else if (actual_Server_count < count) {
                        server_id = unique_identificator_help.toString();
                        count = actual_Server_count;

                    }
                }

                return Model_HomerServer.get_byId(server_id);
            }
        }

        /*
                V Developer režimu se instance a vše další přiděluje na Test server, který je vytvořen pomocí demodat. Spoléhá se na to,
                že se vytváří jen jeden server.
         */
        if (Server.server_mode == Enum_Tyrion_Server_mode.developer) {
            return Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.test_server).setMaxRows(1).findUnique();
        }

        return null;

    }

    @JsonIgnore
    @Transient
    public String get_Grid_APP_URL() {
        return server_url + ":" + grid_port + "/";
    }

    @JsonIgnore
    @Transient
    public String get_WebView_APP_URL() {
        return server_url + ":" + web_view_port + "/";
    }

/* SERVER WEBSOCKET CONTROLLING OF HOMER SERVER--------------------------------------------------------------------------*/

    @JsonIgnore
    @Transient
    public static final String CHANNEL = "homer_server";

    @JsonIgnore
    @Transient
    public static void Messages(WS_HomerServer homer, ObjectNode json) {

        new Thread(() -> {

            try {

                switch (json.get("message_type").asText()) {

                    case WS_Message_Check_homer_server_permission.message_type: {

                        final Form<WS_Message_Check_homer_server_permission> form = Form.form(WS_Message_Check_homer_server_permission.class).bind(json);
                        if (form.hasErrors())
                            throw new Exception("WS_Message_Check_homer_server_person_permission: Incoming Json from Homer Server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                        aprove_validation_for_homer_server(homer, form.get());
                        return;
                    }

                    case WS_Message_Homer_Token_validation_request.message_type: {

                        final Form<WS_Message_Homer_Token_validation_request> form = Form.form(WS_Message_Homer_Token_validation_request.class).bind(json);
                        if (form.hasErrors())
                            throw new Exception("WS_Message_Homer_Token_validation_request: Incoming Json from Homer Server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                        validate_incoming_user_connection_to_hardware_logger(homer, form.get());
                        return;
                    }



                    default: {
                        terminal_logger.warn("Incoming Message not recognized::" + json.toString());
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

    @JsonIgnore
    @Transient
    public ObjectNode write_with_confirmation(ObjectNode json, Integer time, Integer delay, Integer number_of_retries) {

        try {

            if (!Controller_WebSocket.homer_servers.containsKey(this.id.toString())) {

                ObjectNode request = Json.newObject();
                request.put("message_type", json.get("message_type").asText());
                request.put("message_channel", Model_HomerServer.CHANNEL);
                request.put("error_code", ErrorCode.HOMER_IS_OFFLINE.error_code());
                request.put("error_message", ErrorCode.HOMER_IS_OFFLINE.error_message());
                request.put("message_id", json.has("message_id") ? json.get("message_id").asText() : "unknown");
                request.put("websocket_identificator", this.id.toString());

                return request;
            }

            return Controller_WebSocket.homer_servers.get(this.id.toString()).write_with_confirmation(json, time, delay, number_of_retries);

        } catch (ExecutionException e) {
            terminal_logger.error("Model_HomerServer:: write_with_confirmation ExecutionException");

            ObjectNode request = Json.newObject();
            request.put("message_type", json.get("message_type").asText());
            request.put("message_channel", json.get("message_channel").asText());
            request.put("error_code", ErrorCode.HOMER_IS_OFFLINE.error_code());
            request.put("error_message", ErrorCode.HOMER_IS_OFFLINE.error_message());
            request.put("message_id", json.has("message_id") ? json.get("message_id").asText() : "unknown");
            request.put("websocket_identificator", this.id.toString());
            return request;

        } catch (InterruptedException e) {

            terminal_logger.error("Model_HomerServer:: write_with_confirmation InterruptedException");
            ObjectNode request = Json.newObject();
            request.put("message_type", json.get("message_type").asText());
            request.put("message_channel", json.get("message_channel").asText());
            request.put("error_code", ErrorCode.HOMER_IS_OFFLINE.error_code());
            request.put("error_message", ErrorCode.HOMER_IS_OFFLINE.error_message());
            request.put("message_id", json.has("message_id") ? json.get("message_id").asText() : "unknown");
            request.put("websocket_identificator", this.id.toString());
            return request;

        } catch (ClosedChannelException e) {

            terminal_logger.error("Model_HomerServer:: write_with_confirmation ClosedChannelException");
            ObjectNode request = Json.newObject();
            request.put("message_type", json.get("message_type").asText());
            request.put("message_channel", json.get("message_channel").asText());
            request.put("error_code", ErrorCode.HOMER_IS_OFFLINE.error_code());
            request.put("error_message", ErrorCode.HOMER_IS_OFFLINE.error_message());
            request.put("message_id", json.has("message_id") ? json.get("message_id").asText() : "unknown");
            request.put("websocket_identificator", this.id.toString());
            return request;

        } catch (TimeoutException e) {

            terminal_logger.error("Model_HomerServer:: write_with_confirmation TimeoutException");
            ObjectNode request = Json.newObject();
            request.put("message_type", json.get("message_type").asText());
            request.put("message_channel", json.get("message_channel").asText());
            request.put("error_code", ErrorCode.WEBSOCKET_TIME_OUT_EXCEPTION.error_code());
            request.put("error_message", ErrorCode.WEBSOCKET_TIME_OUT_EXCEPTION.error_message());
            request.put("message_id", json.has("message_id") ? json.get("message_id").asText() : "unknown");
            request.put("websocket_identificator", this.id.toString());
            return request;
        }
    }

    @JsonIgnore
    @Transient
    public void write_without_confirmation(ObjectNode json) {

        if (!Controller_WebSocket.homer_servers.containsKey(this.id.toString().toString())) {
            return;
        }

        Controller_WebSocket.homer_servers.get(this.id.toString().toString()).write_without_confirmation(json);
    }

    @JsonIgnore
    @Transient
    public void write_without_confirmation(String message_id, ObjectNode json) {

        if (!Controller_WebSocket.homer_servers.containsKey(this.id.toString().toString())) {
            return;
        }

        Controller_WebSocket.homer_servers.get(this.id.toString()).write_without_confirmation(message_id, json);
    }


    // Permission

    @JsonIgnore
    @Transient
    public static void aprove_validation_for_homer_server(WS_HomerServer ws_homer_server, WS_Message_Check_homer_server_permission message) {
        try {


            if (message.hash_token.equals(Model_HomerServer.get_byId(ws_homer_server.identifikator).hash_certificate)) {

                Model_HomerServer homer_server = Model_HomerServer.get_byId(ws_homer_server.identifikator);
                homer_server.make_log_connect();

                ws_homer_server.approve_server_verification(message.message_id);

                // Send echo to all connected users (its public servers)
                if (homer_server.server_type.equals(Enum_Cloud_HomerServer_type.public_server) || homer_server.server_type.equals(Enum_Cloud_HomerServer_type.main_server) || homer_server.server_type.equals(Enum_Cloud_HomerServer_type.backup_server)) {
                    WS_Message_Online_Change_status.synchronize_online_state_with_becki_public_objects(Model_HomerServer.class, homer_server.id.toString(), true);
                }

                if (homer_server.server_type.equals(Enum_Cloud_HomerServer_type.private_server)) {
                    throw new Exception("aprove_validation_for_homer_server - TODO private server!!!");
                }

                return;

            } else {

                System.out.println("Hash nesedí ");
                System.out.println("Hash původního Serveru:: " + Model_HomerServer.get_byId(ws_homer_server.identifikator).hash_certificate);
                System.out.println("Hash příchozí  zprávy :: " + message.hash_token);

                ws_homer_server.security_token_confirm = false;
                ws_homer_server.rest_api_token = null;

                ws_homer_server.reject_server_verification(message.message_id);
                return;
            }

        } catch (Exception e) {
            ws_homer_server.reject_server_verification(UUID.randomUUID().toString());
            terminal_logger.internalServerError(e);
        }
    }

    @JsonIgnore
    @Transient
    public static void validate_incoming_user_connection_to_hardware_logger(WS_HomerServer ws_homer_server, WS_Message_Homer_Token_validation_request message){
        try {


            Model_HomerServer server = Model_HomerServer.get_byId(ws_homer_server.get_identificator());
            if(server == null) {
                terminal_logger.error("validate_incoming_user_connection_to_hardware_logger:: homer server is null");
                return;
            }

           // Permition for everyone!
           if( server.server_type == Enum_Cloud_HomerServer_type.public_server ||
                   server.server_type == Enum_Cloud_HomerServer_type.backup_server ||
                   server.server_type == Enum_Cloud_HomerServer_type.main_server){


               // Zjistím, zda v Cache už token není Pokud není - vyhledám Token objekt a ověřím jeho platnost
               if(!Model_Person.token_cache.containsKey(message.client_token)){

                   Model_FloatingPersonToken model_token = Model_FloatingPersonToken.find.where().eq("authToken", message.client_token).findUnique();
                   if(model_token == null || !model_token.isValid()){
                       terminal_logger.warn("validate_incoming_user_connection_to_hardware_logger:: Token::" + message.client_token + " is not t is no longer valid according time");
                       ws_homer_server.write_without_confirmation(message.get_result(false));
                       return;
                   }

                   if(model_token.person != null) {
                       Model_Person.token_cache.put(message.client_token, model_token.person.id);
                   }else {
                       terminal_logger.warn("getUsername:: Model_FloatingPersonToken not contains Person!");
                   }

               }


               terminal_logger.trace("validate_incoming_user_connection_to_hardware_logger:: validation true for token {}", message.client_token);
               ws_homer_server.write_without_confirmation(message.get_result(true));


           }else {
               terminal_logger.internalServerError(new BadAttributeValueExpException("Dopíče není zde dořešen privátní server!!! "));
           }

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

    // Settings
    /*
    @JsonIgnore @Transient  public void set_new_configuration_on_homer(){
        try{

            if(!server_is_online()) return;

            Synchronize_Homer_Synchronize_Settings check = new Synchronize_Homer_Synchronize_Settings();
            check.start();

        }catch (Exception e) {
            terminal_logger.internalServerError("set_new_configuration_on_homer:", e);
        }
    }
    */

    // Get Data

    @JsonIgnore
    @Transient
    public WS_Message_Homer_Instance_list get_homer_server_list_of_instance() {
        try {

            JsonNode node = write_with_confirmation(new WS_Message_Homer_Instance_list().make_request(), 1000 * 15, 0, 2);
            final Form<WS_Message_Homer_Instance_list> form = Form.form(WS_Message_Homer_Instance_list.class).bind(node);
            if (form.hasErrors()) {
                terminal_logger.warn("get_homer_server_list_of_instance:: Json Incorrect value. {}", node);
                terminal_logger.warn("get_homer_server_list_of_instance:: Response. {}", form.errorsAsJson(Lang.forCode("en-US")).toString());
                write_without_confirmation(node.get("message_id").asText(), WS_Message_Invalid_Message.make_request(WS_Message_Homer_Instance_list.message_type, form.errorsAsJson(Lang.forCode("en-US")).toString()));
                return new WS_Message_Homer_Instance_list();
            }

            return form.get();

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return new WS_Message_Homer_Instance_list();
        }
    }

    @JsonIgnore
    @Transient
    public WS_Message_Homer_Hardware_list get_homer_server_list_of_hardware() {
        try {

            JsonNode node = write_with_confirmation(new WS_Message_Homer_Hardware_list().make_request(), 1000 * 15, 0, 2);

            final Form<WS_Message_Homer_Hardware_list> form = Form.form(WS_Message_Homer_Hardware_list.class).bind(node);
            if (form.hasErrors()) {
                terminal_logger.warn("get_homer_server_list_of_hardware:: Json Incorrect value. {}", node);
                terminal_logger.warn("get_homer_server_list_of_hardware:: Response. {}", form.errorsAsJson(Lang.forCode("en-US")).toString());
                write_without_confirmation(node.get("message_id").asText(), WS_Message_Invalid_Message.make_request(WS_Message_Homer_Hardware_list.message_type, form.errorsAsJson(Lang.forCode("en-US")).toString()));

                return new WS_Message_Homer_Hardware_list();
            }


            return form.get();

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return new WS_Message_Homer_Hardware_list();
        }
    }

    @JsonIgnore
    @Transient
    public WS_Message_Homer_Instance_number get_homer_server_number_of_instance() {
        try {

            JsonNode node = write_with_confirmation(new WS_Message_Homer_Instance_number().make_request(), 1000 * 5, 0, 2);
            final Form<WS_Message_Homer_Instance_number> form = Form.form(WS_Message_Homer_Instance_number.class).bind(node);

            if (form.hasErrors()) {
                write_without_confirmation(node.get("message_id").asText(), WS_Message_Invalid_Message.make_request(WS_Message_Homer_Instance_number.message_type, form.errorsAsJson(Lang.forCode("en-US")).toString()));
                terminal_logger.warn("get_homer_server_list_of_hardware:: Json Incorrect value");
                return new WS_Message_Homer_Instance_number();
            }

            return form.get();

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return new WS_Message_Homer_Instance_number();
        }
    }


    // Add & Remove Instance

    @JsonIgnore
    @Transient
    public WS_Message_Homer_Instance_add add_instance(Model_HomerInstance instance) {
        try {

            JsonNode node = write_with_confirmation(new WS_Message_Homer_Instance_add().make_request(instance.id), 1000 * 5, 0, 2);

            final Form<WS_Message_Homer_Instance_add> form = Form.form(WS_Message_Homer_Instance_add.class).bind(node);
            if (form.hasErrors())
                throw new Exception("WS_Message_Homer_Instance_add: Incoming Json for Yoda has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

            return form.get();

        } catch (InterruptedException | TimeoutException e) {
            terminal_logger.warn("Cloud Homer server", personal_server_name, " ", id, " is offline!");
        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }

        return new WS_Message_Homer_Instance_add();
    }

    @JsonIgnore
    @Transient
    public WS_Message_Homer_Instance_destroy remove_instance(List<String> instance_ids) {
        try {

            new WS_Message_Homer_Instance_destroy().make_request(instance_ids);

            JsonNode node = write_with_confirmation(new WS_Message_Homer_Instance_destroy().make_request(instance_ids), 1000 * 5, 0, 2);

            final Form<WS_Message_Homer_Instance_destroy> form = Form.form(WS_Message_Homer_Instance_destroy.class).bind(node);

            return form.get();

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }

        return new WS_Message_Homer_Instance_destroy();
    }


    // Updates

    @JsonIgnore
    @Transient
    public WS_Message_Hardware_UpdateProcedure_Command update_devices_firmware(List<Swagger_UpdatePlan_brief_for_homer> tasks) {
        try {

            JsonNode node = write_with_confirmation(new WS_Message_Hardware_UpdateProcedure_Command().make_request(tasks), 1000 * 60, 0, 2);

            final Form<WS_Message_Hardware_UpdateProcedure_Command> form = Form.form(WS_Message_Hardware_UpdateProcedure_Command.class).bind(node);
            if (form.hasErrors())
                throw new Exception("WS_Message_Hardware_UpdateProcedure_Command: Incoming Json for Yoda has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

            return form.get();

        } catch (TimeoutException e) {
            terminal_logger.warn("set_auto_backup: Timeout");
            return new WS_Message_Hardware_UpdateProcedure_Command();
        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return new WS_Message_Hardware_UpdateProcedure_Command();
        }
    }


    // Supported

    @JsonIgnore
    @Transient
    public void is_disconnect() {
        terminal_logger.debug("is_disconnect:: Tyrion lost connection with Homer server: " + id);
        make_log_disconnect();

        // Send echo to all connected users (its public servers)
        if (server_type == Enum_Cloud_HomerServer_type.public_server || server_type == Enum_Cloud_HomerServer_type.main_server || server_type == Enum_Cloud_HomerServer_type.backup_server) {
            WS_Message_Online_Change_status.synchronize_online_state_with_becki_public_objects(Model_HomerServer.class, id.toString(), false);
        }
    }

    @JsonIgnore
    @Transient
    public WS_Message_Homer_ping ping() {
        try {

            System.out.println("vyvolávám homer ping ");

            JsonNode node = write_with_confirmation(new WS_Message_Homer_ping().make_request(), 1000 * 2, 0, 2);

            final Form<WS_Message_Homer_ping> form = Form.form(WS_Message_Homer_ping.class).bind(node);
            if (form.hasErrors()) {
                terminal_logger.error("WS_Add_new_instance:: Incoming Json for Yoda has not right Form:: " + form.errorsAsJson(new Lang(new play.api.i18n.Lang("en", "US"))).toString());
                return new WS_Message_Homer_ping();
            }

            return form.get();

        } catch (Exception e) {
            terminal_logger.warn("Cloud Homer server {} Id {} is offline!", personal_server_name, id);
            return new WS_Message_Homer_ping();
        }
    }



/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    // only with successfully connection
    public void make_log_connect() {
        new Thread(() -> {
            try {
                Server.documentClient.createDocument(Server.online_status_collection.getSelfLink(), DM_HomerServer_Connect.make_request(this.id.toString()), null, true);
            } catch (DocumentClientException e) {
                terminal_logger.internalServerError(e);
            }
        }).start();
    }

    public void make_log_disconnect() {
        new Thread(() -> {
            try {
                Server.documentClient.createDocument(Server.online_status_collection.getSelfLink(), DM_HomerServer_Disconnect.make_request(this.id.toString()), null, true);
            } catch (DocumentClientException e) {
                terminal_logger.internalServerError(e);
            }
        }).start();
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/


    @JsonIgnore
    @Transient
    public static final String read_permission_docs = "read: User (Admin with privileges) can read public servers, User (Customer) can read own private servers";
    @JsonIgnore
    @Transient
    public static final String create_permission_docs = "create: User (Admin with privileges) can create public cloud cloud_blocko_server where the system uniformly creating Blocko instantiates or (Customer) can create private cloud_blocko_server for own projects";

    // TODO oprávnění bude komplikovanější až se budou podporovat lokální servery
    @JsonIgnore
    @Transient
    public boolean create_permission() {
        return Controller_Security.get_person().has_permission("Cloud_Homer_Server_create");
    }

    @JsonIgnore
    @Transient
    public boolean read_permission() {
        return Controller_Security.get_person().has_permission("Cloud_Homer_Server_read");
    }

    @JsonProperty
    @Transient
    public boolean edit_permission() {
        return Controller_Security.get_person().has_permission("Cloud_Homer_Server_edit");
    }

    @JsonProperty
    @Transient
    public boolean delete_permission() {
        return Controller_Security.get_person().has_permission("Cloud_Homer_Server_delete");
    }

    public enum permissions {Cloud_Homer_Server_create, Cloud_Homer_Server_read, Cloud_Homer_Server_edit, Cloud_Homer_Server_delete}


/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE = Model_HomerServer.class.getName() + "_MODEL";

    public static Cache<String, Model_HomerServer> cache = null; // Server_cache Override during server initialization

    public static Model_HomerServer get_byId(String id) {

        Model_HomerServer server = cache.get(id);
        if (server == null) {

            server = find.byId(id);
            if (server == null) return null;

            cache.put(id, server);
        }

        return server;
    }

    public static List<Model_HomerServer> get_all() {
        return Model_HomerServer.find.all();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String, Model_HomerServer> find = new Model.Finder<>(Model_HomerServer.class);

}
