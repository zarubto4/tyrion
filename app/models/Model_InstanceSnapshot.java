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
import utilities.model.TaggedModel;
import utilities.notifications.helps_objects.Becki_color;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.swagger.input.*;
import utilities.swagger.output.Swagger_InstanceSnapshot_JsonFile;
import utilities.swagger.output.Swagger_InstanceSnapshot_JsonFile_Interface;
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
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "InstanceSnapshot", description = "Model of InstanceSnapshot")
@Table(name="InstanceSnapshot")
public class Model_InstanceSnapshot extends TaggedModel {

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
    @JsonIgnore @OneToMany(fetch = FetchType.LAZY) public List<Model_UpdateProcedure> procedures = new ArrayList<>(); // Reálně zde je uložena jen jedna, pokud byla instance nasazena víckrát, vždy se tvoří nový aktualizační plán! Pak jich tu je víc než jedna

    /**
     * Here we collect everything additional settings for Snapshot.
     * For example permission to Snapshot MProgram Aplications.
     *
     *
     * SnapShotConfiguration Object!!!!
     */
    @JsonIgnore @Column(columnDefinition = "TEXT") public String json_additional_parameter;  // DB dokument - s možností rozšíření na cokoliv

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
        try {
            if (this.json_additional_parameter != null) {
                return baseFormFactory.formFromJsonWithValidation(Swagger_InstanceSnapShotConfiguration.class, Json.parse(this.json_additional_parameter));
            } else {
                if (this.get_instance().current_snapshot_id != null && this.get_instance().current_snapshot_id.equals(this.id)) {
                    Swagger_InstanceSnapShotConfiguration configuration = new Swagger_InstanceSnapShotConfiguration();

                    for (Model_BProgramVersionSnapGridProject grid_project_snapshots : b_program_version.grid_project_snapshots) {
                        Swagger_InstanceSnapShotConfigurationFile project_config = new Swagger_InstanceSnapShotConfigurationFile();
                        project_config.grid_project_id = grid_project_snapshots.grid_project.id;

                        for (Model_BProgramVersionSnapGridProjectProgram program : grid_project_snapshots.grid_programs) {
                            Swagger_InstanceSnapShotConfigurationProgram program_config = new Swagger_InstanceSnapShotConfigurationProgram();
                            program_config.grid_program_id = program.grid_program().id;
                            program_config.grid_program_version_id = program.grid_program_version.id;
                            program_config.snapshot_settings = GridAccess.PROJECT;
                            program_config.connection_token = program.id;

                            project_config.grid_programs.add(program_config);
                        }

                        configuration.grids_collections.add(project_config);
                    }

                    this.json_additional_parameter = Json.toJson(configuration).toString();
                    this.update();

                    return configuration;
                }
            }

            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            this.json_additional_parameter = null;
            this.update();
            return null;
        }
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(value = "only if snapshot is main")
    public Swagger_InstanceSnapshot_JsonFile program(){
        try {

            if (program != null) return  baseFormFactory.formFromJsonWithValidation(Swagger_InstanceSnapshot_JsonFile.class, Json.parse( program.get_fileRecord_from_Azure_inString()));

            return null;

        }catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(value = "only if snapshot is main")
    public List<Model_UpdateProcedure>  updates() throws _Base_Result_Exception {
        try {

            return getUpdateProcedure();

        } catch (Exception e) {
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
    public List<UUID> getHardwareIds() throws _Base_Result_Exception {
        List<UUID> list = new ArrayList<>();

        for (Swagger_InstanceSnapshot_JsonFile_Interface interface_hw : this.program().interfaces) {

            if(interface_hw.type.equals("hardware")) {
                list.add(interface_hw.target_id);
            }
        }

        return list;
    }

    @JsonIgnore
    public List<UUID> getHardwareGroupseIds() throws _Base_Result_Exception {
        List<UUID> list = new ArrayList<>();

        for (Swagger_InstanceSnapshot_JsonFile_Interface interface_hw : this.program().interfaces) {

            if(interface_hw.type.equals("group")) {
                list.add(interface_hw.target_id);
            }
        }

        return list;
    }

    @JsonIgnore
    public Model_Product getProduct() throws _Base_Result_Exception {
        return this.get_instance().getProject().getProduct();

    }


    @JsonIgnore
    public List<UUID> getUpdateProcedureIds() {

        if (cache().gets(Model_UpdateProcedure.class) == null) {
            cache().add(Model_UpdateProcedure.class, Model_UpdateProcedure.find.query().where().eq("instance.id", id).orderBy("created desc").select("id").findSingleAttributeList());
        }

        return cache().gets(Model_UpdateProcedure.class);
    }

    @JsonIgnore
    public List<Model_UpdateProcedure> getUpdateProcedure() {
        try {

            List<Model_UpdateProcedure> list = new ArrayList<>();

            for (UUID id : getUpdateProcedureIds() ) {
                list.add(Model_UpdateProcedure.getById(id));
            }

            return list;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }


    /* Actions --------------------------------------------------------------------------------------------------------*/

    public void deploy() {
        Model_Person person = its_person_operation() ? _BaseController.person() : null;
        new Thread(() -> {

            try {
                // Step 1
                logger.debug("deploy - begin - step 1");
                if (this.get_instance().current_snapshot_id != null && !this.get_instance().current_snapshot_id.equals(this.id)) {
                    logger.debug("deploy - stop previous running snapshot");
                    Model_InstanceSnapshot previous = getById(this.get_instance().current_snapshot_id);
                    if (previous != null) {
                        this.get_instance().current_snapshot_id = null;
                        this.update();
                    }
                }

                if (get_instance().getServer().online_state() != NetworkStatus.ONLINE) {
                    logger.debug("deploy - server is offline, it is not possible to continue");
                    get_instance().current_snapshot_id = this.id;
                    get_instance().update();

                    if(person != null) {
                        notification_instance_set_wait_for_server(person);
                    }
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
                    WS_Message_Homer_Instance_add result_instance = get_instance().server_main.add_instance(get_instance());
                    if (!result_instance.status.equals("success")) {
                        logger.internalServerError(new Exception("Failed to add Instance. ErrorCode: " + result_instance.error_code + ". Error: " + result_instance.error + result_instance.error_message));
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
                WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Instance.class, get_instance_id(), true, this.get_instance().getProjectId());

                // Only if there are hardware for update
                if(program().interfaces.size() > 0) {

                    // Step 5
                    logger.trace("deploy - instance {}, step 5 - Override all previous update procedures in this snapshot ", get_instance_id());

                    this.override_all_actualization_hardware_request();

                    logger.trace("deploy - instance {}, step 6 - Deploy Hardware ", get_instance_id());
                    Model_UpdateProcedure procedure = this.create_actualization_hardware_request();

                    if (procedure == null) {
                        logger.error("deploy - instance {}, step 6 - Error - Unsuccessful creating of Update Procedure");
                        return;
                    }

                    // Step 6
                    logger.trace("deploy - instance {}, step 7 - Start wtih Update ", get_instance_id());
                    procedure.execute_update_procedure();

                }


            }catch (Exception e) {
                logger.internalServerError(e);
            }

        }).start();
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

    @JsonIgnore
    public void override_all_actualization_hardware_request(){
        for(Model_UpdateProcedure procedure : getUpdateProcedure()) {

            if (procedure.state == Enum_Update_group_procedure_state.CANCELED) {
                continue;
            }

            procedure.date_of_finish = new Date();
            if(procedure.state !=  Enum_Update_group_procedure_state.COMPLETE && procedure.state != Enum_Update_group_procedure_state.SUCCESSFULLY_COMPLETE) {
                procedure.state = Enum_Update_group_procedure_state.CANCELED;
            }

            procedure.update();

            for(Model_HardwareUpdate update : procedure.getUpdates()) {
                if(update.state != HardwareUpdateState.COMPLETE) {
                    update.state = HardwareUpdateState.OBSOLETE;
                }

                update.update();
            }
        }
    }

    @JsonIgnore
    public Model_UpdateProcedure create_actualization_hardware_request(){
        try {

            Model_UpdateProcedure procedure = new Model_UpdateProcedure();
            procedure.type_of_update = UpdateType.MANUALLY_BY_USER_BLOCKO_GROUP;
            procedure.project_id = get_instance().getProjectId();
            procedure.instance = this;

            if (deployed != null) {
                // Planed
                procedure.date_of_planing = deployed;
            } else {
                // Immediately
                procedure.date_of_planing = new Date();
            }

            for (Swagger_InstanceSnapshot_JsonFile_Interface interface_hw : this.program().interfaces) {

                Model_CProgramVersion version = Model_CProgramVersion.getById(interface_hw.interface_id);

                //IF Group
                if(interface_hw.type.equals("group")) {

                    Model_HardwareGroup group = Model_HardwareGroup.getById(interface_hw.target_id);

                    List<UUID> uuid_ids = Model_Hardware.find.query().where().eq("hardware_groups.id", group.id).select("id").findIds();

                    for (UUID uuid_id : uuid_ids) {
                        Model_Hardware hardware = Model_Hardware.getById(uuid_id);

                        Model_HardwareUpdate plan = new Model_HardwareUpdate();
                        plan.hardware = hardware;
                        plan.firmware_type = FirmwareType.FIRMWARE;
                        plan.state = HardwareUpdateState.NOT_YET_STARTED;

                        if(!hardware.database_synchronize) {
                            plan.state = HardwareUpdateState.PROHIBITED_BY_CONFIG;
                        }

                        plan.c_program_version_for_update = version;
                        procedure.updates.add(plan);
                    }

                }

                //If Independent Hardware
                if(interface_hw.type.equals("hardware")) {

                    Model_Hardware hardware = Model_Hardware.getById(interface_hw.target_id);
                    Model_HardwareUpdate plan = new Model_HardwareUpdate();
                    plan.hardware = hardware;
                    plan.firmware_type = FirmwareType.FIRMWARE;
                    plan.state = HardwareUpdateState.NOT_YET_STARTED;

                    if(!hardware.database_synchronize) {
                        plan.state = HardwareUpdateState.PROHIBITED_BY_CONFIG;
                    }

                    plan.c_program_version_for_update = version;
                    procedure.updates.add(plan);

                }
            }

            // Cache Operation
            procedure.save();

            return procedure;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/


/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    /**
     * Saved Snap Shot as default, but server is offline, so it will be uploaded as soon as possible.
     */
    public void notification_instance_set_wait_for_server(Model_Person person) {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.LOW)
                    .setLevel(NotificationLevel.INFO)
                    .setText( new Notification_Text().setText("Snapshot is Set as default. But "))
                    .setObject(get_instance().getServer())
                    .setText( new Notification_Text().setText("is"))
                    .setText( new Notification_Text().setText("offline").setBoldText().setColor(Becki_color.byzance_red))
                    .setText( new Notification_Text().setText("."))
                    .setNewLine()
                    .setText( new Notification_Text().setText("Immediately after server reconnect, We will deploy it on server."))
                    .send(person);

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void notification_instance_start_upload(Model_Person person) {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.LOW)
                    .setLevel(NotificationLevel.INFO)
                    .setText( new Notification_Text().setText("Server started creating new Blocko Instance of Blocko Version "))
                    .setText( new Notification_Text().setText(this.get_b_program_version().get_b_program().name).setBoldText())
                    .setObject(this.get_b_program_version())
                    .setText( new Notification_Text().setText(" from Blocko program "))
                    .setObject(this.get_b_program_version().get_b_program())
                    .send(person);

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void notification_instance_successful_upload(Model_Person person) {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.LOW)
                    .setLevel(NotificationLevel.SUCCESS)
                    .setText(new Notification_Text().setText("Server successfully created the instance of Blocko Version "))
                    .setObject(this.get_b_program_version())
                    .setText(new Notification_Text().setText(" from Blocko program "))
                    .setObject(this.get_b_program_version().get_b_program())
                    .send(person);

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void notification_instance_unsuccessful_upload(String reason, Model_Person person) {
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
                    .send(person);

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
                    .send_under_project(this.get_instance().getProjectId());

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

                summary.grid_app_url += Model_HomerServer.getById(instance.getServer_id()).get_Grid_APP_URL() + instance.id + "/" + collection.grid_project_id + "/"  + program.connection_token;
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

                summary.grid_app_url += Model_HomerServer.getById(instance.getServer_id()).get_Grid_APP_URL() + instance.id + "/" + collection.grid_project_id + "/" + terminal.terminal_token;
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

                summary.grid_app_url += instance.server_main.server_url + instance.server_main.grid_port + "/" + instance.b_program_name() + "/#token";
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

/* JSON Override  Method -----------------------------------------------------------------------------------------*/

    @Override
    public void save() {
        super.save();

        this.instance.cache().add(this.getClass(), this.id);
        cache.put(this.id, this);
    }

    @Override
    public void update() {

        logger.debug("update - updating in database, id: {}",  this.id);

        super.update();
    }

    @Override
    public boolean delete() {

        logger.debug("delete - deleting from database, id: {} ", this.id);

        super.delete();

        if(get_instance().current_snapshot_id.equals(this.id)) {
            get_instance().current_snapshot_id = null;
            get_instance().update();
            get_instance().stop();
        }

        get_instance().cache().remove(this.getClass(), this.id);

        if (cache.containsKey(this.id)) {
            cache.remove(this.id);
        }

        return false;
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
        // Check Permission
        if(snapshot.its_person_operation()) {
            snapshot.check_read_permission();
        }

        return snapshot;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_InstanceSnapshot> find = new Finder<>(Model_InstanceSnapshot.class);
}
