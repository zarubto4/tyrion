package websocket.interfaces;

import akka.stream.Materializer;
import com.google.inject.Inject;
import controllers._BaseFormFactory;
import exceptions.NotFoundException;
import models.Model_CProgramVersion;
import models.Model_Hardware;
import models.Model_Person;
import models.Model_Project;
import mongo.ModelMongo_Hardware_RegistrationEntity;
import org.mindrot.jbcrypt.BCrypt;
import play.libs.Json;
import utilities.Server;
import utilities.document_mongo_db.document_objects.DM_Board_Bootloader_DefaultConfig;
import utilities.enums.NetworkStatus;
import utilities.enums.ServerMode;
import utilities.hardware.synchronization.SynchronizationService;
import utilities.logger.Logger;
import utilities.network.NetworkStatusService;
import websocket.Interface;
import websocket.Message;
import websocket.messages.homer_hardware_with_tyrion.*;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Model_Hardware_Temporary_NotDominant_record;
import websocket.messages.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Progress;
import websocket.messages.tyrion_with_becki.WS_Message_Online_Change_status;

import java.util.Date;
import java.util.UUID;

public class Homer extends Interface {

    private static final Logger logger = new Logger(Homer.class);

    private final SynchronizationService synchronizationService;

    private boolean authorized;

    @Inject
    public Homer(NetworkStatusService networkStatusService, Materializer materializer, _BaseFormFactory formFactory, SynchronizationService synchronizationService) {
        super(networkStatusService, materializer, formFactory);
        this.synchronizationService = synchronizationService;
    }

    @Override
    public void onMessage(Message message) {
        try {
            switch (message.getChannel()) {
                case "homer_server": break;
                case "hardware": this.onMessageHardware(message); break;
                case "instance": break;

                default:
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }
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

            this.networkStatusService.setStatus(hardware, NetworkStatus.ONLINE);

            if (hardware.project().id == null) {
                logger.warn("onHardwareConnected - hardware {} is not in project", hardware.id);
            }

            if (hardware.database_synchronize) {
                this.synchronizationService.synchronize(hardware);
            }

            // Notifikce
            if (hardware.developer_kit) {
                try {
                    hardware.notification_board_connect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (hardware.connected_server_id != this.getId()) {
                logger.warn("onHardwareConnected - updating connected server id property to: {}", this.getId());
                hardware.connected_server_id = this.getId();
                hardware.update();
            }

            hardware.make_log_connect();

            // ZDe do budoucna udělat synchronizaci jen když to bude opravdu potřeba - ale momentálně je nad lidské síly udělat argoritmus,
            // který by vyřešil zbytečné dotazování
            hardware.hardware_firmware_state_check();

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

            logger.debug("master_device_Disconnected:: Updating hardware status " + help.uuid + " on offline ");

            this.networkStatusService.setStatus(hardware, NetworkStatus.OFFLINE);


            // Uprava Cache Paměti
            hardware.cache_latest_online = new Date().getTime();


            // Standartní synchronizace
            if (hardware.project().id != null) {
                WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Hardware.class, hardware.id, false, hardware.project().id);
            }

            if (hardware.developer_kit) {
                // Notifikace
                hardware.notification_board_disconnect();
            }

            // Záznam do DM databáze
            hardware.make_log_disconnect();

            Model_Hardware.cache_status.put(hardware.id, false);

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
