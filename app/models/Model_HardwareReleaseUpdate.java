package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.Expr;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.cache.CacheFinder;
import utilities.cache.Cached;
import utilities.cache.InjectCache;
import utilities.enums.EntityType;
import utilities.enums.FirmwareType;
import utilities.enums.HardwareUpdateState;
import utilities.enums.UpdateType;
import utilities.logger.Logger;
import utilities.model.NamedModel;
import utilities.model.TaggedModel;
import utilities.model.UnderProject;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.swagger.output.Swagger_Bootloader_Update_program;
import utilities.swagger.output.Swagger_C_Program_Update_program;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.*;

@Entity
@ApiModel(value = "HardwareReleaseUpdate", description = "Model of Hardware Release Updates")
@Table(name="HardwareReleaseUpdate")
public class Model_HardwareReleaseUpdate extends TaggedModel implements Permissible, UnderProject {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_HardwareReleaseUpdate.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore public UUID project_id;

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty
    @ApiModelProperty(required = true, readOnly = true)
    public Integer procedure_size_all() {
        try {
           return  Model_HardwareUpdate.find.query().where().eq("tracking_release_procedure_id.id", id).findCount();
        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public Integer state_pending() {
        try {
            return  Model_HardwareUpdate.find.query().where()
                    .eq("tracking_release_procedure_id", id)
                    .eq("state",  HardwareUpdateState.PENDING)
                    .findCount();
        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public Integer state_running() {
        try {
            return  Model_HardwareUpdate.find.query().where()
                    .eq("tracking_release_procedure_id", id)
                    .eq("state",  HardwareUpdateState.RUNNING)
                    .findCount();
        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public Integer state_complete() {
        try {
            return  Model_HardwareUpdate.find.query().where()
                    .eq("tracking_release_procedure_id", id)
                    .eq("state",  HardwareUpdateState.COMPLETE)
                    .findCount();
        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public Integer state_canceled() {
        try {
            return  Model_HardwareUpdate.find.query().where()
                    .eq("tracking_release_procedure_id", id)
                    .eq("state",  HardwareUpdateState.CANCELED)
                    .findCount();
        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public Integer state_faild() {
        try {
            return  Model_HardwareUpdate.find.query().where()
                    .eq("tracking_release_procedure_id", id)
                    .eq("state",  HardwareUpdateState.FAILED)
                    .findCount();
        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true, value = "Only if type_of_update constant is MANUALLY_RELEASE_MANAGER && firmware_type is FIRMWARE or BACKUP")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Swagger_C_Program_Update_program program(){
        try {
            return Model_HardwareUpdate.find.query().where().eq("tracking_release_procedure_id", id).isNotNull("c_program_version_for_update").setMaxRows(1).findOne().c_program_detail();
        } catch (Exception e){
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true, value = "Only of type_of_update constant is MANUALLY_RELEASE_MANAGER && firmware_type is BOOTLOADER")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Swagger_Bootloader_Update_program bootloader(){
        try {
            return Model_HardwareUpdate.find.query().where().eq("tracking_release_procedure_id", id).isNotNull("bootloader").setMaxRows(1).findOne().bootloader_detail();
        } catch (Exception e){
            return null;
        }
    }

    /* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public Model_Project getProject() {
        return Model_Project.find.query().nullable().where().eq("id", project_id).findOne();
    }

/* CRUD CLASSES --------------------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    /*
        @JsonIgnore
    public void notification_update_procedure_start() {
        try {

            logger.debug("notification_update_procedure_start :: operation ");

            Model_Notification notification = new Model_Notification();
            notification.setImportance(NotificationImportance.LOW);
            notification.setLevel(NotificationLevel.INFO);

            if (type_of_update == UpdateType.MANUALLY_BY_USER_INDIVIDUAL) {

                notification.setText(new Notification_Text().setText("Your manual update "))
                .setObject(this);

                if (updates.size() == 1) {

                    if (getUpdates().get(0).firmware_type == FirmwareType.FIRMWARE)
                        notification.setText(new Notification_Text().setText(" for Hardware "))
                                    .setObject(getUpdates().get(0).getHardware())
                                    .setText( new Notification_Text().setText(" from Code Editor with Program "))
                                    .setObject(getUpdates().get(0).c_program_version_for_update.get_c_program())
                                    .setText( new Notification_Text().setText(" version "))
                                    .setObject(getUpdates().get(0).c_program_version_for_update)
                                    .setText( new Notification_Text().setText("."));

                    else  if (getUpdates().get(0).firmware_type == FirmwareType.BOOTLOADER)
                        notification.setText(new Notification_Text().setText(" for Hardware "))
                                .setObject(getUpdates().get(0).getHardware())
                                .setText( new Notification_Text().setText(" Bootloader version " + getUpdates().get(0).getBootloader().version_identifier))
                                .setText( new Notification_Text().setText("."));

                } else {
                    notification.setText(new Notification_Text().setText(" for " + getUpdates().size()  + " devices from Code Editor"));
                }

                notification.setText(new Notification_Text().setText(" just begun. We will keep you informed about progress."));

            } else if (type_of_update == UpdateType.MANUALLY_BY_USER_BLOCKO_GROUP || type_of_update == UpdateType.MANUALLY_BY_USER_BLOCKO_GROUP_ON_TIME ) {


                notification.setText(new Notification_Text().setText("Update under Instance "))
                .setObject(Model_InstanceSnapshot.find.byId(getInstanceSnapshotId()).getInstance()); // TODO objekt notifikace

                if (getUpdates().size() == 1) {

                    notification.setText(new Notification_Text().setText(" with one device "));
                    notification.setObject(getUpdates().get(0).getHardware());

                } else {
                    notification.setText(new Notification_Text().setText(" with " + getUpdates().size()  + " devices from Blocko Snapshot "));
                }

                notification.setText(new Notification_Text().setText(" just begun. We will keep you informed about progress."));

            } else {

                throw new Exception( "Update procedure has not set the type_of_update.");
            }

            notification.send_under_project(get_project_id());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void notification_update_procedure_progress() {
        try {

            if (get_project_id() != null)
                new Thread(() -> {
                    logger.debug("notification_update_procedure_progress :: operation ");

                    if (state == Enum_Update_group_procedure_state.COMPLETE || state == Enum_Update_group_procedure_state.SUCCESSFULLY_COMPLETE || state == Enum_Update_group_procedure_state.COMPLETE_WITH_ERROR) {
                        logger.warn("notification_update_procedure_progress ::  called inappropriately (complete) !!!!");
                        return;
                    }

                    Model_Notification notification = new Model_Notification();

                    notification.setNotificationId(UUID.randomUUID())
                            .setImportance(NotificationImportance.LOW)
                            .setLevel(NotificationLevel.INFO)
                            .setText(new Notification_Text().setText("Update of Procedure "))
                            .setObject(this)
                            .setText(new Notification_Text().setText(" is done from " + procedure_size_complete() + "/" + procedure_size_all() + " ."))
                            .send_under_project(get_project_id());

                }).start();

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void notification_update_procedure_final_report() {
        try {

            logger.debug("notification_update_procedure_final_report :: operation ");

            if (get_project_id() != null)
                new Thread(() -> {
                    Model_Notification notification = new Model_Notification();

                    // Single Update
                    if (this.getUpdates().size() == 1 && (type_of_update == UpdateType.MANUALLY_BY_USER_INDIVIDUAL || type_of_update == UpdateType.MANUALLY_BY_USER_BLOCKO_GROUP)) {

                        logger.debug("notification_update_procedure_final_report :: Notification is for single update");

                        // Bootloader
                        if (this.getUpdates().get(0).firmware_type == FirmwareType.BOOTLOADER) {

                            logger.debug("notification_update_procedure_final_report :: Single Update bootloader");

                            Model_BootLoader.notification_bootloader_procedure_success_information_single(this.getUpdates().get(0));
                            return;
                        }

                        // Backup
                        if (this.getUpdates().get(0).firmware_type == FirmwareType.BACKUP) {

                            logger.debug("notification_update_procedure_final_report :: Single Update backup");
                            logger.debug("TOD o ");

                        }
                    }

                    notification.setImportance(NotificationImportance.LOW)
                            .setLevel(NotificationLevel.SUCCESS);


                    int successfully_updated = Model_HardwareUpdate.find.query().where()
                            .eq("actualization_procedure.id", id).where()
                            .eq("state", HardwareUpdateState.COMPLETE)
                            .findCount();

                    int waiting_for_device = Model_HardwareUpdate.find.query().where()
                            .eq("actualization_procedure.id", id).where()
                            .disjunction()
                            .eq("state", HardwareUpdateState.WAITING_FOR_DEVICE)
                            .eq("state", HardwareUpdateState.HOMER_SERVER_IS_OFFLINE)
                            .eq("state", HardwareUpdateState.INSTANCE_INACCESSIBLE)
                            .endJunction()
                            .findCount();

                    int error_device = Model_HardwareUpdate.find.query().where()
                            .eq("actualization_procedure.id", id).where()
                            .disjunction()
                            .eq("state", HardwareUpdateState.CRITICAL_ERROR)
                            .eq("state", HardwareUpdateState.NOT_UPDATED)
                            .endJunction()
                            .findCount();

                    notification.setText(new Notification_Text().setText("Update Procedure "))
                            .setObject(this)
                            .setText(new Notification_Text().setText(" is complete. \n"))
                            .setText(new Notification_Text().setText("Number of Total devices for update: " + getUpdates().size() + ". "))
                            .setText(new Notification_Text().setText("Successfully updated: " + successfully_updated + ". "));

                    if (waiting_for_device != 0) {
                        notification.setNewLine();
                        notification.setText(new Notification_Text().setText("-> Unavailable \"offline\" devices: " + waiting_for_device + ". "));
                    }

                    if (error_device != 0) {
                        notification.setNewLine();
                        notification.setText(new Notification_Text().setText("-> Unsuccessful updates " + error_device + ". "));
                    }

                    notification.send_under_project(get_project_id());

                }).start();

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public void notification_update_procedure_complete() {
        try {

            if (get_project_id() != null)
            new Thread(() -> {

                logger.warn("notification_update_procedure_complete :: operation ");

                Model_Notification notification =  new Model_Notification();

                notification.setImportance(NotificationImportance.NORMAL)
                            .setLevel(NotificationLevel.SUCCESS);

                // Individual update
                if (type_of_update == UpdateType.MANUALLY_BY_USER_INDIVIDUAL) {

                    if (procedure_size_all() == 1) {
                        return;
                    }

                    if (state == Enum_Update_group_procedure_state.SUCCESSFULLY_COMPLETE) {
                        notification.setText(new Notification_Text().setText("Update Procedure "))
                                .setObject(this)
                                .setText(new Notification_Text().setText(" is complete."));
                    } else {
                        notification.setText(new Notification_Text().setText("Update Procedure "))
                                .setObject(this)
                                .setText(new Notification_Text().setText(" is set as complete but something is wrong. Check Update procedure details."));
                    }

                    notification.send_under_project(get_project_id());
                    return;
                }

                // Možná tady zauvažovat o progressu?? TYRION-599

                if (state == Enum_Update_group_procedure_state.SUCCESSFULLY_COMPLETE) {

                    notification.setText( new Notification_Text().setText("Update Procedure "))
                            .setObject(this)
                            .setText(new Notification_Text().setText(" started at: "))
                            .setDate(this.created)
                            .setText(new Notification_Text().setText(" is done with no errors or other issues."));

                    notification.send_under_project(get_project_id());
                    return;
                }

                if (state == Enum_Update_group_procedure_state.COMPLETE) {

                    notification.setText( new Notification_Text().setText("Update Procedure "))
                                .setObject(this)
                                .setText(new Notification_Text().setText(" started at: "))
                                .setDate(this.created)
                                .setText(new Notification_Text().setText(" is done but something is not right. For more information, please visit the update procedure details."));

                    notification.send_under_project(get_project_id());
                }

            }).start();

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

     */
/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
    return EntityType.UPDATE_PROCEDURE;
}

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE);
    }

    /* CACHE ---------------------------------------------------------------------------------------------------------------*/

    /**
     * Specialní vyjímka - vždy vracíme Hardware podle full_id (číslo procesoru) kde
     * máme dominanci! Tuto metodu výlučně používá část systému obsluhující fyzický hardware.
     */
    public static Model_HardwareReleaseUpdate getByFullId(String fullId) {
        logger.trace("getByFullId: {}", fullId);
        UUID id = find.query().where().eq("full_id", fullId).eq("dominant_entity", true).select("id").findSingleAttribute();


        if (id == null){
            logger.debug("getByFullId: {} Database ID is null", fullId);
            return null;
        }

        logger.trace("getByFullId: {} Database ID {}", fullId, id.toString());
        return find.byId(id);
    }

    /* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_HardwareReleaseUpdate.class)
    public static CacheFinder<Model_HardwareReleaseUpdate> find = new CacheFinder<>(Model_HardwareReleaseUpdate.class);

}
