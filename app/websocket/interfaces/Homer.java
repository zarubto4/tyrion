package websocket.interfaces;

import akka.stream.Materializer;
import com.google.inject.Inject;
import com.google.inject.Injector;
import controllers._BaseFormFactory;
import exceptions.NotFoundException;
import models.*;
import mongo.ModelMongo_Hardware_RegistrationEntity;
import org.mindrot.jbcrypt.BCrypt;
import play.libs.Json;
import utilities.Server;
import utilities.document_mongo_db.document_objects.DM_Board_Bootloader_DefaultConfig;
import utilities.enums.HomerType;
import utilities.enums.NetworkStatus;
import utilities.enums.ServerMode;
import utilities.hardware.HardwareEvents;
import utilities.hardware.HardwareService;
import utilities.homer.HomerService;
import utilities.homer.HomerSynchronizationTask;
import utilities.swagger.input.Swagger_InstanceSnapShotConfiguration;
import utilities.swagger.input.Swagger_InstanceSnapShotConfigurationFile;
import utilities.swagger.input.Swagger_InstanceSnapShotConfigurationProgram;
import utilities.synchronization.SynchronizationService;
import utilities.hardware.update.UpdateService;
import utilities.logger.Logger;
import utilities.network.NetworkStatusService;
import websocket.Interface;
import websocket.Message;
import websocket.messages.homer_hardware_with_tyrion.*;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Model_Hardware_Temporary_NotDominant_record;
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
import websocket.messages.tyrion_with_becki.WS_Message_Online_Change_status;

import java.util.HashMap;
import java.util.UUID;

public class Homer extends Interface {

    private static final Logger logger = new Logger(Homer.class);

    public static HashMap<UUID, Homer> apiKeys = new HashMap<>(); // TODO use DI instead of static field

    private final HardwareEvents hardwareEvents;
    private final SynchronizationService synchronizationService;
    private final HardwareService hardwareService;
    private final UpdateService updateService;
    private final HomerService homerService;
    private final Injector injector;

    private boolean authorized = false;
    private UUID apiKey;

    @Inject
    public Homer(NetworkStatusService networkStatusService, Materializer materializer, _BaseFormFactory formFactory, Injector injector, HardwareEvents hardwareEvents,
                 SynchronizationService synchronizationService, UpdateService updateService, HardwareService hardwareService, HomerService homerService) {
        super(networkStatusService, materializer, formFactory);
        this.hardwareEvents = hardwareEvents;
        this.synchronizationService = synchronizationService;
        this.hardwareService = hardwareService;
        this.updateService = updateService;
        this.homerService = homerService;
        this.injector = injector;
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
    protected void onClose() {
        super.onClose();
        apiKeys.remove(this.apiKey);
    }

/* HOMER MESSAGES ------------------------------------------------------------------------------------------------------*/

    private void onMessageHomer(Message message) {
        switch (message.getType()) {
            case "tyrion_ping": break;
            case WS_Message_Check_homer_server_permission.message_type: this.onHomerAuthenticate(message.as(WS_Message_Check_homer_server_permission.class)); break;
            case WS_Message_Homer_Token_validation_request.message_type: this.onLoggerSubscriptionAuthenticate(message.as(WS_Message_Homer_Token_validation_request.class));
            default: logger.warn("onMessageHomer - cannot consume message with id: {}, unknown type: {}", message.getId(), message.getType());
        }
    }

    private void onHomerAuthenticate(WS_Message_Check_homer_server_permission message) {
        try {

            Model_HomerServer server = Model_HomerServer.find.byId(this.id);

            if (message.hash_token.equals(server.hash_certificate)) {

                server.make_log_connect(); // TODO injection

                this.apiKey = UUID.randomUUID();

                apiKeys.put(this.apiKey, this);

                this.authorized = true;

                this.send(new WS_Message_Homer_Verification_result().make_request(true, this.apiKey.toString()).put("message_id", message.message_id));

                // TODO injection rework
                // Send echo to all connected users (its public servers)
                if (server.isPublic()) {
                    WS_Message_Online_Change_status.synchronize_online_state_with_becki_public_objects(Model_HomerServer.class, server.id, true);
                } else {
                    WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_HomerServer.class, server.id, true, server.get_project_id());
                }

                HomerSynchronizationTask task = this.injector.getInstance(HomerSynchronizationTask.class);
                task.setServer(server);

                this.synchronizationService.submit(task);

            } else {
                this.send(new WS_Message_Homer_Verification_result().make_request(false, null).put("message_id", message.message_id));
            }

        } catch (Exception e) {
            logger.internalServerError(e);
            this.send(new WS_Message_Homer_Verification_result().make_request(false, null).put("message_id", message.message_id));
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
                        this.send(message.get_result(false));
                        return;
                    }
                }

                this.send(message.get_result(true));
                return;
            }
        } catch (NotFoundException e) {
            logger.warn("onLoggerSubscriptionAuthenticate - not found");
        }

        this.send(message.get_result(false));
    }

/* INSTANCE MESSAGES ---------------------------------------------------------------------------------------------------*/

    public void onMessageInstance(Message message) {
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

    public void onTerminalVerify(WS_Message_Grid_token_verification message) {
        try {

            logger.info("onTerminalVerify - verifying GRID token: {}, for instance: {}", message.token, message.instance_id);

            Model_GridTerminal terminal = Model_GridTerminal.find.query().nullable().where().eq("terminal_token", message.token).findOne();

            // Pokud je terminall null - nikdy se uživatel nepřihlásit a nevytvořil se o tom záznam - ale to stále neznamená že není možno povolit přístup
            if (terminal == null) {

                logger.trace("cloud_verification_token_GRID:: terminal == null");
                // Najít c configuráku token

                Swagger_InstanceSnapShotConfiguration settings = this.current_snapshot().settings();
                Swagger_InstanceSnapShotConfigurationFile collection = null;
                Swagger_InstanceSnapShotConfigurationProgram program = null;


                if(settings == null){
                    logger.error("SnapShotConfiguration is missing return null");
                    throw new NotFoundException(Swagger_InstanceSnapShotConfiguration.class);
                }

                for(Swagger_InstanceSnapShotConfigurationFile grids_collection : settings.grids_collections){
                    for(Swagger_InstanceSnapShotConfigurationProgram grids_program : grids_collection.grid_programs){
                        if(grids_program.grid_program_id.equals(message.grid_app_id) || grids_program.grid_program_version_id.equals(message.grid_app_id)){
                            logger.debug("cloud_verification_token_GRID:: set collection and program");
                            collection = grids_collection;
                            program = grids_program;
                            break;
                        }
                    }
                }

                if(collection == null){
                    logger.error("SnapShotConfigurationFile is missing return null");
                    throw new NotFoundException(Swagger_InstanceSnapShotConfigurationFile.class);
                }

                logger.debug("Enum_MProgram_SnapShot_settings: {}", program.snapshot_settings);

                switch (program.snapshot_settings) {

                    case PUBLIC: {

                        this.send(message.get_result(true));
                        return;
                    }

                    case PROJECT: {

                        this.send(message.get_result(false));
                        return;
                    }

                    case TESTING:{

                        this.send(message.get_result(false));
                        return;
                    }

                    default: {

                        this.send(message.get_result(false));
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
                        this.send(message.get_result(true));
                    } else {
                        logger.trace("cloud_verification_token_GRID:: Permission not found");
                        this.send(message.get_result(false));
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
                        this.send(message.get_result(true));
                    } else {
                        logger.trace("cloud_verification_token_GRID:: Permission not found");
                        this.send(message.get_result(false));
                    }
                }
            }

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void onRemoteViewVerify(WS_Message_WebView_token_verification message) {
        try {

            logger.info("onRemoteViewVerify - verifying VIEW token: {}, for instance: {}", message.token, message.instance_id);

            Model_HomerServer server = Model_HomerServer.find.byId(this.id);

            Model_AuthorizationToken authorizationToken = Model_AuthorizationToken.getByToken(message.token);
            if (authorizationToken.isValid()) {
                Model_Person person = authorizationToken.getPerson();
                if (!server.isPublic()) {
                    if (Model_Project.find.query().where().eq("servers.id", server.id).eq("persons.id", person.id).findCount() == 0) {
                        this.send(message.get_result(false));
                        return;
                    }
                } else if (Model_Instance.find.query().where().eq("id", message.instance_id).eq("b_program.project.persons.id", authorizationToken.get_person_id()).findCount() == 0) {
                    this.send(message.get_result(false));
                    return;
                }

                this.send(message.get_result(true));
                return;
            }
        } catch (NotFoundException e) {
            logger.warn("onRemoteViewVerify - not found");
        }

        this.send(message.get_result(false));
    }

/* HARDWARE MESSAGES ---------------------------------------------------------------------------------------------------*/

    public void onMessageHardware(Message message) {
        switch (message.getType()) {
            case WS_Message_Hardware_connected.message_type: this.onHardwareConnected(message.as(WS_Message_Hardware_connected.class)); break;
            case WS_Message_Hardware_disconnected.message_type: this.onHardwareDisconnected(message.as(WS_Message_Hardware_disconnected.class)); break;
            case WS_Message_Hardware_online_status.message_type: this.device_online_synchronization_echo(message.as(WS_Message_Hardware_online_status.class)); break; // TODO probably not needed
            case WS_Message_Hardware_autobackup_made.message_type: this.device_auto_backup_done_echo(message.as(WS_Message_Hardware_autobackup_made.class)); break;
            case WS_Message_Hardware_autobackup_making.message_type: this.device_auto_backup_start_echo(message.as(WS_Message_Hardware_autobackup_making.class)); break;
            case WS_Message_Hardware_UpdateProcedure_Progress.message_type: {

                Model_HardwareUpdate.update_procedure_progress(formFromJsonWithValidation(homer, WS_Message_Hardware_UpdateProcedure_Progress.class, json));
                return;
            }

            case WS_Message_Hardware_validation_request.message_type: this.check_mqtt_hardware_connection_validation(message.as(WS_Message_Hardware_validation_request.class)); break;
            case WS_Message_Hardware_terminal_logger_validation_request.message_type: this.check_hardware_logger_access_terminal_validation(message.as(WS_Message_Hardware_terminal_logger_validation_request.class)); break;
            case WS_Message_Hardware_uuid_converter.message_type: this.convert_hardware_full_id_uuid(message.as(WS_Message_Hardware_uuid_converter.class)); break;
            case WS_Message_Hardware_set_settings.message_type: this.device_settings_set(message.as(WS_Message_Hardware_set_settings.class)); break;

            // Ignor messages - Jde pravděpodobně o zprávy - které přišly s velkým zpožděním - Tyrion je má ignorovat
            case WS_Message_Hardware_command_execute.message_type: {
                logger.warn("WS_Message_Hardware_Restart: A message with a very high delay has arrived.");
                return;
            }
            case WS_Message_Hardware_overview.message_type: {
                logger.warn("WS_Message_Hardware_overview: A message with a very high delay has arrived.");
                return;
            }
            case WS_Message_Hardware_change_server.message_type: {
                logger.warn("WS_Message_Hardware_change_server: A message with a very high delay has arrived.");
                return;
            }

            default: {
                logger.error("onMessageHardware - incoming message not recognized: {}", message.getMessage().toString());
                if (!message.isErroneous()) {
                    this.send(message.getMessage().put("error_message", "message_type not recognized").put("error_code", 400));
                }
            }
        }
    }

    public void onHardwareConnected(WS_Message_Hardware_connected help) {
        try {

            logger.debug("onHardwareConnected - hardware, id: {} is online", help.uuid);

            Model_Hardware hardware = Model_Hardware.find.byId(help.uuid);

            if (hardware.connected_server_id != this.getId()) {
                logger.warn("onHardwareConnected - updating connected server id property to: {}", this.getId());
                hardware.connected_server_id = this.getId();
                hardware.update();
            }

            this.hardwareEvents.connected(hardware);

        } catch (NotFoundException e) {
            logger.warn("Hardware not found. Message from Homer server: ID = " + help.websocket_identificator + ". Unregistered Hardware Id: " + help.uuid);
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void onHardwareDisconnected(WS_Message_Hardware_disconnected help) {

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
    public void device_auto_backup_start_echo(WS_Message_Hardware_autobackup_making help) {
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
    public void device_auto_backup_done_echo(WS_Message_Hardware_autobackup_made help) {
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
    public void device_online_synchronization_echo(WS_Message_Hardware_online_status report) {
        try {
            for (WS_Message_Hardware_online_status.DeviceStatus status : report.hardware_list) {

                try{
                    UUID uuid = UUID.fromString(status.uuid);
                    //do something

                    Model_Hardware hardware = Model_Hardware.find.byId(uuid);

                    this.networkStatusService.setStatus(hardware, status.online_status ? NetworkStatus.ONLINE : NetworkStatus.OFFLINE);

                } catch (IllegalArgumentException exception){
                    //handle the case where string is not valid UUID
                }
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void check_mqtt_hardware_connection_validation(WS_Message_Hardware_validation_request request) {
        try {

            logger.debug("check_mqtt_hardware_connection_validation: {} ", Json.toJson(request) );


            Model_Hardware hardware = request.get_hardware();

            if(hardware == null) {
                logger.debug("check_mqtt_hardware_connection_validation: Device has not any active or dominant entity in local database");


                // For public Homer server are all hardware allowed, but only if we find this Hardware in Central authority database!
                if(this.getModelHomerServer().isPublic()) {


                    // We will save last know server, where we have hardware, in case of registration under some project. SAVE is only if device has permission!!!!!
                    WS_Model_Hardware_Temporary_NotDominant_record record = new WS_Model_Hardware_Temporary_NotDominant_record();
                    record.homer_server_id = this.id;
                    record.random_temporary_hardware_id = UUID.randomUUID();

                    logger.debug("check_mqtt_hardware_connection_validation: Device is on Public Server but wihtout dominant entity - so we will save it to cache");

                    logger.debug("check_mqtt_hardware_connection_validation: Before, we will try to know Mqtt PASS and Name - maybe from historical devices in local database?");
                    hardware = Model_Hardware.find.query().where().eq("full_id", request.full_id).setMaxRows(1).select("id").findOne();
                    if(hardware != null) {
                        logger.debug("check_mqtt_hardware_connection_validation: Yes, we find historical device wiht same full_id, now we will check MQTT NAME and PASS");

                        logger.debug("check_mqtt_hardware_connection_validation: Request PASS:: {}", request.password );
                        logger.debug("check_mqtt_hardware_connection_validation: entity PASS:: {}", hardware.mqtt_password );

                        logger.debug("check_mqtt_hardware_connection_validation: Request NAME:: {}", request.user_name );
                        logger.debug("check_mqtt_hardware_connection_validation: entity NAME:: {}", hardware.mqtt_username );

                        if (BCrypt.checkpw(request.password, hardware.mqtt_password) && BCrypt.checkpw(request.user_name, hardware.mqtt_username)) {

                            // Save it - device has permission!
                            cache_not_dominant_hardware.put(request.full_id, record);
                            this.send(request.get_result(true,  record.random_temporary_hardware_id, null, false));
                            return;

                            // in the case of several HW, it was saved in reverse
                        } else if (BCrypt.checkpw( request.user_name, hardware.mqtt_password) && BCrypt.checkpw(request.password, hardware.mqtt_username)) {

                            // Save it - device has permission!
                            cache_not_dominant_hardware.put(request.full_id, record);
                            this.send(request.get_result(true, record.random_temporary_hardware_id, null, false));
                            return;

                        } else if( Server.mode == ServerMode.DEVELOPER && request.user_name.equals("user") && request.password.equals("pass")) {

                            cache_not_dominant_hardware.put(request.full_id, record);
                            this.send(request.get_result(true,  record.random_temporary_hardware_id, null, false));
                            return;

                        } else {

                            logger.debug("check_mqtt_hardware_connection_validation: Device {} on public server has not right credentials Access Denied ",  hardware.full_id);
                            this.send(request.get_result(false,  null, null, false));
                            return;

                        }
                    }


                    logger.debug("check_mqtt_hardware_connection_validation: Before, we will try to know Mqtt PASS and Name - maybe from central authority?");
                    ModelMongo_Hardware_RegistrationEntity entity = ModelMongo_Hardware_RegistrationEntity.getbyFull_id(request.full_id);
                    if(entity != null) {
                        logger.debug("check_mqtt_hardware_connection_validation: Yes, we find HardwareRegistrationEntity  device with same full_id, now we will check MQTT NAME and PASS");

                        logger.debug("check_mqtt_hardware_connection_validation: Request PASS:: {}", request.password );
                        logger.debug("check_mqtt_hardware_connection_validation: entity PASS:: {}", entity.mqtt_password );

                        logger.debug("check_mqtt_hardware_connection_validation: Request NAME:: {}", request.user_name );
                        logger.debug("check_mqtt_hardware_connection_validation: entity NAME:: {}", entity.mqtt_username );


                        logger.debug("check_mqtt_hardware_connection_validation: PASS valid: {}", BCrypt.checkpw(request.password, entity.mqtt_password));
                        logger.debug("check_mqtt_hardware_connection_validation: USERNAME valid {}", BCrypt.checkpw(request.user_name, entity.mqtt_username));


                        if ( BCrypt.checkpw(request.password, entity.mqtt_password) && BCrypt.checkpw(request.user_name, entity.mqtt_username)) {

                            logger.debug("check_mqtt_hardware_connection_validation: Device {} on public server has  right credentials Access Allowed with ModelMongo_Hardware_RegistrationEntity Device check",  entity.full_id);

                            // Save it - device has permission!
                            cache_not_dominant_hardware.put(request.full_id, record);
                            this.send(request.get_result(true, record.random_temporary_hardware_id, null, false));
                            return;

                            // in the case of several HW, it was saved in reverse
                        } else if ( BCrypt.checkpw( request.user_name, entity.mqtt_password) && BCrypt.checkpw(request.password, entity.mqtt_username)) {

                            logger.debug("check_mqtt_hardware_connection_validation: Device {} on public server has  right credentials Access Allowed with ModelMongo_Hardware_RegistrationEntity Device check",  entity.full_id);

                            // Save it - device has permission!
                            cache_not_dominant_hardware.put(request.full_id, record);
                            this.send(request.get_result(true, record.random_temporary_hardware_id, null, false));
                            return;

                            // Only for DEV server!
                        } else if( Server.mode == ServerMode.DEVELOPER && request.user_name.equals("user") && request.password.equals("pass")) {

                            cache_not_dominant_hardware.put(request.full_id, record);
                            this.send(request.get_result(true,  record.random_temporary_hardware_id, null, false));
                            return;

                        } else {

                            logger.debug("check_mqtt_hardware_connection_validation: Device {} on public server has not right credentials Access Denied with  HardwareRegistrationEntity check",  entity.full_id);
                            this.send(request.get_result(false, null, null, false));
                            return;

                        }
                    }

                    logger.debug("check_mqtt_hardware_connection_validation: Device {} on public server we havent historical or HardwareRegistrationEntity, so Access Denied",  request.full_id);
                    this.send(request.get_result(false,  null, null, false));
                    return;

                } else {

                    logger.debug("check_mqtt_hardware_connection_validation: Device is on PRIVATE!!!! Server but without dominant entity. This is not ok! Wo we will redirect this device to another server");

                    // Find public server and redirect Hardware:
                    this.send(request.get_result(false, null, "redirect_url", false));
                    return;

                }
            }

            logger.debug("check_mqtt_hardware_connection_validation: Request PASS:: {}", request.password );
            logger.debug("check_mqtt_hardware_connection_validation: Hardware PASS:: {}", hardware.mqtt_password );

            logger.debug("check_mqtt_hardware_connection_validation: Request NAME:: {}", request.user_name );
            logger.debug("check_mqtt_hardware_connection_validation: Hardware NAME:: {}", hardware.mqtt_username );


            if (BCrypt.checkpw(request.password, hardware.mqtt_password) && BCrypt.checkpw(request.user_name, hardware.mqtt_username)) {
                logger.debug("check_mqtt_hardware_connection_validation: Device {}:: Access Approve", hardware.full_id);
                this.send(request.get_result(true, hardware.id, null, true));
            } else if (BCrypt.checkpw(request.user_name, hardware.mqtt_password) && BCrypt.checkpw(request.password, hardware.mqtt_username)) {
                logger.debug("check_mqtt_hardware_connection_validation: Device {}:: Access Approve", hardware.full_id);
                this.send(request.get_result(true, hardware.id, null, true));
            }  else if( Server.mode == ServerMode.DEVELOPER && request.user_name.equals("user") && request.password.equals("pass")) {
                logger.debug("check_mqtt_hardware_connection_validation: Device {}:: Access Approve - DEV server - user and pass", hardware.full_id);
                this.send(request.get_result(true, hardware.id, null, true));
            } else {
                logger.debug("check_mqtt_hardware_connection_validation: Device {}:: Access Denied",  hardware.full_id);
                this.send(request.get_result(false,  hardware.id, null, false));
            }

        } catch (Exception e) {

            logger.internalServerError(e);

            if(Server.mode == ServerMode.DEVELOPER) {
                logger.error("check_mqtt_hardware_connection_validation:: Device has not right permission to connect. But this is Dev version of Tyrion. So its allowed.");
                this.send(request.get_result(true, null, null, true));
                // Save it - device has permission!
                return;
            }

            logger.internalServerError(e);
            this.send(request.get_result(false, null, null, true));
        }
    }

    public void convert_hardware_full_id_uuid(WS_Message_Hardware_uuid_converter request) {
        try {

            logger.debug("convert_hardware_full_id_to_uuid:: Incomimng Request for Transformation:: ", Json.toJson(request));

            // Přejlad na UUID
            if(request.full_id != null) {
                Model_Hardware board = Model_Hardware.getByFullId(request.full_id);

                if (board == null) {
                    logger.debug("convert_hardware_full_id_to_uuid:: {} Device Not Found with Dominant Entity - bus there is still hope! with Cache", request.full_id);

                    System.err.println("Právě je čas zkontrolovat cache_not_dominant_hardware od kterého očekávám, že bude mít HW id v sobě: Kontrolované ID je  " + request.full_id);

                    if(cache_not_dominant_hardware.containsKey(request.full_id)) {

                        System.err.println("Ano - Cache obsahuje HW s Full ID:: " + request.full_id);

                        logger.debug("convert_hardware_full_id_to_uuid:: Device Found in cache_not_dominant_hardware");
                        this.send(request.get_result(cache_not_dominant_hardware.get(request.full_id).random_temporary_hardware_id, request.full_id));
                        return;

                    } else {

                        System.err.println("Ne Cache neobsahuje HW s Full ID:: " + request.full_id);

                        ModelMongo_Hardware_RegistrationEntity entity = ModelMongo_Hardware_RegistrationEntity.getbyFull_id(request.full_id);
                        if(entity != null) {

                            System.err.println("Ne Cache neobsahuje HW s Full ID:: " + request.full_id + " ale našel jsem záznam z ModelMongo_Hardware_RegistrationEntity");

                            // We will save last know server, where we have hardware, in case of registration under some project. SAVE is only if device has permission!!!!!
                            WS_Model_Hardware_Temporary_NotDominant_record record = new WS_Model_Hardware_Temporary_NotDominant_record();
                            record.homer_server_id = this.id;
                            record.random_temporary_hardware_id = UUID.randomUUID();
                            cache_not_dominant_hardware.put(request.full_id, record);

                            this.send(request.get_result(cache_not_dominant_hardware.get(request.full_id).random_temporary_hardware_id, request.full_id));
                            return;

                        } else {

                            logger.debug("convert_hardware_full_id_to_uuid:: Device Not Found and also not found in cache");
                            this.send(request.get_result_error());
                            return;
                        }
                    }
                }

                logger.debug("convert_hardware_full_id_to_uuid:: Device found - Return Success");
                this.send(request.get_result(board.id, board.full_id));
                return;
            }

            // Přejlad na FULL_ID
            if(request.uuid != null) {
                Model_Hardware board = Model_Hardware.find.byId(request.uuid);

                if (board == null) {
                    logger.debug("convert_hardware_full_id_to_uuid:: Device Not Found!");
                    this.send(request.get_result_error());
                    return;
                }

                logger.debug("convert_hardware_full_id_to_uuid:: Device found - Return Success");
                this.send(request.get_result(board.id, board.full_id));
                return;
            }

            logger.error("convert_hardware_full_id_to_uuid: Incoming message not contain full_id or uuid!!!!");

        }catch (Exception e){
            logger.internalServerError(e);
        }

    }

    public void check_hardware_logger_access_terminal_validation(WS_Message_Hardware_terminal_logger_validation_request request) {
        try {

            UUID project_id = null;

            for (UUID id : request.uuid_ids) {

                Model_Hardware board =  Model_Hardware.find.byId(id);
                if (board == null) {
                    this.send(request.get_result(false));
                    return;
                }

                if (project_id != null  && project_id.equals(board.project().id)) continue;
                if (project_id != null && !project_id.equals(board.project().id)) project_id = null;

                // P5edpokládá se že project_id bude u všech desek stejné - tak se podle toho bude taky kontrolovat
                if (project_id == null) {
                    Model_Person person = Model_Person.getByAuthToken(request.token);
                    if (person == null) {
                        this.send(request.get_result(false));
                        return;
                    }

                    Model_Project project = Model_Project.find.query().where().eq("persons.id", person.id).eq("id", board.project().id).findOne();

                    if (project == null) {
                        this.send(request.get_result(false));
                        return;
                    }

                    project_id = project.id;
                }
            }

            this.send(request.get_result(true));

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void device_settings_set(WS_Message_Hardware_set_settings settings) {
        if (settings.key != null) {
            if (settings.uuid != null) {
                Model_Hardware hardware = Model_Hardware.find.byId(settings.uuid);

                DM_Board_Bootloader_DefaultConfig configuration = hardware.bootloader_core_configuration();

                configuration.pending.remove(settings.key.toLowerCase());

                hardware.update_bootloader_configuration(configuration);

            } else {
                logger.warn("device_settings_set - got message without 'uuid' property");
            }
        } else {
            logger.warn("device_settings_set - got message without 'key' property");
        }
    }
}
