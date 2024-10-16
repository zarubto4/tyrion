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
import utilities.notifications.helps_objects.Becki_color;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.swagger.input.*;
import utilities.swagger.output.Swagger_InstanceSnapshot_JsonFile;
import utilities.swagger.output.Swagger_InstanceSnapshot_JsonFile_Interface;
import utilities.swagger.output.Swagger_Mobile_Connection_Summary;
import utilities.swagger.output.Swagger_Short_Reference;

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
    public static _BaseFormFactory formFactory; // Its Required to set this in Server.class Component

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
                return formFactory.formFromJsonWithValidation(Swagger_InstanceSnapShotConfiguration.class, Json.parse(this.json_additional_parameter));
            } else {

                Model_InstanceSnapshot snapshot = Model_InstanceSnapshot.find.query().nullable().where().eq("instance.id", instance.id).ne("id", this.id).isNotNull("json_additional_parameter").orderBy("deployed").setMaxRows(1).findOne();

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
                                    program_config.connection_token = getInstanceId() + "/" + program_config.grid_program_id;

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
                                program_config.connection_token = getInstanceId() + "/" + program_config.grid_program_id;

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
                            program_config.connection_token = getInstanceId() + "/" + program_config.grid_program_id;

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

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(value = "File Link")
    public String link_to_download() {
        try {
            if (getBlob() != null) {
                return getBlob().link;
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }
        return null;
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_Blob getBlob() {
        return isLoaded("program") ? program : Model_Blob.find.query().nullable().where().eq("snapshot.id", id).findOne();
    }

    @JsonIgnore
    public Model_BProgramVersion getBProgramVersion() {
        return isLoaded("b_program_version") ? b_program_version : Model_BProgramVersion.find.query().nullable().where().eq("instances.id", id).findOne();
    }

    @JsonIgnore
    public Model_Instance getInstance() {
        return isLoaded("instance") ? instance : Model_Instance.find.query().where().eq("snapshots.id", id).findOne();
    }

    @JsonIgnore
    public UUID getInstanceId() {

        if (idCache().gets(Model_Instance.class) == null) {
            idCache().add(Model_Instance.class, (UUID) Model_Instance.find.query().where().eq("snapshots.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_Instance.class);
    }

    @JsonIgnore
    public List<UUID> getHardwareIds() {
        try {

            List<UUID> list = new ArrayList<>();

            for (Swagger_InstanceSnapshot_JsonFile_Interface interface_hw : this.getProgram().interfaces) {

                if (interface_hw.type.equals("hardware")) {
                    list.add(interface_hw.target_id);
                }
            }

            return list;
        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public List<UUID> getHardwareGroupIds() {
        List<UUID> list = new ArrayList<>();

        for (Swagger_InstanceSnapshot_JsonFile_Interface interface_hw : this.getProgram().interfaces) {

            if (interface_hw.type.equals("group")) {
                list.add(interface_hw.target_id);
            }
        }

        return list;
    }

    @JsonIgnore
    public Model_Product getProduct() {
        return this.getInstance().getProject().getProduct();

    }

    @JsonIgnore
    public Swagger_InstanceSnapshot_JsonFile getProgram() {
        try {

            return formFactory.formFromJsonWithValidation(Swagger_InstanceSnapshot_JsonFile.class, Json.parse(getBlob().downloadString()));

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore @Override
    public Model_Project getProject() {
        return this.getInstance().getProject();
    }

    @JsonIgnore
    public List<Model_Hardware> getRequiredHardware() {
        List<Model_Hardware> hardwareList = new ArrayList<>();

        logger.debug("getRequiredHardware: this.getProgram: ", Json.toJson(this.getProgram()));


        this.getProgram().interfaces.forEach(iface -> {

            if (iface.type.equals("hardware")) {

                logger.debug("getRequiredHardware: this.getProgram interface Hardware: ", iface.target_id);
                try {
                    if (hardwareList.stream().noneMatch(hw -> hw.id == iface.target_id)) {
                        hardwareList.add(Model_Hardware.find.byId(iface.target_id));
                    }
                } catch (NotFoundException e) {
                    logger.warn("getRequiredHardware - hardware from added interfaces was not found, id: {}", iface.target_id);
                }

            } else if (iface.type.equals("group")) {
                try {

                    logger.debug("getRequiredHardware: this.getProgram interface Group: {} ", iface.target_id);
                    Model_HardwareGroup group = Model_HardwareGroup.find.byId(iface.target_id);


                    logger.debug("getRequiredHardware: this.getProgram interface Group size: {} ",  group.getHardware().size());

                    group.getHardware().forEach(hardware -> {

                        logger.debug("getRequiredHardware: this.getProgram interface Group add Device ", hardware.name);

                        if (!hardwareList.contains(hardware)) {
                            hardwareList.add(hardware);
                        }
                    });

                } catch (NotFoundException e) {
                    logger.warn("getRequiredHardware - hardware group from added interfaces was not found, id: {}", iface.target_id);
                }
            }
        });

        return hardwareList;
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    /**
     * Saved Snap Shot as default, but server is offline, so it will be uploaded as soon as possible.
     */
    public Model_Notification notificationServerOffline() {
        return new Model_Notification()
                .setImportance(NotificationImportance.LOW)
                .setLevel(NotificationLevel.INFO)
                .setText( new Notification_Text().setText("Server "))
                .setObject(this.getInstance().getServer())
                .setText( new Notification_Text().setText(" is "))
                .setText( new Notification_Text().setText("offline").setBoldText().setColor(Becki_color.byzance_red))
                .setText( new Notification_Text().setText("."))
                .setNewLine()
                .setText( new Notification_Text().setText("The instance snapshot will be deployed as soon as possible."));
    }

    public Model_Notification notificationDeploymentStart() {
        return new Model_Notification()
                .setImportance(NotificationImportance.LOW)
                .setLevel(NotificationLevel.INFO)
                .setText( new Notification_Text().setText("The instance snapshot from the "))
                .setObject(this.getBProgramVersion())
                .setText( new Notification_Text().setText(" is being deployed."));
    }

    public Model_Notification notificationDeploymentSuccess() {
        return new Model_Notification()
                .setImportance(NotificationImportance.LOW)
                .setLevel(NotificationLevel.SUCCESS)
                .setText(new Notification_Text().setText("The instance snapshot from the "))
                .setObject(this.getBProgramVersion())
                .setText(new Notification_Text().setText(" was deployed successfully."));
    }

    public Model_Notification notificationDeploymentFail(String message) {
        return new Model_Notification()
                .setImportance(NotificationImportance.LOW)
                .setLevel(NotificationLevel.WARNING)
                .setText(new Notification_Text().setText("Deployment of the instance snapshot from the "))
                .setObject(this.getBProgramVersion())
                .setText(new Notification_Text().setText(" has failed with error: "))
                .setText(new Notification_Text().setText(message).setBoldText())
                .setText(new Notification_Text().setText("."));
    }

/* Helper Class --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
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
        summary.grid_app_url += getInstanceId() + "/" ;

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
            M_Program_Parser program_parser = formFactory.formFromJsonWithValidation(M_Program_Parser.class, m_code);

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

        this.idCache().add(Model_Instance.class, this.getInstanceId());

        // Add to Cache
        if(getInstance() != null) {
            getInstance().getSnapShotsIds();
            getInstance().idCache().add(this.getClass(), id);
            getInstance().sort_Model_InstanceSnapshot_ids();
        }
    }

    @Override
    public boolean delete() {

        logger.debug("delete - deleting from database, id: {} ", this.id);

        getInstance().idCache().remove(this.getClass(), this.id);

        return super.delete();
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
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
