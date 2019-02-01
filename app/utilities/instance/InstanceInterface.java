package utilities.instance;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Instance;
import play.libs.concurrent.HttpExecutionContext;
import utilities.enums.NetworkStatus;
import websocket.Message;
import websocket.Request;
import websocket.WebSocketInterface;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_overview;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Message_Homer_Hardware_ID_UUID_Pair;
import websocket.messages.homer_instance_with_tyrion.WS_Message_Instance_set_hardware;
import websocket.messages.homer_instance_with_tyrion.WS_Message_Instance_set_program;
import websocket.messages.homer_instance_with_tyrion.WS_Message_Instance_set_terminals;
import websocket.messages.homer_instance_with_tyrion.WS_Message_Instance_status;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class InstanceInterface {

    private final Model_Instance instance;
    private final WebSocketInterface webSocketInterface;
    private final HttpExecutionContext httpExecutionContext;

    public InstanceInterface(Model_Instance instance, WebSocketInterface webSocketInterface, HttpExecutionContext httpExecutionContext) {
        this.instance = instance;
        this.webSocketInterface = webSocketInterface;
        this.httpExecutionContext = httpExecutionContext;
    }

    private CompletionStage<Message> ask(ObjectNode message) {
        if (!message.has("instance_id")) {
            message.put("instance_id", this.instance.getId().toString());
        }
        return this.webSocketInterface.ask(new Request(message));
    }

    public CompletionStage<NetworkStatus> getNetworkStatus() {
        return this.ask(new WS_Message_Instance_status().make_request(Collections.singletonList(this.instance.id.toString())))
                .thenApplyAsync(message -> {
                    WS_Message_Instance_status response = message.as(WS_Message_Instance_status.class);
                    return response.get_status(this.instance.id).status ? NetworkStatus.ONLINE : NetworkStatus.OFFLINE;
                }, this.httpExecutionContext.current());
    }

    public CompletionStage<WS_Message_Instance_status> getStatus() {
        return this.ask(new WS_Message_Instance_status().make_request(Collections.singletonList(this.instance.id.toString())))
                .thenApplyAsync(message -> message.as(WS_Message_Instance_status.class));
    }

    public CompletionStage<WS_Message_Instance_set_program> setProgram() {
        return this.ask(new WS_Message_Instance_set_program().make_request(this.instance.current_snapshot())) // TODO maybe not current_snapshot()
                .thenApplyAsync(message -> message.as(WS_Message_Instance_set_program.class));
    }

    public CompletionStage<WS_Message_Instance_set_hardware> setHardware(List<WS_Message_Homer_Hardware_ID_UUID_Pair> hardware) {
        return this.ask(new WS_Message_Instance_set_hardware().make_request(hardware))
                .thenApplyAsync(message -> message.as(WS_Message_Instance_set_hardware.class));
    }

    public CompletionStage<WS_Message_Instance_set_terminals> setTerminals(List<UUID> terminalIds) {
        return this.ask(new WS_Message_Instance_set_terminals().make_request(terminalIds))
                .thenApplyAsync(message -> message.as(WS_Message_Instance_set_terminals.class));
    }

    public CompletionStage<WS_Message_Hardware_overview> getHardwareOverview() {
        return this.ask(new WS_Message_Hardware_overview().make_request(this.instance.getHardwareIds()))
                .thenApplyAsync(message -> message.as(WS_Message_Hardware_overview.class));
    }
}
