package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import controllers._BaseController;
import controllers._BaseFormFactory;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.data.validation.Constraints;
import play.libs.Json;
import play.mvc.Http;
import utilities.Server;
import utilities.authentication.Authentication;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.enums.*;
import utilities.errors.Exceptions.*;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.swagger.input.Swagger_GridWidgetVersion_GridApp_source;
import utilities.swagger.input.Swagger_InstanceSnapShotConfiguration;
import utilities.swagger.input.Swagger_InstanceSnapShotConfigurationFile;
import utilities.swagger.input.Swagger_InstanceSnapShotConfigurationProgram;
import utilities.swagger.output.Swagger_Mobile_Connection_Summary;
import utilities.swagger.output.Swagger_Short_Reference;
import websocket.messages.homer_instance_with_tyrion.WS_Message_Instance_set_hardware;
import websocket.messages.homer_instance_with_tyrion.WS_Message_Instance_status;
import websocket.messages.homer_instance_with_tyrion.WS_Message_Instance_set_program;
import websocket.messages.homer_instance_with_tyrion.WS_Message_Instance_set_terminals;
import websocket.messages.homer_with_tyrion.WS_Message_Homer_Instance_add;
import websocket.messages.tyrion_with_becki.WS_Message_Online_Change_status;

import javax.persistence.*;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "InstanceSnapshot", description = "Model of InstanceSnapshot")
@Table(name="InstanceSnapshot")
public class Model_InstanceSnapshot extends BaseModel {

    /**
     * _BaseFormFactory
     */
    public static _BaseFormFactory baseFormFactory; // Its Required to set this in Server.class Component

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_InstanceSnapshot.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true, dataType = "integer", example = "1466163478925")
    public Date deployed;

    @ApiModelProperty(required = true, readOnly = true, dataType = "integer", example = "1466163478925")
    public Date stopped;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Instance instance;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_BProgramVersion b_program_version;
    @JsonIgnore @OneToOne(fetch = FetchType.LAZY)  public Model_Blob program;
    @JsonIgnore @OneToMany(fetch = FetchType.LAZY) public List<Model_UpdateProcedure> procedures = new ArrayList<>();

    /**
     * Here we collect everything additional settings for Snapshot.
     * For example permission to Snapshot MProgram Aplications.
     *
     *
     * SnapShotConfiguration Object!!!!
     */
    @JsonIgnore @Column(columnDefinition = "TEXT") public String json_additional_parameter;  // DB dokument - smožností rozšíření na cokoliv

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_version_id;
    @JsonIgnore @Transient @Cached private UUID cache_instance_id;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public Swagger_Short_Reference b_program_version(){
        try {

            Model_BProgramVersion b_program_version = get_b_program_version();
            return new Swagger_Short_Reference(b_program_version.id, b_program_version.name, b_program_version.description);

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public Swagger_Short_Reference b_program(){
        try {

            Model_BProgramVersion b_program_version = get_b_program_version();
            return new Swagger_Short_Reference(b_program_version.get_b_program().id, b_program_version.get_b_program().name, b_program_version.get_b_program().description);

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Model_BProgramVersionSnapGridProject> m_projects(){
        try {

            Model_BProgramVersion b_program_version = get_b_program_version();
            return b_program_version.grid_project_snapshots;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public Swagger_InstanceSnapShotConfiguration settings(){
        return baseFormFactory.formFromJsonWithValidation(Swagger_InstanceSnapShotConfiguration.class, Json.parse(this.json_additional_parameter));
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(value = "only if snapshot is main")
    public String program(){
        try {

            if (Model_Instance.find.query().where().eq("current_snapshot_id", id).findCount() > 0) {
                if(program != null) program.get_fileRecord_from_Azure_inString();
            }

            return null;

        }catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }



/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_BProgramVersion get_b_program_version() throws _Base_Result_Exception {

        if (cache_version_id == null) {
            return Model_BProgramVersion.getById(get_b_program_version_id());
        }else {
            return Model_BProgramVersion.getById(cache_version_id);
        }
    }

    @JsonIgnore
    public UUID get_b_program_version_id() throws _Base_Result_Exception {

        if (cache_version_id == null) {
            Model_BProgramVersion version = Model_BProgramVersion.find.query().where().eq("instances.id", id).select("id").findOne();
            if (version == null) throw new Result_Error_NotFound(Model_BProgramVersion.class);
            cache_version_id = version.id;
        }

        return cache_version_id;
    }

    @JsonIgnore
    public Model_Instance get_instance() throws _Base_Result_Exception  {
        if (cache_instance_id == null) {
            return Model_Instance.getById(get_instance_id());
        }else {
            return Model_Instance.getById(cache_instance_id);
        }
    }

    @JsonIgnore
    public UUID get_instance_id() throws _Base_Result_Exception {

        if (cache_instance_id == null) {
            Model_Instance instance = Model_Instance.find.query().where().eq("snapshots.id", id).select("id").findOne();
            if (instance == null) throw new Result_Error_NotFound(Model_Instance.class);
            cache_instance_id = instance.id;
        }

        return cache_instance_id;
    }


    @JsonIgnore
    public List<String> getHardwareFullIds() throws _Base_Result_Exception {
        // TODO - Vylouskat z Jsonu Snapshotu instance
        throw new Result_Error_NotSupportedException();
    }

    @JsonIgnore
    public List<UUID> getHardwareGroupseIds() throws _Base_Result_Exception {
        // TODO - Vylouskat z Jsonu Snapshotu instance
        throw new Result_Error_NotSupportedException();
    }

    @JsonIgnore
    public Model_Product getProduct() throws _Base_Result_Exception {
        return this.get_instance().get_project().getProduct();

    }


/* Actions --------------------------------------------------------------------------------------------------------*/

    public void deploy() {
        new Thread(() -> {

            try {
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

                if (instanceStatus.error_code != null) {
                    logger.warn("deploy - instance {} is not set in Homer Server ", get_instance_id());
                }

                // Instance status
                if (!instanceStatus.status) {
                    // Vytvořím Instanci
                    WS_Message_Homer_Instance_add result_instance = get_instance().server_main.add_instance(instance);
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
                WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Instance.class, get_instance_id(), true, this.get_instance().get_project_id());

                // Step 4
                // TODO this.create_actualization_hardware_request();

            }catch (Exception e) {
                logger.internalServerError(e);
            }

        }).start();
    }

    public void stop() throws _Base_Result_Exception {
        check_update_permission();

        // TODO notifikace
        get_instance().stop();
    }

    @JsonIgnore
    public WS_Message_Instance_set_hardware setHardware() {
        try {

            // Seznam - který by na instanci měl běžet!
            List<String> hardware_ids_required_by_instance = getHardwareFullIds();

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

            // TODO Update terminals command to connected devices
            // List<UUID> terminalIds = new ArrayList<>();
            // return get_instance().setTerminals(terminalIds);

            WS_Message_Instance_set_terminals result = new WS_Message_Instance_set_terminals();
            result.status = "success";
            return result;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Instance_set_terminals();
        }
    }

    @JsonIgnore
    public WS_Message_Instance_set_program setProgram() {
        try {

            JsonNode node = get_instance().write_with_confirmation(new WS_Message_Instance_set_program().make_request(this), 1000 * 6, 0, 2);

            return baseFormFactory.formFromJsonWithValidation(WS_Message_Instance_set_program.class, node);

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Instance_set_program();
        }
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
                    .send_under_project(this.get_instance().get_project_id());

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
                    .send_under_project(this.get_instance().get_project_id());

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
                    .send_under_project(this.get_instance().get_project_id());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/


/* Helper Class --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Swagger_Mobile_Connection_Summary get_connection_summary(UUID grid_program_version_id,  Http.Context context) throws _Base_Result_Exception {

        // OBJEKT který se variabilně naplní a vrátí - ITS EMPTY!!!!
        Swagger_Mobile_Connection_Summary summary = new Swagger_Mobile_Connection_Summary();

        Swagger_InstanceSnapShotConfiguration settings = settings();
        Swagger_InstanceSnapShotConfigurationFile collection = null;
        Swagger_InstanceSnapShotConfigurationProgram program = null;

        for(Swagger_InstanceSnapShotConfigurationFile grids_collection : settings.grids_collections){
            for(Swagger_InstanceSnapShotConfigurationProgram grids_program : collection.grid_programs){
                if(grids_program.grid_program_version_id == grid_program_version_id){
                    collection = grids_collection;
                    program = grids_program;
                    break;
                }
            }
        }

        if(collection == null){
            logger.error("SnapShotConfigurationFile is missing return null");
            throw new Result_Error_NotFound(Swagger_InstanceSnapShotConfigurationFile.class);
        }

        if(program == null){
            logger.error("SnapShotConfigurationFileProgram is missing return null");
            throw new Result_Error_NotFound(Swagger_InstanceSnapShotConfigurationProgram.class);
        }

        // Nastavení SSL
        if (Server.mode == ServerMode.DEVELOPER) {
            summary.grid_app_url = "ws://";
        } else {
            summary.grid_app_url = "wss://";
        }

        switch (program.snapshot_settings) {

            case PUBLIC: {

                summary.grid_app_url += Model_HomerServer.getById(instance.server_id()).get_Grid_APP_URL() + instance.id + "/" + collection.grid_project_id + "/"  + program.connection_token;
                summary.grid_program = Model_GridProgramVersion.getById(program.grid_program_version_id).file.get_fileRecord_from_Azure_inString();
                summary.grid_project_id = collection.grid_project_id;
                summary.grid_program_id = program.grid_program_id;
                summary.grid_program_version_id = program.grid_program_version_id;
                summary.instance_id = get_instance().id;
                summary.source_code_list = version_separator(Json.parse(Model_GridProgramVersion.getById(program.grid_program_version_id).file.get_fileRecord_from_Azure_inString()));
                return summary;
            }

            case PROJECT: {

                // Check Token
                String token = new Authentication().getUsername(context);
                if (token == null) throw new Result_Error_PermissionDenied();

                // Check Person By Token (who send request)
                Model_Person person = _BaseController.person();
                if (person == null) throw new Result_Error_PermissionDenied();

                //Chekc Permission
                check_read_permission();

                Model_GridTerminal terminal = new Model_GridTerminal();
                terminal.device_name = "Unknown";
                terminal.device_type = "Unknown";

                if ( Http.Context.current().request().headers().get("User-Agent")[0] != null) terminal.user_agent =  Http.Context.current().request().headers().get("User-Agent")[0];
                else  terminal.user_agent = "Unknown browser";

                terminal.person = person;
                terminal.save();

                summary.grid_app_url += Model_HomerServer.getById(instance.server_id()).get_Grid_APP_URL() + instance.id + "/" + collection.grid_project_id + "/" + terminal.terminal_token;
                summary.grid_program = Model_GridProgramVersion.getById(program.grid_program_version_id).file.get_fileRecord_from_Azure_inString();
                summary.grid_project_id = collection.grid_project_id;
                summary.grid_program_id = program.grid_program_id;
                summary.grid_program_version_id = program.grid_program_version_id;
                summary.instance_id = get_instance().id;
                summary.source_code_list = version_separator(Json.parse(Model_GridProgramVersion.getById(program.grid_program_version_id).file.get_fileRecord_from_Azure_inString()));
                return summary;
            }

            /* TODO doimplementovat v budoucnu
            case only_for_project_members_and_imitated_emails: {

                summary.grid_app_url += instance.cloud_homer_server.server_url + instance.cloud_homer_server.grid_port + "/" + instance.b_program_name() + "/#token";
                summary.grid_program = Model_MProgram.get_m_code(grid_program_version);
                summary.instance_id = get_instance().id;

                return summary;
            }
            */
        }

        logger.error("Invalid settings on Instance Grid App permissions");
        throw new Result_Error_Unauthorized();
    }


    /**
     * Modelové schéma určené k parsování m_programu která přišla z Becki ----------------------------------------------
     */
    private List<Swagger_GridWidgetVersion_GridApp_source> version_separator(JsonNode m_program) {

        try {

            // List for returning
            List<Swagger_GridWidgetVersion_GridApp_source> list = new ArrayList<>();

            // Create object
            M_Program_Parser program_parser = baseFormFactory.formFromJsonWithValidation(M_Program_Parser.class, m_program);

            // Loking for objects
            for (Widget_Parser widget_parser : program_parser.screens.main.get(0).widgets) {

                Swagger_GridWidgetVersion_GridApp_source detail = new Swagger_GridWidgetVersion_GridApp_source();
                detail.id          = widget_parser.type.version_id;
                detail.logic_json = Model_WidgetVersion.getById(widget_parser.type.version_id).logic_json;

                list.add(detail);
            }
            return list;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    public static class M_Program_Parser{

        public M_Program_Parser() {}

        @Valid
        public Screen_Parser screens;
    }

    public static class Screen_Parser{

        public Screen_Parser() {}

        @Valid public List<Main_Parser> main = new ArrayList<>();
    }

    public static class Main_Parser{

        public Main_Parser() {}

        @Valid public List<Widget_Parser> widgets  = new ArrayList<>();
    }

    public static class Widget_Parser{

        public Widget_Parser() {}

        @Valid  public Type_Parser type;
    }

    public static class Type_Parser{

        public Type_Parser() {}

        @Valid public String version_id;
    }


/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public String get_path() {
        // If its new Snapshot
        if(instance != null) {
            return instance.get_path() + "/snapshots/" + this.id;
        }else {
            return get_instance().get_path() + "/snapshots/" + this.id;
        }
    }

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
