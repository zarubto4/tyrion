package utilities.hardware;

import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.FailedMessageException;
import io.ebean.Expr;
import models.Model_Hardware;
import models.Model_HardwareUpdate;
import models.Model_HomerServer;
import utilities.document_mongo_db.document_objects.DM_Board_Bootloader_DefaultConfig;
import utilities.enums.*;
import utilities.logger.Logger;
import utilities.swagger.input.Swagger_Board_Developer_parameters;
import websocket.Message;
import websocket.Request;
import websocket.WebSocketInterface;
import websocket.messages.homer_hardware_with_tyrion.*;
import websocket.messages.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Command;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * This interface object is used for every interaction with the physical hardware.
 */
public class HardwareInterface {

    private static final Logger logger = new Logger(HardwareInterface.class);

    private final Model_Hardware hardware;
    private final WebSocketInterface webSocketInterface;

    public HardwareInterface(Model_Hardware hardware, WebSocketInterface webSocketInterface) {
        this.hardware = hardware;
        this.webSocketInterface = webSocketInterface;
    }

    /**
     * Sends a command to the hardware.
     * @param command to execute
     * @param highestPriority if true the command will be executed as soon as possible
     */
    public void command(BoardCommand command, boolean highestPriority) {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Hardware_command_execute().make_request(Collections.singletonList(this.hardware.id), command, highestPriority)));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        }
    }

    /**
     * Gets the network status of the hardware.
     * @return
     */
    public NetworkStatus getNetworkStatus() {
        Message message = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Hardware_online_status().make_request(Collections.singletonList(hardware.getId()))));
        WS_Message_Hardware_online_status response = message.as(WS_Message_Hardware_online_status.class);
        return response.is_device_online(hardware.getId()) ? NetworkStatus.ONLINE : NetworkStatus.OFFLINE;
    }

    public WS_Message_Hardware_overview_Board getOverview() {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Hardware_overview().make_request(Collections.singletonList(this.hardware.getId()))));

        if (response.isSuccessful()) {
            return response.as(WS_Message_Hardware_overview.class).get_device_from_list(this.hardware.getId());
        } else if (response.isErroneous()) {
            throw new FailedMessageException(response);
        } else {
            return null;
        }
    }

    public void setAlias(String alias) {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this.hardware), "alias", alias)));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        }
    }

    public void setDatabaseSynchronize(boolean synchronize) {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this.hardware), "DATABASE_SYNCHRONIZE", synchronize)));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        }
    }

    public void setHardwareGroups(List<UUID> groupIds, Enum_type_of_command command) {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Hardware_set_hardware_groups().make_request(Collections.singletonList(this.hardware), groupIds, command)));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        }
    }

    // TODO rework to something more systematic
    public void setConfiguration(Swagger_Board_Developer_parameters help) {
        DM_Board_Bootloader_DefaultConfig configuration = this.hardware.bootloader_core_configuration();

        try {

            ObjectNode message;

            String name = help.parameter_type.toLowerCase();

            Field field = configuration.getClass().getField(name);
            Class<?> type = field.getType();

            if (type.equals(Boolean.class)) {

                // Jediná přístupná vyjímka je pro autoback - ten totiž je zároven v COnfig Json (DM_Board_Bootloader_DefaultConfig)
                // Ale zároveň je také přímo přístupný v databázi Tyriona
                if (name.equals("autobackup")) {
                    this.hardware.backup_mode = help.boolean_value; // TODO no modification should be here
                    // Update bude proveden v this.update_bootloader_configuration
                }

                field.set(configuration, help.boolean_value);

                message = new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this.hardware), name, help.boolean_value);

            } else if (type.equals(String.class)) {

                field.set(configuration, help.string_value);

                message = new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this.hardware), name, help.string_value);

            } else if (type.equals(Integer.class)) {

                field.set(configuration, help.integer_value);

                message = new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this.hardware), name, help.integer_value);
            } else {
                throw new NoSuchFieldException();
            }

            if (!configuration.pending.contains(name)) {
                configuration.pending.add(name);
            }

            this.hardware.update_bootloader_configuration(configuration);

            this.webSocketInterface.sendWithResponseAsync(new Request(message), (response) -> {
                if (response.isErroneous()) {
                    logger.internalServerError(new Exception("Got error response: " + response.getMessage().toString()));
                }
            });

        } catch (Exception e) {
            logger.internalServerError(e);
            throw new IllegalArgumentException("Incoming Value " + help.parameter_type.toLowerCase() + " not recognized");
        }
    }

    // TODO rework to something better
    public void setAutoBackup() {
        // 1) změna registru v configur
        DM_Board_Bootloader_DefaultConfig configuration = this.hardware.bootloader_core_configuration();
        configuration.autobackup = true;
        this.hardware.update_bootloader_configuration(configuration);
        // V databázi
        this.hardware.backup_mode = true;
        this.hardware.update();

        //Zabít všechny procedury kde je nastaven backup a ještě nebyly provedeny - ty co jsou teprve v plánu budou provedeny standartně
        List<Model_HardwareUpdate> firmware_plans = Model_HardwareUpdate.find.query().where().eq("hardware.id", this.hardware.id)
                .disjunction()
                .add(Expr.eq("state", HardwareUpdateState.PENDING))
                .add(Expr.eq("state", HardwareUpdateState.RUNNING))
                .endJunction()
                .eq("firmware_type", FirmwareType.BACKUP.name())
                // TODO .lt("actualization_procedure.date_of_planing", new Date())
                // TODO .order().desc("actualization_procedure.date_of_planing")
                .select("id")
                .findList();

        // Zaloha kdyby byly stále platné aktualizace na backup
        for (int i = 0; i < firmware_plans.size(); i++) {
            firmware_plans.get(i).state = HardwareUpdateState.OBSOLETE;
            firmware_plans.get(i).update();
        }

        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this.hardware), "autobackup", true)));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        }
    }

    public void relocate(Model_HomerServer server) {
        this.relocate(server.server_url, server.mqtt_port.toString());
    }

    public void relocate(String mqttHost, String mqttPort) {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Hardware_change_server().make_request(mqttHost, mqttPort, Collections.singletonList(this.hardware.id))));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        }
    }

    public void changeUUIDOnServer(UUID oldId) {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Hardware_uuid_converter_cleaner().make_request(this.hardware.id, oldId, this.hardware.full_id)));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        }
    }

    public void removeUUIDOnServer() {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Hardware_uuid_converter_cleaner().make_request(null, this.hardware.id, this.hardware.full_id)));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        }
    }

    public void update(Model_HardwareUpdate update) {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Hardware_UpdateProcedure_Command().make_request(Collections.singletonList(update.get_brief_for_update_homer_server()))));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        }
    }
}
