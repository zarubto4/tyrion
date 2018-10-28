package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.documentdb.DocumentClientException;
import controllers.Controller_WebSocket;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import mongo.ModelMongo_CompilationServer_OnlineStatus;
import play.libs.Json;
import utilities.Server;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.CompilationStatus;
import utilities.enums.EntityType;
import utilities.enums.NetworkStatus;
import utilities.errors.ErrorCode;
import utilities.model.BaseModel;
import utilities.model.Publishable;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.permission.WithPermission;
import utilities.threads.compilator_server.Compilation_After_BlackOut;
import utilities.logger.Logger;
import websocket.WS_Message;
import websocket.messages.compilator_with_tyrion.WS_Message_Make_compilation;
import websocket.messages.compilator_with_tyrion.WS_Message_Ping_compilation_server;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name="CompilationServer")
@ApiModel(description = "Model of CompilationServer",
          value = "Compilation_Server")
public class Model_CompilationServer extends BaseModel implements Permissible, Publishable {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_CompilationServer.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Column(unique=true) public String personal_server_name;

    @JsonIgnore public String connection_identifier;
    @JsonIgnore public String hash_certificate;

    @ApiModelProperty(required = true, readOnly = true) @Column(unique=true) public String server_url;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true) @JsonProperty

    public NetworkStatus online_state() {
        try {
            return Controller_WebSocket.compilers.containsKey(this.id) ? NetworkStatus.ONLINE : NetworkStatus.OFFLINE;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @WithPermission @ApiModelProperty(required = false, readOnly = true)
    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public String connection_identificator() {
        return connection_identifier;
    }

    @WithPermission @ApiModelProperty(required = false, readOnly = true)
    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public String hash_certificate() {
        return hash_certificate;
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public boolean isPublic() {
        return true;
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        // TODO - ADD SSH public KEY from USER
        if (hash_certificate == null) hash_certificate = UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString();
        if (connection_identifier == null) connection_identifier = UUID. randomUUID().toString() + "-" + UUID. randomUUID().toString() ;

        super.save();
    }

/* SERVER WEBSOCKET  --------------------------------------------------------------------------------------------------*/

    public static String CHANNEL = "compilation_server";

    @JsonIgnore
    public ObjectNode write_with_confirmation(ObjectNode json,  Integer time, Integer delay, Integer number_of_retries) {

        try {

            if (!Controller_WebSocket.compilers.containsKey(this.id)) {

                ObjectNode request = Json.newObject();
                request.put("message_type", json.get("message_type").asText());
                request.put("status", "error");
                request.put("message_channel", Model_CompilationServer.CHANNEL);
                request.put("error_code", ErrorCode.HOMER_IS_OFFLINE.error_code());
                request.put("error_message", ErrorCode.HOMER_IS_OFFLINE.error_message());
                request.put("message_id", json.has("message_id") ? json.get("message_id").asText() : "unknown");
                request.put("websocket_identificator", this.id.toString());

                return request;
            }

            return Controller_WebSocket.compilers.get(this.id).sendWithResponse(new WS_Message(json, delay, time, number_of_retries));

        } catch (Exception e) {
            logger.error("write_with_confirmation - exception", e);

            ObjectNode request = Json.newObject();
            request.put("message_type", json.get("message_type").asText());
            request.put("status", "error");
            request.put("message_channel", Model_CompilationServer.CHANNEL);
            request.put("error_code", ErrorCode.COMPILATION_SERVER_IS_OFFLINE.error_code());
            request.put("error_message", ErrorCode.COMPILATION_SERVER_IS_OFFLINE.error_message());
            request.put("message_id", json.has("message_id") ? json.get("message_id").asText() : "unknown");
            request.put("websocket_identificator", this.id.toString());

            return request;
        }
    }

    @JsonIgnore
    public static boolean is_online() {
        return !Controller_WebSocket.compilers.isEmpty();
    }

    @JsonIgnore
    public static WS_Message_Make_compilation make_Compilation(ObjectNode request) {
        try {

            // Vyberu náhodný kompilační server
            List<UUID> keys = new ArrayList<>(Controller_WebSocket.compilers.keySet());
            Model_CompilationServer server = Model_CompilationServer.find.byId(Controller_WebSocket.compilers.get(keys.get(new Random().nextInt(keys.size()))).id);

            ObjectNode compilation_request = server.write_with_confirmation(request, 5*1000, 0, 3);
            WS_Message_Make_compilation compilation = formFromJsonWithValidation(WS_Message_Make_compilation.class, compilation_request);

            if (compilation.build_url != null) {
                logger.trace("make_Compilation:: Build URL is not null: {} ", compilation.build_url);
                compilation.status = "success";
            }

            logger.trace("make_Compilation:: TOTAL RESPONSE {}",  Json.toJson(compilation).toString());
            return compilation;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Make_compilation();
        }
    }

    @JsonIgnore
    public WS_Message_Ping_compilation_server ping() {
        try {

            JsonNode json = write_with_confirmation(new WS_Message_Ping_compilation_server().make_request(),  1000 * 30, 0, 3);

            return formFromJsonWithValidation(WS_Message_Ping_compilation_server.class, json);

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Ping_compilation_server();
        }
    }

    @JsonIgnore
    public void compiler_server_is_disconnect() {
        logger.debug("compiler_server_is_disconnect:: Connection lost with compilation cloud_blocko_server!: " + id + " name " + personal_server_name);
        make_log_disconnect();
    }

    @JsonIgnore
    public void check_after_connection() {

        // Po připojení compilačního serveru s nastartuje procedura zpětné kompilace všeho.
        // Předpoklad je že se připojí více serverů (třeba po pádu nebo udpatu) a tím by došlo k průseru kdy by si
        // servery kompilovali verze navzájem.
        // Proto byl vytvořen singleton který to má řešit.

        // a) ověřím zda existuje vůbec něco, co by mělo smysl kompilovat

        Model_CProgramVersion version = Model_CProgramVersion.find.query().where().eq("compilation.status", CompilationStatus.SERVER_OFFLINE.name()).order().desc("created").setMaxRows(1).findOne();
        if (version == null) {
            logger.debug("check_after_connection:: 0 c_program versions for compilations");
            return;
        }

        make_log_connect();

        // b) pokud ano pošlu to do Compilation_After_BlackOut
        Compilation_After_BlackOut.getInstance().start(this);
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    public void make_log_connect() {
        new Thread(() -> {
            try {
                ModelMongo_CompilationServer_OnlineStatus.create_record(this, true);
            } catch (Exception e) {
                logger.internalServerError(e);
            }
        }).start();
    }

    public void make_log_disconnect() {
        new Thread(() -> {
            try {
                ModelMongo_CompilationServer_OnlineStatus.create_record(this, false);
            } catch (Exception e) {
                logger.internalServerError(e);
            }
        }).start();
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.COMPILER;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_CompilationServer.class)
    public static CacheFinder<Model_CompilationServer> find = new CacheFinder<>(Model_CompilationServer.class);
}
