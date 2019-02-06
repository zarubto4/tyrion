package utilities.hardware;

import io.ebean.Expr;
import models.Model_Hardware;
import models.Model_HardwareUpdate;
import models.Model_HomerServer;
import play.libs.concurrent.HttpExecutionContext;
import utilities.document_mongo_db.document_objects.DM_Board_Bootloader_DefaultConfig;
import utilities.enums.*;
import utilities.logger.Logger;
import websocket.Message;
import websocket.Request;
import websocket.WebSocketInterface;
import websocket.messages.homer_hardware_with_tyrion.*;
import websocket.messages.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Command;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * This interface object is used for every interaction with the physical hardware.
 */
public class HardwareInterface {

    private static final Logger logger = new Logger(HardwareInterface.class);

    private final Model_Hardware hardware;
    private final WebSocketInterface webSocketInterface;
    private final HttpExecutionContext httpExecutionContext;

    public HardwareInterface(Model_Hardware hardware, WebSocketInterface webSocketInterface, HttpExecutionContext httpExecutionContext) {
        this.hardware = hardware;
        this.webSocketInterface = webSocketInterface;
        this.httpExecutionContext = httpExecutionContext;
    }

    /**
     * Sends a command to the hardware.
     * @param command to execute
     * @param highestPriority if true the command will be executed as soon as possible
     */
    public CompletionStage<Message> command(BoardCommand command, boolean highestPriority) {
        return this.webSocketInterface.ask(new Request(new WS_Message_Hardware_command_execute().make_request(Collections.singletonList(this.hardware.id), command, highestPriority)));
    }

    public CompletionStage<NetworkStatus> getNetworkStatus() {
        return this.webSocketInterface.ask(new Request(new WS_Message_Hardware_online_status().make_request(Collections.singletonList(this.hardware.getId()))))
                .thenApplyAsync(message -> {
                    WS_Message_Hardware_online_status response = message.as(WS_Message_Hardware_online_status.class);
                    return response.is_device_online(hardware.getId()) ? NetworkStatus.ONLINE : NetworkStatus.OFFLINE;
                }, this.httpExecutionContext.current());
    }

    public CompletionStage<WS_Message_Hardware_overview_Board> getOverview() {
        return this.webSocketInterface.ask(new Request(new WS_Message_Hardware_overview().make_request(Collections.singletonList(this.hardware.getId()))))
                .thenApplyAsync(message -> message.as(WS_Message_Hardware_overview.class).get_device_from_list(this.hardware.getId()), this.httpExecutionContext.current());
    }

    public CompletionStage<Message> setHardwareGroups(List<UUID> groupIds, Enum_type_of_command command) {
        return this.webSocketInterface.ask(new Request(new WS_Message_Hardware_set_hardware_groups().make_request(Collections.singletonList(this.hardware), groupIds, command)));
    }

    public CompletionStage<Message> setParameter(String name, String value) {
        return this.webSocketInterface.ask(new Request(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this.hardware), name, value)));
    }

    public CompletionStage<Message> setParameter(String name, Boolean value) {
        return this.webSocketInterface.ask(new Request(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this.hardware), name, value)));
    }

    public CompletionStage<Message> setParameter(String name, Integer value) {
        return this.webSocketInterface.ask(new Request(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this.hardware), name, value)));
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
                .findList();

        // Zaloha kdyby byly stále platné aktualizace na backup
        for (int i = 0; i < firmware_plans.size(); i++) {
            firmware_plans.get(i).state = HardwareUpdateState.OBSOLETE;
            firmware_plans.get(i).update();
        }

        this.setParameter("autobackup", true);
    }

    public CompletionStage<Message> relocate(Model_HomerServer server) {
        return this.relocate(server.server_url, server.mqtt_port.toString());
    }

    public CompletionStage<Message> relocate(String mqttHost, String mqttPort) {
        return this.webSocketInterface.ask(new Request(new WS_Message_Hardware_change_server().make_request(mqttHost, mqttPort, Collections.singletonList(this.hardware.id))));
    }

    public CompletionStage<Message> changeUUIDOnServer(UUID oldId) {
        return this.webSocketInterface.ask(new Request(new WS_Message_Hardware_uuid_converter_cleaner().make_request(this.hardware.id, oldId, this.hardware.full_id)));
    }

    public CompletionStage<Message> changeUUIDOnServer(String oldId) {
        return this.webSocketInterface.ask(new Request(new WS_Message_Hardware_uuid_converter_cleaner().make_request(this.hardware.id, oldId, this.hardware.full_id)));
    }

    public CompletionStage<Message> changeUUIDOnServer(UUID oldId, UUID newId) {
        return this.webSocketInterface.ask(new Request(new WS_Message_Hardware_uuid_converter_cleaner().make_request(newId, oldId, this.hardware.full_id)));
    }

    public CompletionStage<Message> removeUUIDOnServer() {
        return this.webSocketInterface.ask(new Request(new WS_Message_Hardware_uuid_converter_cleaner().make_request(null, this.hardware.id, this.hardware.full_id)));
    }

    public CompletionStage<Message> update(Model_HardwareUpdate update) {
        return this.webSocketInterface.ask(new Request(new WS_Message_Hardware_UpdateProcedure_Command().make_request(Collections.singletonList(update.get_brief_for_update_homer_server()))));
    }
}
