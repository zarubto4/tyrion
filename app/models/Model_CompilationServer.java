package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import mongo.ModelMongo_CompilationServer_OnlineStatus;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.CompilationStatus;
import utilities.enums.EntityType;
import utilities.enums.NetworkStatus;
import utilities.model.BaseModel;
import utilities.model.Publishable;
import utilities.network.JsonNetworkStatus;
import utilities.network.Networkable;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.permission.WithPermission;
import utilities.threads.compilator_server.Compilation_After_BlackOut;
import utilities.logger.Logger;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name="CompilationServer")
@ApiModel(description = "Model of CompilationServer",
          value = "Compilation_Server")
public class Model_CompilationServer extends BaseModel implements Permissible, Publishable, Networkable {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_CompilationServer.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Column(unique=true) public String personal_server_name;

    @JsonIgnore public String connection_identifier;
    @JsonIgnore public String hash_certificate;

    @ApiModelProperty(required = true, readOnly = true) @Column(unique=true) public String server_url;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonNetworkStatus @Transient
    public NetworkStatus online_state;

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
    public void compiler_server_is_disconnect() {
        logger.warn("compiler_server_is_disconnect:: Connection lost with compilation cloud_blocko_server!: " + id + " name " + personal_server_name);
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
