package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.libs.Json;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.enums.NetworkStatus;
import utilities.enums.NotificationImportance;
import utilities.enums.NotificationLevel;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.notifications.helps_objects.Notification_Text;
import websocket.messages.homer_instance_with_tyrion.WS_Message_Instance_set_hardware;
import websocket.messages.homer_instance_with_tyrion.WS_Message_Instance_status;
import websocket.messages.homer_instance_with_tyrion.WS_Message_Instance_set_program;
import websocket.messages.homer_instance_with_tyrion.WS_Message_Instance_set_terminals;
import websocket.messages.homer_with_tyrion.WS_Message_Homer_Instance_add;
import websocket.messages.tyrion_with_becki.WS_Message_Online_Change_status;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "InstanceSnapshot", description = "Model of InstanceSnapshot")
@Table(name="InstanceSnapshot")
public class Model_InstanceSnapshot extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_InstanceSnapshot.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true, dataType = "integer", example = "1466163478925")
    public Date deployed;

    @ApiModelProperty(required = true, readOnly = true, dataType = "integer", example = "1466163478925")
    public Date stopped;

    @ManyToOne(fetch = FetchType.LAZY) public Model_Instance instance;
    @ManyToOne public Model_BProgramVersion b_program_version;
    @OneToOne  public Model_Blob program;
    @JsonIgnore @OneToMany(fetch = FetchType.LAZY)  public List<Model_UpdateProcedure> procedures = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_version_id;
    @JsonIgnore @Transient @Cached private UUID cache_instance_id;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_BProgramVersion get_b_program_version() {


        if (cache_version_id == null) {
            return Model_BProgramVersion.getById(get_b_program_version_id());
        }else {
            return Model_BProgramVersion.getById(cache_version_id);
        }
    }

    @JsonIgnore
    public UUID get_b_program_version_id() {

        if (cache_version_id == null) {
            Model_BProgramVersion version = Model_BProgramVersion.find.query().where().eq("instances.id", id).select("id").findOne();
            if (version == null) throw new Result_Error_NotFound(Model_BProgramVersion.class);
            cache_version_id = version.id;
        }

        return cache_version_id;
    }

    @JsonIgnore
    public Model_Instance get_instance() {
        if (cache_instance_id == null) {
            return Model_Instance.getById(get_instance_id());
        }else {
            return Model_Instance.getById(cache_instance_id);
        }
    }

    @JsonIgnore
    public UUID get_instance_id() {

        if (cache_instance_id == null) {
            Model_Instance instance = Model_Instance.find.query().where().eq("snapshots.id", id).select("id").findOne();
            if (instance == null) throw new Result_Error_NotFound(Model_Instance.class);
            cache_instance_id = instance.id;
        }

        return cache_instance_id;
    }

    public void deploy() {
        new Thread(() -> {

            // Step 1
            logger.debug("deploy - begin");
            if (this.get_instance().current_snapshot_id != null && !this.get_instance().current_snapshot_id.equals(this.id)) {
                logger.debug("deploy - stop previous running snapshot");
                Model_InstanceSnapshot previous = getById(this.get_instance().current_snapshot_id);
                if (previous != null) {
                    previous.stop();
                }
            }

            if (get_instance().server_online_state() != NetworkStatus.ONLINE) {
                logger.debug("deploy - server is offline, it is not possible to continue");
                return;
            }

            WS_Message_Instance_status status = get_instance().get_instance_status();

            WS_Message_Instance_status.InstanceStatus instanceStatus = status.get_status(get_instance_id());

            if (instanceStatus.error_code != null ) {
                logger.warn("deploy - instance {} is not set in Homer Server ", get_instance_id());
            }

            // Instance status
            if (!instanceStatus.status) {
                // Vytvořím Instanci
                WS_Message_Homer_Instance_add result_instance   = get_instance().server_main.add_instance(instance);
                if (!result_instance.status.equals("success")) {
                    logger.internalServerError(new Exception("Failed to add Instance. ErrorCode: " + result_instance.error_code + ". Error: " + result_instance.error));
                    return;
                }
            }

            // Step 2
            WS_Message_Instance_set_program result_step_2 = this.setProgram();
            if (!result_step_2.status.equals("success")) {
                logger.warn("deploy - instance {}, step 2 failed: {}", get_instance_id(), result_step_2.error_code);
                return;
            }

            // Step 3
            WS_Message_Instance_set_hardware result_step_3 = this.setHardware();
            if (!result_step_3.status.equals("success")) {
                logger.warn("deploy - instance {}, step 3 failed: {}", get_instance_id(), result_step_3.error_code);
                return;
            }

            // Step 4
            WS_Message_Instance_set_terminals result_step_4 = this.setTerminals();
            if (!result_step_4.status.equals("success")) {
                logger.warn("deploy - instance {}, step 4 failed: {}", get_instance_id(), result_step_4.error_code);
                return;
            }

            Model_Instance.cache_status.put(get_instance_id(), true);
            WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Instance.class, get_instance_id(), true, this.get_instance().project_id());

            // Step 4
            // TODO this.create_actualization_hardware_request();


        }).start();
    }

    public void stop() {
        check_update_permission();

        // TODO notifikace
        get_instance().stop();
    }

    @JsonIgnore
    public WS_Message_Instance_set_hardware setHardware() {
        try {

            // Seznam - který by na instanci měl běžet!
            List<UUID> hardware_ids_required_by_instance = getHardwareIds();

            // Přidat nový otisk hardwaru
            if (!hardware_ids_required_by_instance.isEmpty()) {
                return get_instance().set_device_to_instance(hardware_ids_required_by_instance);
            } else {
                WS_Message_Instance_set_hardware result = new WS_Message_Instance_set_hardware();
                result.status = "success";
                return result;
            }

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Instance_set_hardware();
        }
    }

    @JsonIgnore
    public WS_Message_Instance_set_terminals setTerminals() {
        try {

            List<UUID> terminalIds = new ArrayList<>();

            for (Model_MProjectProgramSnapShot snapShot : this.get_b_program_version().b_program_version_snapshots) {
                terminalIds.add(snapShot.grid_project_id());
            }

            if (!terminalIds.isEmpty()) {
                return get_instance().setTerminals(terminalIds);
            } else {
                WS_Message_Instance_set_terminals result = new WS_Message_Instance_set_terminals();
                result.status = "success";
                return result;
            }

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Instance_set_terminals();
        }
    }

    @JsonIgnore
    public WS_Message_Instance_set_program setProgram() {
        try {

            JsonNode node = get_instance().write_with_confirmation(new WS_Message_Instance_set_program().make_request(this), 1000 * 6, 0, 2);

            return Json.fromJson(node, WS_Message_Instance_set_program.class);

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Instance_set_program();
        }
    }

    @JsonIgnore
    public List<UUID> getHardwareIds() {
        // TODO - Vylouskat z Jsonu Snapshotu instance
        throw new Result_Error_NotSupportedException();
    }

    @JsonIgnore
    public List<UUID> getHardwareGroupseIds() {
        // TODO - Vylouskat z Jsonu Snapshotu instance
        throw new Result_Error_NotSupportedException();
    }

    @JsonIgnore
    public Model_Product getProduct() {
        return this.get_instance().get_project().getProduct();

    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    public void notification_instance_start_upload() {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.LOW)
                    .setLevel(NotificationLevel.INFO)
                    .setText( new Notification_Text().setText("Server started creating new Blocko Instance of Blocko Version "))
                    .setText( new Notification_Text().setText(this.get_b_program_version().get_b_program().name).setBoldText())
                    .setObject(this.get_b_program_version())
                    .setText( new Notification_Text().setText(" from Blocko program "))
                    .setObject(this.get_b_program_version().get_b_program())
                    .send(_BaseController.person());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void notification_instance_successful_upload() {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.LOW)
                    .setLevel(NotificationLevel.SUCCESS)
                    .setText(new Notification_Text().setText("Server successfully created the instance of Blocko Version "))
                    .setObject(this.get_b_program_version())
                    .setText(new Notification_Text().setText(" from Blocko program "))
                    .setObject(this.get_b_program_version().get_b_program())
                    .send_under_project(this.get_instance().project_id());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void notification_instance_unsuccessful_upload(String reason) {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.LOW)
                    .setLevel(NotificationLevel.WARNING)
                    .setText( new Notification_Text().setText("Server did not upload instance to cloud on Blocko Version "))
                    .setText( new Notification_Text().setText(this.get_b_program_version().name ).setBoldText())
                    .setText( new Notification_Text().setText(" from Blocko program "))
                    .setText( new Notification_Text().setText(this.get_b_program_version().get_b_program().name).setBoldText())
                    .setText( new Notification_Text().setText(" for reason: ").setBoldText() )
                    .setText( new Notification_Text().setText(reason + " ").setBoldText())
                    .setObject(this.get_b_program_version())
                    .setText( new Notification_Text().setText(" from Blocko program "))
                    .setObject(this.get_b_program_version().get_b_program())
                    .setText( new Notification_Text().setText(". Server will try to do that as soon as possible."))
                    .send_under_project(this.get_instance().project_id());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void notification_new_actualization_request_instance() {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.LOW)
                    .setLevel(NotificationLevel.INFO)
                    .setText( new Notification_Text().setText("New actualization task was added to Task Queue on Version "))
                    .setObject(this.get_b_program_version())
                    .send_under_project(this.get_instance().project_id());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.InstanceSnapshot_create.name())) return;
        get_instance().check_update_permission();
    }

    @JsonIgnore @Transient @Override public void check_read_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.InstanceSnapshot_read.name())) return;
        get_instance().check_update_permission();
    }

    @JsonIgnore @Transient @Override public void check_update_permission()  {
        if(_BaseController.person().has_permission(Permission.InstanceSnapshot_update.name())) return;
        get_instance().check_update_permission();
    }

    @JsonIgnore @Transient @Override public void  check_delete_permission() throws _Base_Result_Exception  {
        if(_BaseController.person().has_permission(Permission.InstanceSnapshot_delete.name())) return;
        get_instance().check_update_permission();
    }


    public enum Permission { InstanceSnapshot_create, InstanceSnapshot_read, InstanceSnapshot_update, InstanceSnapshot_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(Model_InstanceSnapshot.class)
    public static Cache<UUID, Model_InstanceSnapshot> cache;

    public static Model_InstanceSnapshot getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }

    public static Model_InstanceSnapshot getById(UUID id) throws _Base_Result_Exception {
        Model_InstanceSnapshot snapshot = cache.get(id);
        if (snapshot == null) {

            snapshot = find.byId(id);
            if (snapshot == null) throw new Result_Error_NotFound(Model_InstanceSnapshot.class);

            cache.put(id, snapshot);
        }

        snapshot.check_read_permission();
        return snapshot;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_InstanceSnapshot> find = new Finder<>(Model_InstanceSnapshot.class);
}
