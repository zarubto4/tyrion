package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.BaseController;
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

    @ManyToOne public Model_Instance instance;
    @ManyToOne public Model_Version b_version;
    @OneToOne  public Model_Blob program;
    @JsonIgnore @OneToMany(fetch = FetchType.LAZY)  public List<Model_UpdateProcedure> procedures = new ArrayList<>();
    @JsonIgnore @ManyToMany(fetch = FetchType.LAZY) public List<Model_HardwareRegistration> hardware = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_version_object_id;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_Version get_b_program_version() {

        if (cache_version_object_id == null) {
            Model_Version version = Model_Version.find.query().where().eq("instances.id", id).select("id").findOne();
            cache_version_object_id = version.id;
        }

        return Model_Version.getById(cache_version_object_id);
    }

    public void deploy() {
        new Thread(() -> {

            // Step 1
            logger.debug("deploy - begin");
            if (this.instance.current_snapshot_id != null && !this.instance.current_snapshot_id.equals(this.id)) {
                logger.debug("deploy - stop previous running snapshot");
                Model_InstanceSnapshot previous = getById(this.instance.current_snapshot_id);
                if (previous != null) {
                    previous.stop();
                }
            }

            if (instance.server_online_state() != NetworkStatus.ONLINE) {
                logger.debug("deploy - server is offline, it is not possible to continue");
                return;
            }

            WS_Message_Instance_status status = instance.get_instance_status();

            WS_Message_Instance_status.InstanceStatus instanceStatus = status.get_status(instance.id);

            if (instanceStatus.error_code != null ) {
                logger.warn("deploy - instance {} is not set in Homer Server ", instance.id);
            }

            // Instance status
            if (!instanceStatus.status) {
                // Vytvořím Instanci
                WS_Message_Homer_Instance_add result_instance   = instance.server_main.add_instance(instance);
                if (!result_instance.status.equals("success")) {
                    logger.internalServerError(new Exception("Failed to add Instance. ErrorCode: " + result_instance.error_code + ". Error: " + result_instance.error));
                    return;
                }
            }

            // Step 2
            WS_Message_Instance_set_program result_step_2 = this.setProgram();
            if (!result_step_2.status.equals("success")) {
                logger.warn("deploy - instance {}, step 2 failed: {}", instance.id, result_step_2.error_code);
                return;
            }

            // Step 3
            WS_Message_Instance_set_hardware result_step_3 = this.setHardware();
            if (!result_step_3.status.equals("success")) {
                logger.warn("deploy - instance {}, step 3 failed: {}", instance.id, result_step_3.error_code);
                return;
            }

            // Step 4
            WS_Message_Instance_set_terminals result_step_4 = this.setTerminals();
            if (!result_step_4.status.equals("success")) {
                logger.warn("deploy - instance {}, step 4 failed: {}", instance.id, result_step_4.error_code);
                return;
            }

            Model_Instance.cache_status.put(this.instance.id, true);
            WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Instance.class, this.instance.id, true, this.instance.project_id());

            // Step 4
            // TODO this.create_actualization_hardware_request();


        }).start();
    }

    public void stop() { // TODO notifikace
        instance.stop();
    }

    @JsonIgnore
    public WS_Message_Instance_set_hardware setHardware() {
        try {

            // Seznam - který by na instanci měl běžet!
            List<UUID> hardware_ids_required_by_instance = getHardwareIds();

            // Přidat nový otisk hardwaru
            if (!hardware_ids_required_by_instance.isEmpty()) {
                return instance.set_device_to_instance(hardware_ids_required_by_instance);
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
                terminalIds.add(snapShot.m_project_id());
            }

            if (!terminalIds.isEmpty()) {
                return instance.setTerminals(terminalIds);
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

            JsonNode node = instance.write_with_confirmation(new WS_Message_Instance_set_program().make_request(this), 1000 * 6, 0, 2);

            return Json.fromJson(node, WS_Message_Instance_set_program.class);

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Instance_set_program();
        }
    }

    @JsonIgnore
    public List<UUID> getHardwareIds() { // TODO groups also
        List<UUID> ids = new ArrayList<>();
        this.hardware.forEach(hardware -> ids.add(hardware.id));
        return ids;
    }

    @JsonIgnore
    public Model_Product getProduct() {
        return this.instance.getProject().getProduct();

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
                    .send(BaseController.person());

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
                    .send_under_project(this.instance.project_id());

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
                    .send_under_project(this.instance.project_id());

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
                    .send_under_project(this.instance.project_id());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Create Permission is always JsonIgnore
    @JsonIgnore   public boolean create_permission()  {  return  false;  }
    @JsonIgnore   public boolean read_permission()    {  return  false;  }
    @JsonProperty public boolean update_permission()  {  return  false;  }
    @JsonProperty public boolean edit_permission()    {  return  false;  }
    @JsonProperty public boolean delete_permission()  {  return  false;  }

    public enum Permission { InstanceSnapshot_create, InstanceSnapshot_read, InstanceSnapshot_update, InstanceSnapshot_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(Model_InstanceSnapshot.class)
    public static Cache<UUID, Model_InstanceSnapshot> cache;

    public static Model_InstanceSnapshot getById(String id) {
        return getById(UUID.fromString(id));
    }

    public static Model_InstanceSnapshot getById(UUID id) {
        Model_InstanceSnapshot snapshot = cache.get(id);
        if (snapshot == null) {

            snapshot = find.byId(id);
            if (snapshot == null) return null;

            cache.put(id, snapshot);
        }

        return snapshot;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_InstanceSnapshot> find = new Finder<>(Model_InstanceSnapshot.class);
}
