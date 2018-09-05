package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.blob.CloudAppendBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import org.apache.commons.io.FileExistsException;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities.cache.Cached;
import utilities.enums.CompilationStatus;
import utilities.enums.NotificationImportance;
import utilities.enums.NotificationLevel;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.models_update_echo.EchoHandler;
import utilities.notifications.helps_objects.Notification_Text;
import websocket.messages.compilator_with_tyrion.WS_Message_Make_compilation;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionStage;

@Entity
@ApiModel(value = "Compilation", description = "Model of Compilation")
@Table(name="Compilation")
public class Model_Compilation extends BaseModel {

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

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached public UUID cache_blob_id;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    public UUID blob_id()throws _Base_Result_Exception {
        if (idCache().get(Model_Blob.class) == null) {
            idCache().add(Model_Blob.class, (UUID) Model_Blob.find.query().where().eq("version.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_Blob.class);
    }

    @JsonIgnore
    public Model_Blob blob()throws _Base_Result_Exception{
    try {
        return Model_Blob.find.byId(blob_id());
    } catch (Exception e) {
        logger.internalServerError(e);
        return null;
    }
}

    @JsonProperty
    public String file_path() {
        try {

            if (cache_blob_id != null ) {
                String link = Model_Blob.cache_public_link.get(cache_blob_id);
                if (link != null) return link;
            }

            if (blob == null) { // TODO Cachovat - a opravit kde je nevhodná návaznost
                return null;
            }

            this.cache_blob_id = blob.id;

            // Separace na Container a Blob
            int slash = blob.path.indexOf("/");
            String container_name = blob.path.substring(0, slash);
            String real_file_path = blob.path.substring(slash + 1);

            CloudAppendBlob blob = Server.blobClient.getContainerReference(container_name).getAppendBlobReference(real_file_path);

            // Create Policy
            Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            cal.setTime(new Date());
            cal.add(Calendar.HOUR, 5);

            SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
            policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ));
            policy.setSharedAccessExpiryTime(cal.getTime());

            String sas = blob.generateSharedAccessSignature(policy, null);

            String total_link = blob.getUri().toString() + "?" + sas;

            logger.debug("path:: Total Link:: " + total_link);

            Model_Blob.cache_public_link.put(cache_blob_id, total_link);

            // Přesměruji na link
            return total_link;

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
        compilation.blob = Model_Blob.upload(body, "firmware.bin", get_path_for_bin());

        logger.trace("compile_program_procedure:: Body is ok - uploading to Azure was successful");
        compilation.status = CompilationStatus.SUCCESS;
        compilation.build_url = compilation_Result.build_url;
        compilation.firmware_build_id = compilation_Result.build_id;
        compilation.virtual_input_output = compilation_Result.interface_code;
        compilation.firmware_build_datetime = new Date();
        compilation.update();

        return compilation;
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override
    public void update() {

        logger.debug("update :: Update object Id: {}",  this.id);

        // Call notification about model update

        if(version != null) {
            new Thread(() -> {
                try {

                    EchoHandler.addToQueue(new WSM_Echo(Model_CProgram.class, version.get_c_program().getProjectId(), version.get_c_program_id()));
                } catch (_Base_Result_Exception e) {
                    // Nothing
                }
            }).start();
        }


        super.update();
    }

    @JsonIgnore @Transient @Override
    public boolean delete() {
        logger.internalServerError(new Exception("This object is not legitimate to remove."));
        return false;
    }


/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void notification_compilation_start() {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.NORMAL)
                    .setLevel(NotificationLevel.INFO)
                    .setText(new Notification_Text().setText("Server starts compilation of Version "))
                    .setObject(this)
                    .send(_BaseController.person());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore @Transient
    public void notification_compilation_success() {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.NORMAL)
                    .setLevel(NotificationLevel.SUCCESS)
                    .setText(new Notification_Text().setText("Compilation of Version "))
                    .setObject(this)
                    .setText(new Notification_Text().setText("was successful."))
                    .send(_BaseController.person());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore @Transient
    public void notification_compilation_unsuccessful_warn(String reason) {
        try {
            new Model_Notification()
                    .setImportance(NotificationImportance.NORMAL)
                    .setLevel(NotificationLevel.WARNING)
                    .setText(new Notification_Text().setText("Compilation of Version "))
                    .setObject(this)
                    .setText(new Notification_Text().setText("was unsuccessful, for reason:"))
                    .setText(new Notification_Text().setText(reason).setBoldText())
                    .send(_BaseController.person());
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore @Transient
    public void notification_compilation_unsuccessful_error(String result) {
        try {
            new Model_Notification()
                    .setImportance(NotificationImportance.NORMAL)
                    .setLevel(NotificationLevel.ERROR)
                    .setText(new Notification_Text().setText("Compilation of Version"))
                    .setObject(this)
                    .setText(new Notification_Text().setText("with critical Error:"))
                    .setText(new Notification_Text().setText(result).setBoldText())
                    .send(_BaseController.person());
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore @Transient
    public void notification_new_actualization_request_on_version() {

        new Thread(() -> {
            try {
                new Model_Notification()
                        .setImportance(NotificationImportance.NORMAL)
                        .setLevel(NotificationLevel.INFO)
                        .setText(new Notification_Text().setText("New actualization task was added to Task Queue on Version "))
                        .setObject(this)
                        .setText(new Notification_Text().setText(" from Program "))
                        .setObject(this.version.get_c_program())
                        .send(_BaseController.person());
            } catch (Exception e) {
                logger.internalServerError(e);
            }
        }).start();
    }

/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public String get_path() {

        return version.get_path();
    }

    @JsonIgnore @Transient
    public static String get_path_for_bin() throws Exception {

        CloudBlobContainer container = Server.blobClient.getContainerReference("bin-files");
        return container.getName() + "/" + UUID.randomUUID().toString();

    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public final static String virtual_input_output_docu = "dsafsdfsdf"; // TODO https://youtrack.byzance.cz/youtrack/issue/TYRION-304

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception { }
    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception { }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception { }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception { }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Compilation> find = new Finder<>(Model_Compilation.class);
}
