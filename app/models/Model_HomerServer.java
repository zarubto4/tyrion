package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.documentdb.DocumentClientException;
import controllers._BaseController;
import controllers.Controller_WebSocket;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.libs.Json;
import utilities.Server;
import utilities.cache.CacheField;
import utilities.document_mongo_db.document_objects.DM_HomerServer_Connect;
import utilities.document_mongo_db.document_objects.DM_HomerServer_Disconnect;
import utilities.enums.HomerType;
import utilities.enums.LogLevel;
import utilities.enums.NetworkStatus;
import utilities.enums.ServerMode;
import utilities.errors.ErrorCode;
import utilities.errors.Exceptions.Result_Error_Bad_request;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.homer_auto_deploy.DigitalOceanThreadRegister;
import utilities.homer_auto_deploy.DigitalOceanTyrionService;
import utilities.homer_auto_deploy.models.common.Swagger_ExternalService;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.slack.Slack;
import utilities.swagger.output.Swagger_UpdatePlan_brief_for_homer;
import websocket.WS_Message;
import websocket.interfaces.WS_Homer;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_online_status;
import websocket.messages.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Command;
import websocket.messages.homer_with_tyrion.*;
import websocket.messages.homer_with_tyrion.WS_Message_Homer_Instance_add;
import websocket.messages.homer_with_tyrion.WS_Message_Homer_Instance_destroy;
import websocket.messages.homer_with_tyrion.verification.WS_Message_Check_homer_server_permission;
import websocket.messages.tyrion_with_becki.WS_Message_Online_Change_status;

import javax.persistence.*;
import java.util.*;
import java.util.List;

@Entity
@ApiModel(description = "Model of HomerServer", value = "HomerServer")
@Table(name="HomerServer")
public class Model_HomerServer extends TaggedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_HomerServer.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore public String connection_identifier;
    @JsonIgnore public String hash_certificate;

    @JsonIgnore @Column(columnDefinition = "TEXT") public String json_additional_parameter;        // DB dokument - smožností rozšíření na cokoliv

    @ApiModelProperty(required = true, readOnly = true) public Integer mqtt_port;                       // MqTT Port
    @ApiModelProperty(required = true, readOnly = true) public Integer grid_port;                       // Grid APP
    @ApiModelProperty(required = true, readOnly = true) public Integer web_view_port;                   // Blocko web View
    @ApiModelProperty(required = true, readOnly = true) public Integer hardware_logger_port;              // HW logger
    @ApiModelProperty(required = true, readOnly = true) public Integer rest_api_port;                   // Rest APi Port

    @ApiModelProperty(required = true, readOnly = true) public String server_url;       // Může být i IP adresa
    @ApiModelProperty(required = true, readOnly = true) public String server_version;   // Může být i IP adresa

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Project project;
    @JsonIgnore @OneToMany(mappedBy = "server_main", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_Instance> instances = new ArrayList<>();

    public HomerType server_type;                     // Určující typ serveru
    public Date time_stamp_configuration;             // Čas konfigurace

    // Stav Deploy
    public Integer days_in_archive;
    public boolean logging;
    public boolean interactive;
    public LogLevel log_level;


    // Příznak, beukládaný do databáze, je true v případě že probíhá deployment na serveru
    @Transient @JsonIgnore public boolean deployment_in_progress = false;

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true) @JsonProperty
    public NetworkStatus online_state() {
        try{

            if(deployment_in_progress) {
                return NetworkStatus.SYNCHRONIZATION_IN_PROGRESS;
            }

            return Controller_WebSocket.homers.containsKey(id) ? NetworkStatus.ONLINE : NetworkStatus.OFFLINE;
        } catch (_Base_Result_Exception e){
            //nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @ApiModelProperty(required = false, readOnly = true) @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public String connection_identificator() {
        try {
            check_update_permission();
            return connection_identifier;
        } catch (_Base_Result_Exception e){
            //nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @ApiModelProperty(required = false, readOnly = true) @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public String hash_certificate() {
        try {
            check_update_permission();
            return hash_certificate;
        } catch (_Base_Result_Exception e){
            //nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @ApiModelProperty(required = false, readOnly = true, value = "Visible only when server is in deployment state")
    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean deployment_in_progress() {
        try {

            if (cache().get(DigitalOceanThreadRegister.class) != null) {
               return true;
            }

            return null;

        } catch (_Base_Result_Exception e){
            //nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public UUID get_project_id() throws _Base_Result_Exception  {

        if (cache().get(Model_Project.class) == null) {
            cache().add(Model_Project.class, Model_Project.find.query().where().eq("servers.id", id).select("id").findSingleAttributeList());
        }

        return cache().get(Model_Project.class);

    }

    @JsonIgnore @Transient public Model_Project get_project() throws _Base_Result_Exception  {
        try {
            return Model_Project.getById(get_project_id());
        }catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore @Transient public Swagger_ExternalService external_settings() {
        if(json_additional_parameter == null ) return null;
        return baseFormFactory.formFromJsonWithValidation(Swagger_ExternalService.class, Json.parse(json_additional_parameter));
    }

    /* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore
    @Override
    public void save() {

        logger.debug("save::Creating new Object");
        this.time_stamp_configuration = new Date();

        // TODO - ADD SSH public KEY by USER
        if (hash_certificate == null)
            hash_certificate = UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString();
        if (connection_identifier == null)
            connection_identifier = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();

        // Save Object
        super.save();

        //Cache Update
        cache.put(this.id, this);
    }

    @JsonIgnore
    @Override
    public void update() {

        logger.debug("update::Update object Id: {}",  this.id);

        // Update Object
        super.update();

        //this.set_new_configuration_on_homer();

        //Cache Update
        cache.put(this.id, this);

    }

    @JsonIgnore
    @Override
    public boolean delete() {

        logger.debug("delete::Delete object Id: {}",  this.id);

        try {
            if (external_settings() != null) {
                DigitalOceanTyrionService.remove(this);
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return super.delete();
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    public static Model_HomerServer get_destination_server() {


        String server_id = null;
        Integer count = null;

        if (Server.mode == ServerMode.PRODUCTION) {

            logger.debug("get_destination_server:: Creating new instance in production mode on production server");

            for (Object unique_identificator_help : Model_HomerServer.find.query().where().eq("server_type", HomerType.PUBLIC).findIds()) {

                Integer actual_Server_count = Model_Instance.find.query().where().eq("server_main.id", server_id).findCount();

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

            logger.debug("get_destination_server:: Detination server is " + server_id);
            return Model_HomerServer.getById(server_id);

        }


        /*
            Pro stage server platí komplikovanější vyjímka - v případě že má stejné možnosti jako production server se chová jako production,
            to jest přiděluje instance na public servery. Pokud nejsou k dispozici - registrují se všechny na main.
        */
        if (Server.mode == ServerMode.STAGE) {


            if (Model_HomerServer.find.query().where().eq("server_type", HomerType.PUBLIC).findCount() < 1) {

                return Model_HomerServer.find.query().where().eq("server_type", HomerType.MAIN).findOne();

            } else {

                for (Object unique_identificator_help : Model_HomerServer.find.query().where().eq("server_type", HomerType.PUBLIC).findIds()) {

                    Integer actual_Server_count = Model_Instance.find.query().where().eq("server_main.id", server_id).findCount();

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

                return Model_HomerServer.getById(server_id);
            }
        }

        /*
                V Developer režimu se instance a vše další přiděluje na Test server, který je vytvořen pomocí demodat. Spoléhá se na to,
                že se vytváří jen jeden server.
         */
        if (Server.mode == ServerMode.DEVELOPER) {
            return Model_HomerServer.find.query().where().eq("server_type", HomerType.TEST).setMaxRows(1).findOne();
        }

        return null;

    }

    @JsonIgnore
    public String get_Grid_APP_URL() {
        return server_url + ":" + grid_port + "/";
    }

    @JsonIgnore
    public String get_WebView_APP_URL() {
        return server_url + ":" + web_view_port + "/";
    }

/* SERVER WEBSOCKET CONTROLLING OF HOMER SERVER--------------------------------------------------------------------------*/

    public static final String CHANNEL = "homer_server";

    public static void Messages(WS_Homer homer, ObjectNode json) {

        new Thread(() -> {

            try {

                switch (json.get("message_type").asText()) {

                    case "tyrion_ping": {
                        // Dont do nothing - Its just a ping for taking a connection
                        return;
                    }

                    case WS_Message_Check_homer_server_permission.message_type: {

                        approve_validation_for_homer_server(homer, baseFormFactory.formFromJsonWithValidation(WS_Message_Check_homer_server_permission.class, json));
                        return;
                    }

                    case WS_Message_Homer_Token_validation_request.message_type: {

                        validate_incoming_user_connection_to_hardware_logger(homer, baseFormFactory.formFromJsonWithValidation(WS_Message_Homer_Token_validation_request.class, json));
                        return;
                    }



                    default: {
                        logger.warn("Incoming Message not recognized::" + json.toString());
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

    @JsonIgnore
    public ObjectNode write_with_confirmation(ObjectNode json, Integer time, Integer delay, Integer number_of_retries) {

        try {

            if (!Controller_WebSocket.homers.containsKey(this.id)) {

                ObjectNode request = Json.newObject();
                request.put("message_type", json.get("message_type").asText());
                request.put("status", "error");
                request.put("message_channel", Model_HomerServer.CHANNEL);
                request.put("error_code", ErrorCode.HOMER_IS_OFFLINE.error_code());
                request.put("error_message", ErrorCode.HOMER_IS_OFFLINE.error_message());
                request.put("message_id", json.has("message_id") ? json.get("message_id").asText() : "unknown");
                request.put("websocket_identificator", this.id.toString());

                return request;
            }

            return Controller_WebSocket.homers.get(this.id).sendWithResponse(new WS_Message(json, delay, time, number_of_retries));

        } catch (Exception e) {
            logger.error("write_with_confirmation - exception", e);

            ObjectNode request = Json.newObject();
            request.put("message_type", json.get("message_type").asText());
            request.put("status", "error");
            request.put("message_channel", json.get("message_channel").asText());
            request.put("error_code", ErrorCode.HOMER_IS_OFFLINE.error_code());
            request.put("error_message", ErrorCode.HOMER_IS_OFFLINE.error_message());
            request.put("message_id", json.has("message_id") ? json.get("message_id").asText() : "unknown");
            request.put("websocket_identificator", this.id.toString());
            return request;

        }
    }

    @JsonIgnore
    public void write_without_confirmation(ObjectNode message) {
        if (Controller_WebSocket.homers.containsKey(this.id)) {
            Controller_WebSocket.homers.get(this.id).send(message);
        }
    }

    @JsonIgnore
    public void write_without_confirmation(String message_id, ObjectNode message) {
        if (Controller_WebSocket.homers.containsKey(this.id)) {
            message.put("message_id", message_id);
            Controller_WebSocket.homers.get(this.id).send(message);
        }
    }


    // Permission
    public static void approve_validation_for_homer_server(WS_Homer ws_homer, WS_Message_Check_homer_server_permission message) {
        try {

            logger.debug("approve_validation_for_homer_server:: message.hash_token {}", message.hash_token);

            Model_HomerServer server = Model_HomerServer.getById(ws_homer.id);
            logger.debug("approve_validation_for_homer_server:: Server ID {}, Server Name: {}", server.id, server.name);
            logger.debug("approve_validation_for_homer_server:: server.hash_token {}", server.hash_certificate);

            if (message.hash_token.equals(server.hash_certificate)) {

                Model_HomerServer homer_server = Model_HomerServer.getById(ws_homer.id);
                homer_server.make_log_connect();

                ws_homer.verificationSuccess(message.message_id);

                // Send echo to all connected users (its public servers)
                if (homer_server.server_type.equals(HomerType.PUBLIC) || homer_server.server_type.equals(HomerType.MAIN) || homer_server.server_type.equals(HomerType.BACKUP)) {
                    WS_Message_Online_Change_status.synchronize_online_state_with_becki_public_objects(Model_HomerServer.class, homer_server.id, true);
                }

                if (homer_server.server_type.equals(HomerType.PRIVATE)) {
                    WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_HomerServer.class,homer_server.id, true, homer_server.get_project_id());
                }

                return;

            } else {

                logger.error("approve_validation_for_homer_server:: Hash is not same on Server {} {}", server.id, server.name);
                logger.error("approve_validation_for_homer_server:: message.hash_token: {}", message.hash_token);
                logger.error("approve_validation_for_homer_server:: server.hash_token:  {}", server.hash_certificate);

                ws_homer.authorized = false;
                ws_homer.token = null;

                ws_homer.verificationFail(message.message_id);
                return;
            }

        } catch (Exception e) {
            logger.internalServerError(e);
            logger.error("approve_validation_for_homer_server: Error");
            ws_homer.verificationFail(UUID.randomUUID().toString());

        }
    }

    @JsonIgnore
    @Transient
    public static void validate_incoming_user_connection_to_hardware_logger(WS_Homer ws_homer, WS_Message_Homer_Token_validation_request message) {
        try {


            Model_HomerServer server = Model_HomerServer.getById(ws_homer.id);
            if (server == null) {
                logger.error("validate_incoming_user_connection_to_hardware_logger:: homer server is null");
                return;
            }

            Model_AuthorizationToken model_token = null;

            // Zjistím, zda v Cache už token není Pokud není - vyhledám Token objekt a ověřím jeho platnost
            if (!Model_Person.token_cache.containsKey(UUID.fromString(message.client_token))) {

                model_token = Model_AuthorizationToken.find.query().where().eq("token", UUID.fromString(message.client_token)).findOne();
                if (model_token == null || !model_token.isValid()) {
                    logger.info("validate_incoming_user_connection_to_hardware_logger:: Token::" + message.client_token + " is not t is no longer valid according time");
                    ws_homer.send(message.get_result(false));
                    return;
                }

                try {
                    model_token.get_person();
                    Model_Person.token_cache.put(UUID.fromString(message.client_token), model_token.get_person_id());
                } catch (Exception e){
                    logger.error("getUsername:: Model_FloatingPersonToken not contains Person!");
                    ws_homer.send(message.get_result(false));
                    return;
                }
            }

            Model_Person person = Model_Person.getById(Model_Person.token_cache.get(UUID.fromString(message.client_token)));

            if(person == null) {
                logger.warn("validate_incoming_user_connection_to_hardware_logger:: person is null!");
                ws_homer.send(message.get_result(false));
                return;
            }

           // Permition for everyone!
           if (server.server_type == HomerType.PUBLIC || server.server_type == HomerType.BACKUP || server.server_type == HomerType.MAIN) {

               logger.trace("validate_incoming_user_connection_to_hardware_logger:: validation true for token {}", message.client_token);
               ws_homer.send(message.get_result(true));

           // Kontrola oprávnění k serveru navíc
           } else {

                if(Model_Project.find.query().where().eq("servers.id", server.id).eq("participants.person.id", person.id).select("id").findOne() != null) {
                    logger.trace("validate_incoming_user_connection_to_hardware_logger:: Private Server Find fot this Person");
                    ws_homer.send(message.get_result(true));
                    return;
                }else {
                    logger.warn("validate_incoming_user_connection_to_hardware_logger:: Private Server NOT Find fot this Person");
                    ws_homer.send(message.get_result(false));
                    return;
                }
           }

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    // Settings
    /*
    @JsonIgnore @Transient  public void set_new_configuration_on_homer() {
        try {

            if (!server_is_online()) return;

            Synchronize_Homer_Synchronize_Settings check = new Synchronize_Homer_Synchronize_Settings();
            check.start();

        } catch (Exception e) {
            logger.internalServerError("set_new_configuration_on_homer:", e);
        }
    }
    */

    // Get Data

    @JsonIgnore
    public WS_Message_Hardware_online_status device_online_synchronization_ask(List<UUID> list) {
        try {

            logger.trace("device_online_synchronization_ask:: Making Request");
            JsonNode node = write_with_confirmation(new WS_Message_Hardware_online_status().make_request(list), 1000 * 5, 0, 2);
            return  baseFormFactory.formFromJsonWithValidation(WS_Message_Hardware_online_status.class, node);

        }catch (Exception e){
            logger.internalServerError(e);
            return new WS_Message_Hardware_online_status();
        }
    }

    @JsonIgnore
    public WS_Message_Homer_Instance_list get_homer_server_list_of_instance() {
        try {

            JsonNode node = write_with_confirmation(new WS_Message_Homer_Instance_list().make_request(), 1000 * 15, 0, 2);
            return baseFormFactory.formFromJsonWithValidation(this, WS_Message_Homer_Instance_list.class, node);

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Homer_Instance_list();
        }
    }

    @JsonIgnore
    public WS_Message_Homer_Hardware_list get_homer_server_list_of_hardware() {
        try {

            JsonNode node = write_with_confirmation(new WS_Message_Homer_Hardware_list().make_request(), 1000 * 15, 0, 2);

            return baseFormFactory.formFromJsonWithValidation(this, WS_Message_Homer_Hardware_list.class, node);

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Homer_Hardware_list();
        }
    }

    @JsonIgnore
    @Transient
    public WS_Message_Homer_Instance_number get_homer_server_number_of_instance() {
        try {

            JsonNode node = write_with_confirmation(new WS_Message_Homer_Instance_number().make_request(), 1000 * 5, 0, 2);
            return baseFormFactory.formFromJsonWithValidation(this, WS_Message_Homer_Instance_number.class, node);

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Homer_Instance_number();
        }
    }


    // Add & Remove Instance

    @JsonIgnore
    @Transient
    public WS_Message_Homer_Instance_add add_instance(Model_Instance instance) {
        try {

            JsonNode node = write_with_confirmation(new WS_Message_Homer_Instance_add().make_request(instance.id), 1000 * 5, 0, 2);

            return baseFormFactory.formFromJsonWithValidation(this, WS_Message_Homer_Instance_add.class, node);

        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return new WS_Message_Homer_Instance_add();
    }

    @JsonIgnore
    @Transient
    public WS_Message_Homer_Instance_destroy remove_instance(List<UUID> instance_ids) {
        try {

            new WS_Message_Homer_Instance_destroy().make_request(instance_ids);

            JsonNode node = write_with_confirmation(new WS_Message_Homer_Instance_destroy().make_request(instance_ids), 1000 * 5, 0, 2);

            return baseFormFactory.formFromJsonWithValidation(this, WS_Message_Homer_Instance_destroy.class, node);

        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return new WS_Message_Homer_Instance_destroy();
    }


    // Updates

    @JsonIgnore
    public WS_Message_Hardware_UpdateProcedure_Command update_devices_firmware(List<Swagger_UpdatePlan_brief_for_homer> tasks) {
        try {

            JsonNode node = write_with_confirmation(new WS_Message_Hardware_UpdateProcedure_Command().make_request(tasks), 1000 * 60, 0, 2);

            return baseFormFactory.formFromJsonWithValidation(WS_Message_Hardware_UpdateProcedure_Command.class, node);

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Hardware_UpdateProcedure_Command();
        }
    }


    // Supported

    @JsonIgnore
    @Transient
    public void is_disconnect() {
        try {
            logger.error("is_disconnect:: Tyrion lost connection with Homer server: " + id);
            make_log_disconnect();

            // Send echo to all connected users (its public servers)
            if (server_type == HomerType.PUBLIC || server_type == HomerType.MAIN || server_type == HomerType.BACKUP) {
                WS_Message_Online_Change_status.synchronize_online_state_with_becki_public_objects(Model_HomerServer.class, this.id, false);
            }

            if (server_type == HomerType.PRIVATE) {
                WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_HomerServer.class, this.id, false, get_project_id());
            }

            if (Server.mode == ServerMode.STAGE) {
                Slack.homer_server_offline(this);
            }
        } catch (Exception e) {

        }
    }

    @JsonIgnore
    @Transient
    public WS_Message_Homer_ping ping() {
        try {

            JsonNode node = write_with_confirmation(new WS_Message_Homer_ping().make_request(), 1000 * 2, 0, 2);

            return baseFormFactory.formFromJsonWithValidation(WS_Message_Homer_ping.class, node);

        } catch (Exception e) {
            logger.warn("Cloud Homer server {} Id {} is offline!", name, id);
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
                logger.internalServerError(e);
            }
        }).start();
    }

    public void make_log_disconnect() {
        new Thread(() -> {
            try {
                Server.documentClient.createDocument(Server.online_status_collection.getSelfLink(), DM_HomerServer_Disconnect.make_request(this.id.toString()), null, true);
            } catch (DocumentClientException e) {
                logger.internalServerError(e);
            }
        }).start();
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if(project != null && server_type != HomerType.PRIVATE){
            throw new Result_Error_Bad_request("Server must be PRIVATE if its registered with Project");
        }

        if(project != null) {
            project.check_update_permission();
            return;
        }

        if(_BaseController.person().has_permission(Permission.Homer_create.name())) return;

    }
    @JsonIgnore @Transient @Override public void check_read_permission() throws _Base_Result_Exception {
        try {


            if(server_type == HomerType.PUBLIC || server_type == HomerType.MAIN  || server_type == HomerType.BACKUP) return;

            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(this.getClass().getSimpleName()  + "_read_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_read_" + id);
                return;
            }

            if ( Model_HomerServer.find.query().where().where().eq("project.participants.person.id", _BaseController.person().id ).eq("id", id).findCount() > 0) {
                _BaseController.person().cache_permission(this.getClass().getSimpleName()  + "_read_" + id, true);
                return;
            }

            if (_BaseController.person().has_permission(Permission.Homer_read.name())) return;

            throw new Result_Error_PermissionDenied();

        // Přidávám do listu false a vracím false
        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_read_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }

    @JsonIgnore @Transient @Override public void check_update_permission()  throws _Base_Result_Exception {
        try {
            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(this.getClass().getSimpleName() +  "_update_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_update_" + id);
                return;
            }

            if ( Model_HomerServer.find.query().where().where().eq("project.participants.person.id", _BaseController.person().id ).eq("id", id).findCount() > 0) {
                _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_update_" + id, true);
                return;
            }

            if (_BaseController.person().has_permission(Permission.Homer_update.name())) return;

            throw new Result_Error_PermissionDenied();

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_update_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }
    @JsonIgnore @Transient @Override public void check_delete_permission()  throws _Base_Result_Exception {
        try {

            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_delete_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_delete_" + id);
                return;
            }

            if (Model_HomerServer.find.query().where().where().eq("project.participants.person.id", _BaseController.person().id).eq("id", id).findCount() > 0) {
                _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, true);
                return;
            }

            if (_BaseController.person().has_permission(Permission.Homer_delete.name())) return;

            throw new Result_Error_PermissionDenied();

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }
    public enum Permission { Homer_create, Homer_read, Homer_update, Homer_delete }


/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_HomerServer.class, duration = 600)
    public static Cache<UUID, Model_HomerServer> cache;

    public static Model_HomerServer getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }
    
    public static Model_HomerServer getById(UUID id) throws _Base_Result_Exception  {

        Model_HomerServer server = cache.get(id);
        if (server == null) {

            server = find.byId(id);
            if (server == null) throw new Result_Error_NotFound(Model_HomerServer.class);

            cache.put(id, server);
        }
        // Check Permission
        if(server.its_person_operation()) {
            server.check_read_permission();
        }

        return server;
    }

    public static List<Model_HomerServer> get_all() {
        return Model_HomerServer.find.all();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_HomerServer> find = new Finder<>(Model_HomerServer.class);

}
