package websocket.interfaces;

import akka.stream.Materializer;
import com.google.inject.Inject;
import controllers._BaseFormFactory;
import exceptions.NotFoundException;
import models.*;
import mongo.ModelMongo_Hardware_RegistrationEntity;
import org.mindrot.jbcrypt.BCrypt;
import play.libs.concurrent.HttpExecutionContext;
import utilities.Server;
import utilities.enums.ServerMode;
import utilities.hardware.DominanceService;
import utilities.hardware.HardwareEvents;
import utilities.hardware.HardwareOverviewService;
import utilities.homer.HomerEvents;
import utilities.swagger.input.Swagger_InstanceSnapShotConfiguration;
import utilities.swagger.input.Swagger_InstanceSnapShotConfigurationFile;
import utilities.swagger.input.Swagger_InstanceSnapShotConfigurationProgram;
import utilities.hardware.update.UpdateService;
import utilities.logger.Logger;
import websocket.Interface;
import websocket.Message;
import websocket.TimeOut;
import websocket.messages.homer_hardware_with_tyrion.*;
import websocket.messages.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Progress;
import websocket.messages.homer_instance_with_tyrion.WS_Message_Instance_set_hardware;
import websocket.messages.homer_instance_with_tyrion.WS_Message_Instance_set_program;
import websocket.messages.homer_instance_with_tyrion.WS_Message_Instance_set_terminals;
import websocket.messages.homer_instance_with_tyrion.WS_Message_Instance_status;
import websocket.messages.homer_instance_with_tyrion.verification.WS_Message_Grid_token_verification;
import websocket.messages.homer_instance_with_tyrion.verification.WS_Message_WebView_token_verification;
import websocket.messages.homer_with_tyrion.WS_Message_Homer_Token_validation_request;
import websocket.messages.homer_with_tyrion.verification.WS_Message_Check_homer_server_permission;
import websocket.messages.homer_with_tyrion.verification.WS_Message_Homer_Verification_result;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Homer extends Interface {

    private static final Logger logger = new Logger(Homer.class);

    public static final String CHANNEL = "homer_server";

    public static HashMap<UUID, Homer> apiKeys = new HashMap<>(); // TODO use DI instead of static field

    private final HardwareEvents hardwareEvents;
    private final UpdateService updateService;
    private final HomerEvents homerEvents;
    private final DominanceService dominanceService;
    private final HardwareOverviewService hardwareOverviewService;

    private boolean authorized = false;
    private UUID apiKey;

    @Inject
    public Homer(Materializer materializer, _BaseFormFactory formFactory, HardwareEvents hardwareEvents, UpdateService updateService, TimeOut timeOut,
                 HomerEvents homerEvents, DominanceService dominanceService, HttpExecutionContext httpExecutionContext, HardwareOverviewService hardwareOverviewService) {
        super(httpExecutionContext, materializer, formFactory, timeOut);
        this.hardwareEvents = hardwareEvents;
        this.updateService = updateService;
        this.homerEvents = homerEvents;
        this.dominanceService = dominanceService;
        this.hardwareOverviewService = hardwareOverviewService;
    }

    @Override
    public void onMessage(Message message) {

        if (!this.authorized) {
            if (message.getType().equals(WS_Message_Check_homer_server_permission.message_type)) {
                this.onHomerAuthenticate(message.as(WS_Message_Check_homer_server_permission.class));
            } else {
                // TODO send fail and close
            }

            return; // Do not accept any messages until the server is authorized
        }

        switch (message.getChannel()) {
            case "homer_server": this.onMessageHomer(message); break;
            case "hardware": this.onMessageHardware(message); break;
            case "instance": this.onMessageInstance(message); break;
            default: logger.warn("onMessage - cannot consume message with id: {}, unknown channel: {}", message.getId(), message.getChannel());
        }
    }

    @Override
    public String getDefaultChannel() {
        return CHANNEL;
    }

    @Override
    protected void onClose() {
        super.onClose();
        apiKeys.remove(this.apiKey);
        this.homerEvents.disconnected(Model_HomerServer.find.byId(this.getId()));
    }

/* HOMER MESSAGES ------------------------------------------------------------------------------------------------------*/

    private void onMessageHomer(Message message) {
        switch (message.getType()) {
            case "tyrion_ping": break;
            case WS_Message_Check_homer_server_permission.message_type: this.onHomerAuthenticate(message.as(WS_Message_Check_homer_server_permission.class)); break;
            case WS_Message_Homer_Token_validation_request.message_type: this.onLoggerSubscriptionAuthenticate(message.as(WS_Message_Homer_Token_validation_request.class)); break;
            default: logger.warn("onMessageHomer - cannot consume message with id: {}, unknown type: {}", message.getId(), message.getType());
        }
    }

    private void onHomerAuthenticate(WS_Message_Check_homer_server_permission message) {
        try {

            Model_HomerServer server = Model_HomerServer.find.byId(this.id);

            if (message.hash_token.equals(server.hash_certificate)) {

                this.apiKey = UUID.randomUUID();

                apiKeys.put(this.apiKey, this);

                this.authorized = true;

                this.tell(new WS_Message_Homer_Verification_result().make_request(true, this.apiKey.toString()).put("message_id", message.message_id));
                
                this.homerEvents.connected(server);

            } else {
                this.tell(new WS_Message_Homer_Verification_result().make_request(false, null).put("message_id", message.message_id));
            }

        } catch (Exception e) {
            logger.internalServerError(e);
            this.tell(new WS_Message_Homer_Verification_result().make_request(false, null).put("message_id", message.message_id));
        }
    }

    private void onLoggerSubscriptionAuthenticate(WS_Message_Homer_Token_validation_request message) {
        try {
            Model_HomerServer server = Model_HomerServer.find.byId(this.id);

            Model_AuthorizationToken authorizationToken = Model_AuthorizationToken.getByToken(UUID.fromString(message.client_token));
            if (authorizationToken.isValid()) {
                if (!server.isPublic()) {
                    Model_Person person = authorizationToken.getPerson();
                    if (Model_Project.find.query().where().eq("servers.id", server.id).eq("persons.id", person.id).findCount() == 0) {
                        this.tell(message.get_result(false));
                        return;
                    }
                }

                this.tell(message.get_result(true));
                return;
            }
        } catch (NotFoundException e) {
            logger.warn("onLoggerSubscriptionAuthenticate - not found");
        }

        this.tell(message.get_result(false));
    }

/* INSTANCE MESSAGES ---------------------------------------------------------------------------------------------------*/

    private void onMessageInstance(Message message) {
        switch (message.getType()) {
            case WS_Message_Grid_token_verification.message_type: this.onTerminalVerify(message.as(WS_Message_Grid_token_verification.class)); break;
            case WS_Message_WebView_token_verification.messageType: this.onRemoteViewVerify(message.as(WS_Message_WebView_token_verification.class)); break;
            case WS_Message_Instance_set_hardware.message_type: { logger.warn("WS_Message_Instance_device_set_snap: A message with a very high delay has arrived."); return;}
            case WS_Message_Instance_status.message_type: { logger.warn("WS_Message_Instance_status: A message with a very high delay has arrived."); return;}
            case WS_Message_Instance_set_terminals.message_type: { logger.warn("WS_Message_Instance_terminal_set_snap: A message with a very high delay has arrived."); return;}
            case WS_Message_Instance_set_program.message_type: { logger.warn("WS_Message_Instance_upload_blocko_program: A message with a very high delay has arrived."); return;}

            default: logger.warn("onMessageInstance - cannot consume message with id: {}, unknown type: {}", message.getId(), message.getType());
        }
    }

    // TODO improve
    private void onTerminalVerify(WS_Message_Grid_token_verification message) {
        try {

            logger.info("onTerminalVerify - verifying GRID token: {}, for instance: {}", message.token, message.instance_id);

            Model_GridTerminal terminal = Model_GridTerminal.find.query().nullable().where().eq("terminal_token", message.token).findOne();

            Model_Instance instance = Model_Instance.find.byId(message.instance_id);

            // Pokud je terminall null - nikdy se uživatel nepřihlásit a nevytvořil se o tom záznam - ale to stále neznamená že není možno povolit přístup
            if (terminal == null) {

                logger.trace("cloud_verification_token_GRID:: terminal == null");
                // Najít c configuráku token

                Swagger_InstanceSnapShotConfiguration settings = instance.current_snapshot().settings();
                Swagger_InstanceSnapShotConfigurationFile collection = null;
                Swagger_InstanceSnapShotConfigurationProgram program = null;

                if (settings == null) {
                    logger.error("SnapShotConfiguration is missing return null");
                    throw new NotFoundException(Swagger_InstanceSnapShotConfiguration.class);
                }

                for (Swagger_InstanceSnapShotConfigurationFile grids_collection : settings.grids_collections) {
                    for (Swagger_InstanceSnapShotConfigurationProgram grids_program : grids_collection.grid_programs) {
                        if (grids_program.grid_program_id.equals(message.grid_app_id) || grids_program.grid_program_version_id.equals(message.grid_app_id)) {
                            logger.debug("onTerminalVerify - set collection and program");
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

                logger.debug("Enum_MProgram_SnapShot_settings: {}", program.snapshot_settings);

                switch (program.snapshot_settings) {

                    case PUBLIC: {

                        this.tell(message.get_result(true));
                        return;
                    }

                    case PROJECT: {

                        this.tell(message.get_result(false));
                        return;
                    }

                    case TESTING:{

                        this.tell(message.get_result(false));
                        return;
                    }

                    default: {

                        this.tell(message.get_result(false));
                    }
                }
            } else {

                logger.trace("cloud_verification_token_GRID::  terminal != null");
                logger.debug("cloud_verification_token_GRID::  Person id:: {}", terminal.person.id);
                logger.debug("cloud_verification_token_GRID::  Person mail:: {}", terminal.person.email);
                logger.debug("cloud_verification_token_GRID::  Instance ID:: {} ", message.instance_id);
                logger.debug("cloud_verification_token_GRID::  App ID:: {}", message.grid_app_id);


                if (terminal.person == null) {
                    logger.trace("cloud_verification_token_GRID:: Person is null");
                    logger.debug("cloud_verification_token:: Grid_Terminal object has not own Person - its probably public - Trying to find Instance");

                    if (Model_Instance.find.query().where().eq("id", message.instance_id).findCount() > 0) {
                        logger.trace("cloud_verification_token_GRID:: Permission found");
                        this.tell(message.get_result(true));
                    } else {
                        logger.trace("cloud_verification_token_GRID:: Permission not found");
                        this.tell(message.get_result(false));
                    }

                } else {
                    logger.trace("cloud_verification_token_GRID:: Person is not null!");
                    logger.debug("cloud_verification_token:: Grid_Terminal object has  own Person - its probably private or it can be public - Trying to find Instance with user ID and public value");
                    if (Model_Instance.find.query().where()
                            .eq("id", message.instance_id)
                            .eq("project.persons.id", terminal.person.id)
                            // .or(Expr.eq("project.participants.person.id", terminal.person.id), Expr.eq("actual_instance.version.public_version", true)) TODO find grid access settings
                            .findCount() > 0) {
                        logger.trace("cloud_verification_token_GRID:: Permission found");
                        this.tell(message.get_result(true));
                    } else {
                        logger.trace("cloud_verification_token_GRID:: Permission not found");
                        this.tell(message.get_result(false));
                    }
                }
            }

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    private void onRemoteViewVerify(WS_Message_WebView_token_verification message) {
        try {

            logger.info("onRemoteViewVerify - verifying VIEW token: {}, for instance: {}", message.token, message.instance_id);

            Model_HomerServer server = Model_HomerServer.find.byId(this.id);

            Model_AuthorizationToken authorizationToken = Model_AuthorizationToken.getByToken(message.token);
            if (authorizationToken.isValid()) {
                Model_Person person = authorizationToken.getPerson();
                if (!server.isPublic()) {
                    if (Model_Project.find.query().where().eq("servers.id", server.id).eq("persons.id", person.id).findCount() == 0) {
                        this.tell(message.get_result(false));
                        return;
                    }
                } else if (Model_Instance.find.query().where().eq("id", message.instance_id).eq("b_program.project.persons.id", authorizationToken.get_person_id()).findCount() == 0) {
                    this.tell(message.get_result(false));
                    return;
                }

                this.tell(message.get_result(true));
                return;
            }
        } catch (NotFoundException e) {
            logger.warn("onRemoteViewVerify - not found");
        }

        this.tell(message.get_result(false));
    }

/* HARDWARE MESSAGES ---------------------------------------------------------------------------------------------------*/

    private void onMessageHardware(Message message) {
        switch (message.getType()) {
            case WS_Message_Hardware_connected.message_type: this.onHardwareConnected(message.as(WS_Message_Hardware_connected.class)); break;
            case WS_Message_Hardware_disconnected.message_type: this.onHardwareDisconnected(message.as(WS_Message_Hardware_disconnected.class)); break;
            case WS_Message_Hardware_online_status.message_type: this.device_online_synchronization_echo(message.as(WS_Message_Hardware_online_status.class)); break; // TODO probably not needed
            case WS_Message_Hardware_autobackup_made.message_type: this.device_auto_backup_done_echo(message.as(WS_Message_Hardware_autobackup_made.class)); break;
            case WS_Message_Hardware_autobackup_making.message_type: this.device_auto_backup_start_echo(message.as(WS_Message_Hardware_autobackup_making.class)); break;
            case WS_Message_Hardware_validation_request.message_type: this.onHardwareMQTTAuthentication(message.as(WS_Message_Hardware_validation_request.class)); break;
            case WS_Message_Hardware_terminal_logger_validation_request.message_type: this.onHardwareLoggerSubscriptionAuthentication(message.as(WS_Message_Hardware_terminal_logger_validation_request.class)); break;
            case WS_Message_Hardware_uuid_converter.message_type: this.onConvertFullIdToUUID(message.as(WS_Message_Hardware_uuid_converter.class)); break;
            case WS_Message_Hardware_set_settings.message_type: this.device_settings_set(message.as(WS_Message_Hardware_set_settings.class)); break;
            case WS_Message_Hardware_UpdateProcedure_Progress.message_type: this.updateService.onUpdateMessage(message.as(WS_Message_Hardware_UpdateProcedure_Progress.class)); break;

            // Ignor messages - Jde pravděpodobně o zprávy - které přišly s velkým zpožděním - Tyrion je má ignorovat
            case WS_Message_Hardware_command_execute.message_type:
            case WS_Message_Hardware_overview.message_type:
            case WS_Message_Hardware_change_server.message_type: {
                logger.warn("onMessageHardware - received unhandled message: {}, probably just delayed response", message.getType());
                break;
            }

            case "ping": {
                // Do nothing
                return;
            }

            default: {
                logger.error("onMessageHardware - incoming message not recognized: {}", message.getMessage().toString());
                if (!message.isErroneous()) {
                    this.tell(message.getMessage().put("error_message", "message_type not recognized").put("error_code", 400));
                }
            }
        }
    }

    private void onHardwareConnected(WS_Message_Hardware_connected help) {
        try {

            logger.debug("onHardwareConnected - hardware is online, full id: {}, id {}", help.full_id, help.uuid);

            Model_Hardware hardware = Model_Hardware.find.byId(help.uuid);

            if (hardware.connected_server_id != this.getId()) {
                logger.warn("onHardwareConnected - updating connected server id property to: {}", this.getId());
                hardware.connected_server_id = this.getId();
                hardware.update();
            }

            this.hardwareEvents.connected(hardware);

        } catch (NotFoundException e) {
            logger.warn("onHardwareConnected - hardware not found, probably unregistered, id: {}", help.uuid);
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    private void onHardwareDisconnected(WS_Message_Hardware_disconnected help) {

        if (help.uuid == null) {
            return;
        }

        try {
            Model_Hardware hardware = Model_Hardware.find.byId(help.uuid);

            this.hardwareEvents.disconnected(hardware);

        } catch (NotFoundException e) {
            logger.warn("onHardwareDisconnected - hardware not found, id: {} ", help.uuid);
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    // TODO Device dělá autobackup
    private void device_auto_backup_start_echo(WS_Message_Hardware_autobackup_making help) {
        try {

            logger.debug("device_auto_backup_start_echo - Device send Echo about making backup on hardware ID:: {} ", help.uuid);

            Model_Hardware hardware = Model_Hardware.find.byId(help.uuid);

            if (hardware.developer_kit) {
                // TODO notification
            }
        } catch (NotFoundException e) {
            logger.warn("device_auto_backup_start_echo - hardware not found, id: {} ", help.uuid);
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    // TODO Device udělal autobackup
    private void device_auto_backup_done_echo(WS_Message_Hardware_autobackup_made help) {
        try {

            logger.debug("device_auto_backup_done_echo:: Device send Echo about backup done on hardware ID:: {} ", help.uuid);

            Model_Hardware hardware = Model_Hardware.find.byId(help.uuid);

            Model_CProgramVersion c_program_version = Model_CProgramVersion.find.query().where().eq("compilation.firmware_build_id", help.uuid).findOne();
            if (c_program_version == null) throw new Exception("Firmware with build ID = " + help.uuid + " was not found in the database!");

            hardware.actual_backup_c_program_version = c_program_version;
            hardware.update();

            if (hardware.developer_kit) {
                // TODO notification
            }
        } catch (NotFoundException e) {
            logger.warn("device_auto_backup_done_echo - hardware not found, id: {} ", help.uuid);
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    // žádost void o synchronizaci online stavu
    private void device_online_synchronization_echo(WS_Message_Hardware_online_status report) {
        try {
            for (WS_Message_Hardware_online_status.DeviceStatus status : report.hardware_list) {
                try {
                    this.hardwareEvents.connected(Model_Hardware.find.byId(UUID.fromString(status.uuid)));
                } catch (IllegalArgumentException exception) {
                    //handle the case where string is not valid UUID
                }
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    private void onHardwareMQTTAuthentication(WS_Message_Hardware_validation_request request) {
        try {

            Model_HomerServer server = Model_HomerServer.find.byId(this.id);

            Model_Hardware hardware = this.dominanceService.getDominant(request.full_id);

            if (hardware == null) {
                logger.debug("onHardwareMQTTAuthentication - dominant hardware was not found");

                // For public Homer server are all hardware allowed, but only if we find this Hardware in Central authority database!
                if (server.isPublic()) {

                    logger.debug("onHardwareMQTTAuthentication - hardware is connected to a public server");

                    List<Model_Hardware> historical = Model_Hardware.find.query().nullable().where().eq("full_id", request.full_id).findList();

                    logger.debug("onHardwareMQTTAuthentication - found {} non-dominant hardware in DB", historical.size());

                    for (Model_Hardware hw : historical) {
                        logger.debug("onHardwareMQTTAuthentication - checking request password: {} against: {}", request.password , hw.mqtt_password);
                        logger.debug("onHardwareMQTTAuthentication - checking request name: {} against: {}", request.user_name , hw.mqtt_username);

                        if ((BCrypt.checkpw(request.password, hw.mqtt_password) && BCrypt.checkpw(request.user_name, hw.mqtt_username))
                                || (BCrypt.checkpw(request.user_name, hw.mqtt_password) && BCrypt.checkpw(request.password, hw.mqtt_username))
                                || (Server.mode == ServerMode.DEVELOPER && request.user_name.equals("user") && request.password.equals("pass"))) {

                            logger.debug("onHardwareMQTTAuthentication - found hardware with same credentials, access allowed, id: {}", request.full_id);
                            UUID randomId = this.dominanceService.rememberNondominant(request.full_id, this.id);
                            this.tell(request.get_result(true,  randomId, null, false));
                            return;
                        }
                    }

                    if (!historical.isEmpty()) {
                        logger.debug("onHardwareMQTTAuthentication - did not find any hardware with same credentials, access denied, id: {}", request.full_id);
                        this.tell(request.get_result(false,  null, null, false));
                        return;
                    }

                    ModelMongo_Hardware_RegistrationEntity entity = ModelMongo_Hardware_RegistrationEntity.getbyFull_id(request.full_id);
                    if (entity != null) {
                        logger.debug("onHardwareMQTTAuthentication - found hardware entity in central authority");
                        logger.debug("onHardwareMQTTAuthentication - checking request password: {} against: {}", request.password , entity.mqtt_password);
                        logger.debug("onHardwareMQTTAuthentication - checking request name: {} against: {}", request.user_name , entity.mqtt_username);

                        if ((BCrypt.checkpw(request.password, entity.mqtt_password) && BCrypt.checkpw(request.user_name, entity.mqtt_username))
                                || (BCrypt.checkpw( request.user_name, entity.mqtt_password) && BCrypt.checkpw(request.password, entity.mqtt_username))
                                || (Server.mode == ServerMode.DEVELOPER && request.user_name.equals("user") && request.password.equals("pass"))) {

                            UUID randomId = this.dominanceService.rememberNondominant(request.full_id, this.id);
                            this.tell(request.get_result(true,  randomId, null, false));
                            return;
                        }
                    }

                    logger.debug("onHardwareMQTTAuthentication - did not find the hardware with same credentials anywhere, access denied, id: {}", request.full_id);
                    this.tell(request.get_result(false,  null, null, false));

                } else {

                    // TODO Find public server and redirect Hardware:
                    logger.debug("onHardwareMQTTAuthentication - non-dominant hardware is on private server, redirecting to a public server - TODO!");
                    this.tell(request.get_result(false, null, "redirect_url", false));
                }
                return;
            } else {
                logger.debug("onHardwareMQTTAuthentication - found dominant hardware entity");
                logger.debug("onHardwareMQTTAuthentication - checking request password: {} against: {}", request.password , hardware.mqtt_password);
                logger.debug("onHardwareMQTTAuthentication - checking request name: {} against: {}", request.user_name , hardware.mqtt_username);

                if ((BCrypt.checkpw(request.password, hardware.mqtt_password) && BCrypt.checkpw(request.user_name, hardware.mqtt_username))
                        || (BCrypt.checkpw( request.user_name, hardware.mqtt_password) && BCrypt.checkpw(request.password, hardware.mqtt_username))
                        || (Server.mode == ServerMode.DEVELOPER && request.user_name.equals("user") && request.password.equals("pass"))) {

                    this.tell(request.get_result(true, hardware.id, null, true));
                    return;
                }
            }

            logger.debug("onHardwareMQTTAuthentication - hardware was not found or it has bad credentials, access denied, id {}", hardware.full_id);
            this.tell(request.get_result(false,  hardware.id, null, false));

        } catch (Exception e) {

            logger.internalServerError(e);

            if (Server.mode == ServerMode.DEVELOPER) {
                logger.error("onHardwareMQTTAuthentication - hardware has not permission to connect, but it is allowed DEV mode");
                this.tell(request.get_result(true, null, null, true));
            } else {
                this.tell(request.get_result(false, null, null, true));
            }
        }
    }

    private void onConvertFullIdToUUID(WS_Message_Hardware_uuid_converter request) {

        Model_Hardware hardware = this.dominanceService.getDominant(request.full_id);

        if (request.full_id != null) {
            if (hardware == null) {
                if (this.dominanceService.hasNondominant(request.full_id)) {
                    UUID randomId = this.dominanceService.rememberNondominant(request.full_id, this.id);
                    this.tell(request.get_result(randomId, request.full_id));
                } else {
                    ModelMongo_Hardware_RegistrationEntity entity = ModelMongo_Hardware_RegistrationEntity.getbyFull_id(request.full_id);
                    if (entity != null) {
                        UUID randomId = this.dominanceService.rememberNondominant(request.full_id, this.id);
                        this.tell(request.get_result(randomId, request.full_id));
                    } else {
                        logger.debug("onConvertFullIdToUUID - hardware was not recognized");
                        this.tell(request.get_result_error());
                    }
                }
            } else {
                logger.debug("onConvertFullIdToUUID - found dominant");
                this.tell(request.get_result(hardware.id, hardware.full_id));
            }
            return;
        }

        if (request.uuid != null) {
            try {
                Model_Hardware hardware1 = Model_Hardware.find.byId(request.uuid);
                this.tell(request.get_result(hardware1.id, hardware1.full_id));
            } catch (NotFoundException e) {
                // nothing
            }
        }

        logger.debug("onConvertFullIdToUUID - hardware was not recognized");
        this.tell(request.get_result_error());
    }

    private void onHardwareLoggerSubscriptionAuthentication(WS_Message_Hardware_terminal_logger_validation_request request) {
        try {

            Model_AuthorizationToken authorizationToken = Model_AuthorizationToken.getByToken(UUID.fromString(request.token));
            if (authorizationToken.isValid()) {
                Model_Person person = authorizationToken.getPerson();
                for (UUID id : request.uuid_ids) {
                    Model_Hardware hardware = Model_Hardware.find.byId(id);
                    Model_Project project = hardware.getProject();
                    if (project == null || !project.isParticipant(person)) {
                        this.tell(request.get_result(false));
                        return;
                    }
                }

                this.tell(request.get_result(true));
            }

            this.tell(request.get_result(false));

        } catch (NotFoundException e) {
            this.tell(request.get_result(false));
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    private void device_settings_set(WS_Message_Hardware_set_settings settings) {
        if (settings.key != null) {
            if (settings.uuid != null) {
                Model_Hardware hardware = Model_Hardware.find.byId(settings.uuid);
                this.hardwareOverviewService.invalidate(settings.uuid);
                this.hardwareEvents.configured(hardware, settings.key);
            } else {
                logger.warn("device_settings_set - got message without 'uuid' property");
            }
        } else {
            logger.warn("device_settings_set - got message without 'key' property");
        }
    }
}
