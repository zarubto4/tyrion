package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import org.apache.commons.io.FileExistsException;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.CompilationStatus;
import utilities.enums.NotificationImportance;
import utilities.enums.NotificationLevel;
import utilities.hardware.update.Updatable;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.notifications.helps_objects.Notification_Text;
import websocket.messages.compilator_with_tyrion.WS_Message_Make_compilation;

import javax.persistence.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionStage;

@Entity
@ApiModel(value = "Compilation", description = "Model of Compilation")
@Table(name="Compilation")
public class Model_Compilation extends BaseModel implements Updatable {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Compilation.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name="c_compilation_version") public Model_CProgramVersion version;

                                 @JsonIgnore  public CompilationStatus status;

                @Column(columnDefinition = "TEXT") public String virtual_input_output;
    @JsonIgnore @Column(columnDefinition = "TEXT") public String build_url;
    @JsonIgnore @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY) @JoinColumn(name="bin_compilation_file_id")  public Model_Blob blob;

    @JsonIgnore  public String firmware_version_core;
    @JsonIgnore  public String firmware_version_mbed;
    @JsonIgnore  public String firmware_version_lib;        // v1.0.1, v1.0.2 etc...
    @JsonIgnore  public String firmware_build_id;
    @JsonIgnore  public Date firmware_build_datetime;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_Blob getBlob() {
        return isLoaded("blob") ? blob : Model_Blob.find.query().where().eq("c_compilations_binary_file.id", id).findOne();
    }

    @JsonIgnore
    public Model_CProgramVersion getVersion() {
        return isLoaded("version") ? version : Model_CProgramVersion.find.query().where().eq("compilation.is", this.id).findOne();
    }

    @JsonProperty
    public String file_path() {
        try {
            return getBlob().link;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* EXECUTION METHODS ----------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_Compilation make_a_individual_compilation(WS_Message_Make_compilation compilation_Result, String library_compilation_version) throws Exception {

        WSClient ws = Server.injector.getInstance(WSClient.class);
        CompletionStage<? extends WSResponse> responsePromise = ws.url(compilation_Result.build_url)
                .setContentType("undefined")
                .setFollowRedirects(true)
                .setRequestTimeout(Duration.ofMillis(7500))
                .get();

        byte[] body = responsePromise.toCompletableFuture().get().asByteArray();

        //(response -> body = response.asByteArray())
        //      get().asByteArray();

        if (body == null || body.length == 0) {
            throw new FileExistsException("Body length is 0");
        }

        logger.trace("compile_program_procedure:: Body is ok - uploading to Azure");


        Model_Compilation compilation = new Model_Compilation();
        compilation.status = CompilationStatus.IN_PROGRESS;
        compilation.firmware_version_lib = library_compilation_version;
        compilation.save();

        // Daný soubor potřebuji dostat na Azure a Propojit s verzí
        compilation.blob = Model_Blob.upload(body, "application/octet-stream", "firmware.bin", get_path_for_bin());

        logger.trace("compile_program_procedure:: Body is ok - uploading to Azure was successful");
        compilation.status = CompilationStatus.SUCCESS;
        compilation.build_url = compilation_Result.build_url;
        compilation.firmware_build_id = compilation_Result.build_id_in_firmware;
        compilation.virtual_input_output = compilation_Result.interface_code;
        compilation.firmware_build_datetime = new Date();
        compilation.update();

        return compilation;
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_Notification notificationStart() {
        return new Model_Notification()
                .setImportance(NotificationImportance.LOW)
                .setLevel(NotificationLevel.INFO)
                .setText(new Notification_Text().setText("Compilation of the "))
                .setObject(this.getVersion())
                .setText(new Notification_Text().setText(" has started."));
    }

    @JsonIgnore
    public Model_Notification notificationSuccess() {
        return new Model_Notification()
                .setImportance(NotificationImportance.LOW)
                .setLevel(NotificationLevel.SUCCESS)
                .setText(new Notification_Text().setText("Compilation of the "))
                .setObject(this.getVersion())
                .setText(new Notification_Text().setText("was successful."));
    }

    @JsonIgnore
    public Model_Notification notificationUnsuccessfulWarn(String message) {
        return new Model_Notification()
                .setImportance(NotificationImportance.NORMAL)
                .setLevel(NotificationLevel.WARNING)
                .setText(new Notification_Text().setText("Compilation of the "))
                .setObject(this.getVersion())
                .setText(new Notification_Text().setText(" was unsuccessful, for reason: "))
                .setText(new Notification_Text().setText(message).setBoldText());
    }

    @JsonIgnore
    public Model_Notification notificationUnsuccessfulError(String message) {
        return new Model_Notification()
                .setImportance(NotificationImportance.NORMAL)
                .setLevel(NotificationLevel.ERROR)
                .setText(new Notification_Text().setText("Compilation of the "))
                .setObject(this.getVersion())
                .setText(new Notification_Text().setText(" was unsuccessful with critical error: "))
                .setText(new Notification_Text().setText(message).setBoldText());
    }

/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public String get_path() {

        return version.get_path();
    }

    @JsonIgnore
    public static String get_path_for_bin() throws Exception {
        return "bin-files" + "/" + UUID.randomUUID().toString();
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_Compilation.class)
    public static CacheFinder<Model_Compilation> find = new CacheFinder<>(Model_Compilation.class);
}
