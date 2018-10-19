package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import controllers._BaseController;
import controllers._BaseFormFactory;
import exceptions.NotFoundException;
import exceptions.UnauthorizedException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.libs.Json;
import play.mvc.Http;
import utilities.Server;
import utilities.authentication.Authentication;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.*;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.model.UnderProject;
import utilities.models_update_echo.EchoHandler;
import utilities.notifications.helps_objects.Becki_color;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.swagger.input.*;
import utilities.swagger.output.Swagger_InstanceSnapshot_JsonFile;
import utilities.swagger.output.Swagger_InstanceSnapshot_JsonFile_Interface;
import utilities.swagger.output.Swagger_Mobile_Connection_Summary;
import utilities.swagger.output.Swagger_Short_Reference;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Message_Homer_Hardware_ID_UUID_Pair;
import websocket.messages.homer_instance_with_tyrion.WS_Message_Instance_set_hardware;
import websocket.messages.homer_instance_with_tyrion.WS_Message_Instance_status;
import websocket.messages.homer_instance_with_tyrion.WS_Message_Instance_set_program;
import websocket.messages.homer_instance_with_tyrion.WS_Message_Instance_set_terminals;
import websocket.messages.homer_with_tyrion.WS_Message_Homer_Instance_add;
import websocket.messages.tyrion_with_becki.WSM_Echo;
import websocket.messages.tyrion_with_becki.WS_Message_Online_Change_status;

import javax.persistence.*;
import javax.validation.Valid;
import java.util.*;
import java.util.List;

@Entity
@ApiModel(value = "InstanceSnapshot", description = "Model of InstanceSnapshot")
@Table(name="InstanceSnapshot")
public class Model_InstanceSnapshot extends TaggedModel implements Permissible, UnderProject {

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
    @JsonIgnore @OneToOne(fetch  = FetchType.LAZY) public Model_Blob program;
    @JsonIgnore @OneToMany(mappedBy = "instance", fetch = FetchType.LAZY) public List<Model_UpdateProcedure> procedures = new ArrayList<>(); // Reálně zde je uložena jen jedna, pokud byla instance nasazena víckrát, vždy se tvoří nový aktualizační plán! Pak jich tu je víc než jedna

    /**
     * Here we collect everything additional settings for Snapshot.
     * For example permission to Snapshot MProgram Applications.
     *
     *
     * SnapShotConfiguration Object!!!!
     */
    @JsonIgnore @Column(columnDefinition = "TEXT") public String json_additional_parameter;  // DB dokument - s možností rozšíření na cokoliv

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public Swagger_Short_Reference b_program_version() {
        try {
            return getBProgramVersion().ref();
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public Swagger_Short_Reference b_program() {
        try {
            return getBProgramVersion().getBProgram().ref();
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Model_BProgramVersionSnapGridProject> m_projects() {
        try {
            return getBProgramVersion().get_grid_project_snapshots();
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public Swagger_InstanceSnapShotConfiguration settings() {
        try {

            if (this.json_additional_parameter != null) {
                return baseFormFactory.formFromJsonWithValidation(Swagger_InstanceSnapShotConfiguration.class, Json.parse(this.json_additional_parameter));
            } else {

                Model_InstanceSnapshot snapshot = Model_InstanceSnapshot.find.query().where().eq("instance.id", instance.id).ne("id", this.id).isNotNull("json_additional_parameter").orderBy("deployed").setMaxRows(1).findOne();

                if (snapshot != null) {

                    if (snapshot.id.equals(this.id)) {
                        System.err.println("Aktuální settings tvořím nad naprosto stejným záznamem jako jem našel - něco je blblě: " + snapshot.name);
                    }

                    Swagger_InstanceSnapShotConfiguration configuration_new = new Swagger_InstanceSnapShotConfiguration();
                    Swagger_InstanceSnapShotConfiguration configuration_old = snapshot.settings();

                    for (Model_BProgramVersionSnapGridProject grid_project_snapshots : getBProgramVersion().get_grid_project_snapshots()) {

                        Swagger_InstanceSnapShotConfigurationFile previous_used_project = null;
                        for (Swagger_InstanceSnapShotConfigurationFile file : configuration_old.grids_collections) {
                            if (file.grid_project_id.equals(grid_project_snapshots.grid_project.id)){
                                previous_used_project = file;
                                break;
                            }
                        }

                        if (previous_used_project != null) {

                            Swagger_InstanceSnapShotConfigurationFile project_config = new Swagger_InstanceSnapShotConfigurationFile();
                            project_config.grid_project_id = grid_project_snapshots.grid_project.id;

                            for (Model_BProgramVersionSnapGridProjectProgram program : grid_project_snapshots.get_grid_programs()) {

                                Swagger_InstanceSnapShotConfigurationProgram previous_used_program = null;
                                for (Swagger_InstanceSnapShotConfigurationProgram old_program : previous_used_project.grid_programs) {
                                    if (old_program.grid_program_id.equals(program.grid_program().id)) {
                                        previous_used_program = old_program;
                                        break;
                                    }
                                }

                                if (previous_used_program != null) {
                                    project_config.grid_programs.add(previous_used_program);
                                } else  {
                                    Swagger_InstanceSnapShotConfigurationProgram program_config = new Swagger_InstanceSnapShotConfigurationProgram();
                                    program_config.grid_program_id = program.grid_program().id;
                                    program_config.grid_program_version_id = program.get_grid_program_version_id();
                                    program_config.snapshot_settings = GridAccess.PROJECT;
                                    program_config.connection_token = get_instance_id() + "/" + program_config.grid_program_id;

                                    project_config.grid_programs.add(program_config);
                                }
                            }

                            configuration_new.grids_collections.add(project_config);
                        } else {
                            Swagger_InstanceSnapShotConfigurationFile project_config = new Swagger_InstanceSnapShotConfigurationFile();
                            project_config.grid_project_id = grid_project_snapshots.grid_project.id;

                            for (Model_BProgramVersionSnapGridProjectProgram program : grid_project_snapshots.get_grid_programs()) {
                                Swagger_InstanceSnapShotConfigurationProgram program_config = new Swagger_InstanceSnapShotConfigurationProgram();
                                program_config.grid_program_id = program.get_grid_version_program().get_grid_program_id();
                                program_config.grid_program_version_id = program.get_grid_program_version_id();
                                program_config.snapshot_settings = GridAccess.PROJECT;
                                program_config.connection_token = get_instance_id() + "/" + program_config.grid_program_id;

                                project_config.grid_programs.add(program_config);
                            }

                            configuration_new.grids_collections.add(project_config);
                        }
                    }

                    configuration_new.api_keys.addAll(configuration_old.api_keys);
                    configuration_new.mesh_keys.addAll(configuration_old.mesh_keys);

                    this.json_additional_parameter = Json.toJson(configuration_new).toString();
                    this.update();

                    return configuration_new;

                } else {

                    Swagger_InstanceSnapShotConfiguration configuration = new Swagger_InstanceSnapShotConfiguration();

                    for (Model_BProgramVersionSnapGridProject grid_project_snapshots : getBProgramVersion().get_grid_project_snapshots()) {
                        Swagger_InstanceSnapShotConfigurationFile project_config = new Swagger_InstanceSnapShotConfigurationFile();
                        project_config.grid_project_id = grid_project_snapshots.grid_project.id;

                        for (Model_BProgramVersionSnapGridProjectProgram program : grid_project_snapshots.get_grid_programs()) {

                            Swagger_InstanceSnapShotConfigurationProgram program_config = new Swagger_InstanceSnapShotConfigurationProgram();
                            program_config.grid_program_id = program.get_grid_version_program().get_grid_program_id();
                            program_config.grid_program_version_id = program.get_grid_program_version_id();
                            program_config.snapshot_settings = GridAccess.PROJECT;
                            program_config.connection_token = get_instance_id() + "/" + program_config.grid_program_id;

                            project_config.grid_programs.add(program_config);
                        }

                        configuration.grids_collections.add(project_config);
                    }

                    this.json_additional_parameter = Json.toJson(configuration).toString();
                    this.update();

                    if (configuration.api_keys.isEmpty()) {
                        Swagger_InstanceSnapShotConfigurationApiKeys key = new Swagger_InstanceSnapShotConfigurationApiKeys();
                        key.token = UUID.randomUUID().toString();
                        key.description = "Default Instance Api Key";
                        key.created = new Date().getTime();
                        configuration.api_keys.add(key);
                    }

                    if (configuration.mesh_keys.isEmpty()) {
                        Swagger_InstanceSnapShotConfigurationApiKeys key = new Swagger_InstanceSnapShotConfigurationApiKeys();

                        StringBuilder sb = new StringBuilder(32);
                        for(int i = 0; i < 32; i++) {
                            sb.append("0123456789abcdef".charAt(new Random().nextInt("0123456789abcdef".length())));
                        }

                        key.token = sb.toString();

                        key.description = "Default Instance Mesh Network Key";
                        key.created = new Date().getTime();
                        configuration.mesh_keys.add(key);
                    }

                    return configuration;
                }
            }

        } catch (Exception e) {
            logger.internalServerError(e);
            this.json_additional_parameter = null;
            this.update();
            return null;
        }
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(value = "only if snapshot is main")
    public String program() {
        try {
            if (program != null) {
                return program.getPublicDownloadLink();
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }
        return null;
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(value = "only if snapshot is main")
    public List<Model_UpdateProcedure>  updates() {
        try {
            return getUpdateProcedure();
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public Model_BProgramVersion getBProgramVersion() {
        return isLoaded("b_program_version") ? b_program_version : Model_BProgramVersion.find.query().where().eq("instances.id", id).findOne();
    }

    @JsonIgnore @Transient
    public Model_Instance getInstance() {
        return isLoaded("instance") ? instance : Model_Instance.find.query().nullable().where().eq("snapshots.id", id).findOne();
    }

    @JsonIgnore @Transient
    public UUID get_instance_id() {

        if (idCache().gets(Model_Instance.class) == null) {
            idCache().add(Model_Instance.class, (UUID) Model_Instance.find.query().where().eq("snapshots.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_Instance.class);
    }

    @JsonIgnore @Transient
    public List<UUID> getHardwareIds() {
        List<UUID> list = new ArrayList<>();

        for (Swagger_InstanceSnapshot_JsonFile_Interface interface_hw : this.getProgram().interfaces) {

            if (interface_hw.type.equals("hardware")) {
                list.add(interface_hw.target_id);
            }
        }

        return list;
    }

    @JsonIgnore @Transient
    public List<UUID> getHardwareGroupseIds() {
        List<UUID> list = new ArrayList<>();

        for (Swagger_InstanceSnapshot_JsonFile_Interface interface_hw : this.getProgram().interfaces) {

            if (interface_hw.type.equals("group")) {
                list.add(interface_hw.target_id);
            }
        }

        return list;
    }

    @JsonIgnore @Transient
    public Model_Product getProduct() {
        return this.getInstance().getProject().getProduct();

    }

    @JsonIgnore @Transient
    public List<UUID> getUpdateProcedureIds() {

        if (idCache().gets(Model_UpdateProcedure.class) == null) {
            idCache().add(Model_UpdateProcedure.class, Model_UpdateProcedure.find.query().where().eq("instance.id", id).orderBy("created desc").select("id").findSingleAttributeList());
        }

        return idCache().gets(Model_UpdateProcedure.class) != null ?  idCache().gets(Model_UpdateProcedure.class) : new ArrayList<>();
    }

    @JsonIgnore @Transient
    public List<Model_UpdateProcedure> getUpdateProcedure() {
        try {

            List<Model_UpdateProcedure> list = new ArrayList<>();

            for (UUID id : getUpdateProcedureIds()) {
                list.add(Model_UpdateProcedure.find.byId(id));
            }

            return list;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore @Transient
    public Swagger_InstanceSnapshot_JsonFile getProgram() {
        try {
            if (program != null) {
                return baseFormFactory.formFromJsonWithValidation(Swagger_InstanceSnapshot_JsonFile.class, Json.parse(program.downloadString()));
            }
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore @Override
    public Model_Project getProject() {
        return this.getInstance().getProject();
    }

/* Actions --------------------------------------------------------------------------------------------------------*/

    public void deploy() {
        Model_Person person = its_person_operation() ? _BaseController.person() : null;
        new Thread(() -> {

            try {

                Model_Instance instance = getInstance();
                // Step 1
                logger.debug("deploy - begin - step 1");
                if (instance.current_snapshot_id != null && !instance.current_snapshot_id.equals(this.id)) {
                    logger.debug("deploy - stop previous running snapshot current_snapshot_id: {}", instance.current_snapshot_id);
                    instance.current_snapshot_id = null;
                    instance.update();
                }

                if (getInstance().getServer().online_state() != NetworkStatus.ONLINE) {
                    logger.debug("deploy - server is offline, it is not possible to continue");

                    instance.current_snapshot_id = this.id;
                    instance.update();

                    if (person != null) {
                        notification_instance_set_wait_for_server(person);
                    }
                    return;
                }

                if (instance.current_snapshot_id == null) {
                    instance.current_snapshot_id = this.id;
                    instance.update();
                }

                WS_Message_Instance_status status = instance.get_instance_status();

                WS_Message_Instance_status.InstanceStatus instanceStatus = status.get_status(get_instance_id());

                if (instanceStatus.error_code != null) {
                    logger.warn("deploy - instance {} is not set in Homer Server ", get_instance_id());
                }

                // Instance status
                if (!instanceStatus.status) {
                    // Vytvořím Instanci
                    WS_Message_Homer_Instance_add result_instance = instance.getServer().add_instance(instance);
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
                WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Instance.class, get_instance_id(), true, instance.getProjectId());

                // Only if there are hardware for update
                if (getProgram().interfaces.size() > 0) {

                    // Step 5
                    logger.trace("deploy - instance {}, step 5 - Override all previous update procedures in this snapshot ", get_instance_id());

                    this.override_all_actualization_hardware_request();

                    logger.trace("deploy - instance {}, step 6 - Deploy Hardware ", get_instance_id());
                    this.create_and_start_actualization_hardware_request();

                }

                WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Hardware.class, get_instance_id(), true, instance.getProjectId());

                logger.warn("Sending Update for Instance ID: {}", this.id);
                new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Instance.class, instance.getProject().id, get_instance_id()))).start();


            }catch (Exception e) {
                logger.internalServerError(e);
            }

        }).start();
    }

    @JsonIgnore @Transient
    public WS_Message_Instance_set_hardware setHardware() {
        try {

            // Seznam - který by na instanci měl běžet!
            List<UUID> hardware_ids_required_by_instance = getHardwareIds();
            List<UUID> group_hardware_ids_required_by_instance = Model_Hardware.find.query().where().in("hardware_groups.id", getHardwareGroupseIds()).select("id").findSingleAttributeList();

            hardware_ids_required_by_instance.addAll(group_hardware_ids_required_by_instance);

            List<WS_Message_Homer_Hardware_ID_UUID_Pair> hardwares = new ArrayList<>();


            for(UUID uuid: hardware_ids_required_by_instance) {
                try {

                    Model_Hardware hw = Model_Hardware.find.byId(uuid);

                    WS_Message_Homer_Hardware_ID_UUID_Pair pair = new WS_Message_Homer_Hardware_ID_UUID_Pair();
                    pair.full_id = hw.full_id;
                    pair.uuid = hw.id.toString(); // Must be string!

                    hardwares.add(pair);

                }catch (Exception e){
                    logger.internalServerError(e);
                }
            }

            // Přidat nový otisk hardwaru
            if (!hardwares.isEmpty()) {
                return getInstance().set_device_to_instance(hardwares);
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

    @JsonIgnore @Transient
    public WS_Message_Instance_set_terminals setTerminals() {
        try {

            // TODO Update terminals command to connected devices
            // List<UUID> terminalIds = new ArrayList<>();
            // return getInstance().setTerminals(terminalIds);

            WS_Message_Instance_set_terminals result = new WS_Message_Instance_set_terminals();
            result.status = "success";
            return result;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Instance_set_terminals();
        }
    }

    @JsonIgnore @Transient
    public WS_Message_Instance_set_program setProgram() {
        try {

            JsonNode node = getInstance().write_with_confirmation(new WS_Message_Instance_set_program().make_request(this), 1000 * 6, 0, 2);

            return baseFormFactory.formFromJsonWithValidation(WS_Message_Instance_set_program.class, node);

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Instance_set_program();
        }
    }

    @JsonIgnore @Transient
    public void override_all_actualization_hardware_request(){

        logger.debug("override_all_actualization_hardware_request Snapshot {} : start with override_all_actualization_hardware_request", this.id);
        logger.debug("override_all_actualization_hardware_request Snapshot {} : size of updates procedure for instance id {} is: {} ", this.id, this.get_instance_id(), getUpdateProcedure().size());
        for(Model_UpdateProcedure procedure : getUpdateProcedure()) {

            logger.debug("override_all_actualization_hardware_request Snapshot {} : FOR CYCLE Procedure ID: {}", this.id, procedure.id);

            if (procedure.state == Enum_Update_group_procedure_state.CANCELED) {
                logger.debug("override_all_actualization_hardware_request Snapshot {} : FOR CYCLE Procedure ID: {} procedure is CANCELED ", this.id, procedure.id);
                continue;
            }

            if(procedure.state ==  Enum_Update_group_procedure_state.COMPLETE || procedure.state == Enum_Update_group_procedure_state.SUCCESSFULLY_COMPLETE) {
                logger.debug("override_all_actualization_hardware_request Snapshot {} : FOR CYCLE Procedure ID: {} procedure is COMPLETE or  SUCCESSFULLY_COMPLETE", this.id, procedure.id);
                continue;
            }

            logger.debug("override_all_actualization_hardware_request Snapshot {} : FOR CYCLE Procedure ID: {} canceling this procedure ", this.id, procedure.id);

            procedure.update();

            for(Model_HardwareUpdate update : procedure.getUpdates()) {
                if(update.state != HardwareUpdateState.COMPLETE) {
                    update.state = HardwareUpdateState.OBSOLETE;
                }
                update.date_of_finish = new Date();
                update.update();
            }

            procedure.state = Enum_Update_group_procedure_state.CANCELED;
            procedure.date_of_finish = new Date();
            procedure.update();
        }
    }

    @JsonIgnore @Transient
    public void create_and_start_actualization_hardware_request(){
        try {

            logger.trace("create_actualization_hardware_request");
            Model_UpdateProcedure procedure = new Model_UpdateProcedure();
            procedure.type_of_update = UpdateType.MANUALLY_BY_USER_BLOCKO_GROUP;
            procedure.project_id = getInstance().getProjectId();
            procedure.instance = this;

            if (deployed != null) {
                // Planed
                procedure.date_of_planing = deployed;
            } else {
                // Immediately
                procedure.date_of_planing = new Date();
            }

            logger.trace("create_actualization_hardware_request:: Check Interface: Size " + this.getProgram().interfaces.size());

            if (this.getProgram().interfaces.size() == 0){
                logger.trace("create_actualization_hardware_request:: Interface list is EMPTY!");
            }

            for (Swagger_InstanceSnapshot_JsonFile_Interface interface_hw : this.getProgram().interfaces) {

                Model_CProgramVersion version = Model_CProgramVersion.find.byId(interface_hw.interface_id);

                //IF Group
                if(interface_hw.type.equals("group")) {

                    logger.trace("create_actualization_hardware_request:: interface_hw type: group ");
                    logger.trace("create_actualization_hardware_request:: interface_hw type: group:  " + interface_hw.target_id);
                    Model_HardwareGroup group = Model_HardwareGroup.find.byId(interface_hw.target_id);

                    List<UUID> uuid_ids = Model_Hardware.find.query().where().eq("hardware_groups.id", group.id).select("id").findIds();

                    for (UUID uuid_id : uuid_ids) {
                        Model_Hardware hardware = Model_Hardware.find.byId(uuid_id);
                        hardware.connected_instance_id = this.get_instance_id();
                        hardware.update();

                        Model_HardwareUpdate plan = new Model_HardwareUpdate();
                        plan.hardware = hardware;
                        plan.firmware_type = FirmwareType.FIRMWARE;
                        plan.state = HardwareUpdateState.NOT_YET_STARTED;

                        if(!hardware.database_synchronize) {
                            plan.state = HardwareUpdateState.PROHIBITED_BY_CONFIG;
                        }

                        plan.c_program_version_for_update = version;
                        plan.actualization_procedure = procedure;


                        logger.trace("create_actualization_hardware_request:: interface_hw type: group plan created:  " + plan.id);
                        procedure.updates.add(plan);
                    }

                }

                //If Independent Hardware
                if (interface_hw.type.equals("hardware")) {

                    logger.trace("create_actualization_hardware_request:: interface_hw type: hardware:  " + interface_hw.target_id);

                    Model_Hardware hardware = Model_Hardware.find.byId(interface_hw.target_id);

                    hardware.connected_instance_id = this.get_instance_id();
                    hardware.update();

                    Model_HardwareUpdate plan = new Model_HardwareUpdate();
                    plan.hardware = hardware;
                    plan.firmware_type = FirmwareType.FIRMWARE;
                    plan.state = HardwareUpdateState.NOT_YET_STARTED;

                    if(!hardware.database_synchronize) {
                        plan.state = HardwareUpdateState.PROHIBITED_BY_CONFIG;
                    }

                    plan.c_program_version_for_update = version;
                    plan.actualization_procedure = procedure;


                    logger.trace("create_actualization_hardware_request:: interface_hw type: hardware plan created:  " + plan.id);
                    procedure.updates.add(plan);

                }
            }

            this.getUpdateProcedureIds();

            // When Save - Do it immediately!
            procedure.save();

            // Add to cache
            idCache().add(Model_UpdateProcedure.class, procedure.id);

        } catch (Exception e) {
            logger.internalServerError(e);
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
                    .setObject(getInstance().getServer())
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
                    .setText( new Notification_Text().setText(this.getBProgramVersion().getBProgram().name).setBoldText())
                    .setObject(this.getBProgramVersion())
                    .setText( new Notification_Text().setText(" from Blocko program "))
                    .setObject(this.getBProgramVersion().getBProgram())
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
                    .setObject(this.getBProgramVersion())
                    .setText(new Notification_Text().setText(" from Blocko program "))
                    .setObject(this.getBProgramVersion().getBProgram())
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
                    .setText( new Notification_Text().setText(this.getBProgramVersion().name ).setBoldText())
                    .setText( new Notification_Text().setText(" from Blocko program "))
                    .setText( new Notification_Text().setText(this.getBProgramVersion().getBProgram().name).setBoldText())
                    .setText( new Notification_Text().setText(" for reason: ").setBoldText() )
                    .setText( new Notification_Text().setText(reason + " ").setBoldText())
                    .setObject(this.getBProgramVersion())
                    .setText( new Notification_Text().setText(" from Blocko program "))
                    .setObject(this.getBProgramVersion().getBProgram())
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
                    .setObject(this.getBProgramVersion())
                    .send_under_project(this.getInstance().getProjectId());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* Helper Class --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public Swagger_Mobile_Connection_Summary get_connection_summary(UUID grid_program_id,  Http.Context context) {

        // OBJEKT který se variabilně naplní a vrátí - ITS EMPTY!!!!
        Swagger_Mobile_Connection_Summary summary = new Swagger_Mobile_Connection_Summary();

        Swagger_InstanceSnapShotConfiguration settings = settings();
        Swagger_InstanceSnapShotConfigurationFile collection = null;
        Swagger_InstanceSnapShotConfigurationProgram program = null;

        for (Swagger_InstanceSnapShotConfigurationFile grids_collection : settings.grids_collections) {

            for (Swagger_InstanceSnapShotConfigurationProgram grids_program : grids_collection.grid_programs) {

                if (grids_program.grid_program_id.equals(grid_program_id) || grids_program.grid_program_version_id.equals(grid_program_id)) {

                    collection = grids_collection;
                    program = grids_program;
                    break;
                }
            }
        }

        if (collection == null) {
            logger.error("SnapShotConfigurationFile is missing return null");
            throw new NotFoundException(Swagger_InstanceSnapShotConfigurationFile.class);
        }

        // Nastavení SSL
        if (Server.mode == ServerMode.DEVELOPER) {
            summary.grid_app_url = "ws://";
        } else {
            summary.grid_app_url = "wss://";
        }

        summary.grid_app_url += Model_HomerServer.find.byId(getInstance().getServer_id()).get_Grid_APP_URL();
        summary.grid_app_url += get_instance_id() + "/" ;

        switch (program.snapshot_settings) {

            case PUBLIC: {

                Model_GridProgramVersion version = Model_GridProgramVersion.find.byId(program.grid_program_version_id);

                summary.grid_app_url += collection.grid_project_id + "/"  + program.grid_program_id + "/" + UUID.randomUUID();
                summary.grid_program = version.file.downloadString();
                summary.grid_project_id = collection.grid_project_id;
                summary.grid_program_id = program.grid_program_id;
                summary.grid_program_version_id = program.grid_program_version_id;
                summary.instance_id = getInstance().id;

                JsonNode jsonNode = Json.parse(summary.grid_program);
                JsonNode m_code = Json.parse(jsonNode.get("m_code").asText().replace("\\\"", "\""));

                summary.source_code_list = version_separator(m_code);

                return summary;
            }

            case PROJECT: {

                // Check Token
                String token = new Authentication().getUsername(context); // TODO ugly
                if (token == null) {
                    throw new UnauthorizedException();
                }

                // Check Person By Token (who send request)
                Model_Person person = _BaseController.person();

                //Chekc Permission
                // TODO check_read_permission();

                Model_GridTerminal terminal = new Model_GridTerminal();
                terminal.device_name = "Unknown";
                terminal.device_type = "Unknown";

                if ( Http.Context.current().request().headers().get("User-Agent")[0] != null) terminal.user_agent =  Http.Context.current().request().headers().get("User-Agent")[0];
                else  terminal.user_agent = "Unknown browser";

                terminal.person = person;
                terminal.save();

                summary.grid_app_url += collection.grid_project_id + "/"  + program.grid_program_id + "/" + terminal.terminal_token;
                summary.grid_program = Model_GridProgramVersion.find.byId(program.grid_program_version_id).file.downloadString();
                summary.grid_project_id = collection.grid_project_id;
                summary.grid_program_id = program.grid_program_id;
                summary.grid_program_version_id = program.grid_program_version_id;
                summary.instance_id = getInstance().id;

                JsonNode jsonNode = Json.parse(summary.grid_program);
                JsonNode m_code = Json.parse(jsonNode.get("m_code").asText().replace("\\\"", "\""));
                summary.source_code_list = version_separator(m_code);

                return summary;
            }

            /* TODO doimplementovat v budoucnu
            case only_for_project_members_and_imitated_emails: {

                summary.grid_app_url += instance.server_main.server_url + instance.server_main.grid_port + "/" + instance.b_program_name() + "/#token";
                summary.grid_program = Model_MProgram.get_m_code(grid_program_version);
                summary.instance_id = getInstance().id;

                return summary;
            }
            */
        }

        logger.error("Invalid settings on Instance Grid App permissions");
        throw new UnauthorizedException();
    }

    /**
     * Modelové schéma určené k parsování m_programu která přišla z Becki ----------------------------------------------
     */
    private List<Swagger_GridWidgetVersion_GridApp_source> version_separator(JsonNode m_code) {

        try {

            // List for returning
            List<Swagger_GridWidgetVersion_GridApp_source> list = new ArrayList<>();

            // Create object
            M_Program_Parser program_parser = baseFormFactory.formFromJsonWithValidation(M_Program_Parser.class, m_code);

            // Loking for objects
            for (Widget_Parser widget_parser : program_parser.screens.main.get(0).widgets) {

                Swagger_GridWidgetVersion_GridApp_source detail = new Swagger_GridWidgetVersion_GridApp_source();
                detail.id         = widget_parser.type.version_id;
                detail.logic_json = Model_WidgetVersion.find.byId(widget_parser.type.version_id).logic_json;

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

        @Valid public UUID version_id;
    }

/* JSON Override  Method -----------------------------------------------------------------------------------------*/

    @Override
    public void save() {

        super.save();

        this.idCache().add(Model_Instance.class, this.get_instance_id());

        // Add to Cache
        if(getInstance() != null) {
            getInstance().getSnapShotsIds();
            getInstance().idCache().add(this.getClass(), id);
            getInstance().sort_Model_InstanceSnapshot_ids();
        }

        new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Instance.class, getInstance().getProjectId(), get_instance_id()))).start();

    }

    @Override
    public boolean delete() {

        logger.debug("delete - deleting from database, id: {} ", this.id);


        if(getInstance().current_snapshot_id != null && getInstance().current_snapshot_id.equals(this.id)) {
            getInstance().stop();
        }

        getInstance().idCache().remove(this.getClass(), this.id);

        return super.delete();
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public String get_path() {
        return getInstance().get_path() + "/snapshots/" + this.id;
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.INSTANCE_SNAPSHOT;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE, Action.DEPLOY);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_InstanceSnapshot.class)
    public static CacheFinder<Model_InstanceSnapshot> find = new CacheFinder<>(Model_InstanceSnapshot.class);
}
