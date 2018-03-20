package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.blob.CloudAppendBlob;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import utilities.Server;
import utilities.cache.Cached;
import utilities.enums.CompilationStatus;
import utilities.enums.NotificationImportance;
import utilities.enums.NotificationLevel;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.models_update_echo.EchoHandler;
import utilities.notifications.helps_objects.Notification_Text;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.*;

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

    @JsonIgnore
    public Model_Blob blob() {
        return Model_Blob.find.query().where().eq("version.id", version.id).eq("name", "firmware.bin").findOne();
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

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override
    public void update() {

        logger.debug("update :: Update object Id: {}",  this.id);

        // Call notification about model update
        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo( Model_CProgram.class, version.get_c_program().getProjectId(), version.get_c_program_id()));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();


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
                        .setObject(this.version.c_program)
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

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public final static String virtual_input_output_docu = "dsafsdfsdf"; // TODO https://youtrack.byzance.cz/youtrack/issue/TYRION-304

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception { }
    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception { }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception { }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception { }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Compilation getById(UUID id) {
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Compilation> find = new Finder<>(Model_Compilation.class);


}
