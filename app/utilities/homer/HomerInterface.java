package utilities.homer;

import models.Model_HardwareUpdate;
import models.Model_HomerServer;
import models.Model_Instance;
import play.libs.concurrent.HttpExecutionContext;
import websocket.Message;
import websocket.Request;
import websocket.WebSocketInterface;
import websocket.messages.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Command;
import websocket.messages.homer_with_tyrion.*;
import websocket.messages.homer_with_tyrion.configuration.WS_Message_Homer_Get_homer_server_configuration;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class HomerInterface {

    private final Model_HomerServer server;
    private final WebSocketInterface webSocketInterface;
    private final HttpExecutionContext httpExecutionContext;

    public HomerInterface(Model_HomerServer server, WebSocketInterface webSocketInterface, HttpExecutionContext httpExecutionContext) {
        this.server = server;
        this.webSocketInterface = webSocketInterface;
        this.httpExecutionContext = httpExecutionContext;
    }

    public CompletionStage<WS_Message_Homer_Get_homer_server_configuration> getOverview() {
        return this.webSocketInterface.ask(new Request(new WS_Message_Homer_Get_homer_server_configuration().make_request()))
                .thenApplyAsync(message -> message.as(WS_Message_Homer_Get_homer_server_configuration.class), this.httpExecutionContext.current());
    }

    public CompletionStage<Message> bulkUpdate(List<Model_HardwareUpdate> updates) {
        return this.webSocketInterface.ask(new Request(new WS_Message_Hardware_UpdateProcedure_Command().make_request(updates.stream().map(Model_HardwareUpdate::get_brief_for_update_homer_server).collect(Collectors.toList()))));
    }

    public CompletionStage<WS_Message_Homer_Instance_list> getInstanceList() {
        return this.webSocketInterface.ask(new Request(new WS_Message_Homer_Instance_list().make_request()))
                .thenApplyAsync(message -> message.as(WS_Message_Homer_Instance_list.class), this.httpExecutionContext.current());
    }

    public CompletionStage<WS_Message_Homer_Hardware_list> getHardwareList() {
        return this.webSocketInterface.ask(new Request(new WS_Message_Homer_Hardware_list().make_request()))
                .thenApplyAsync(message -> message.as(WS_Message_Homer_Hardware_list.class), this.httpExecutionContext.current());
    }

    public CompletionStage<WS_Message_Homer_Instance_add> addInstance(Model_Instance instance) {
        return this.webSocketInterface.ask(new Request(new WS_Message_Homer_Instance_add().make_request(instance.id)))
                .thenApplyAsync(message -> message.as(WS_Message_Homer_Instance_add.class), this.httpExecutionContext.current());
    }

    public CompletionStage<WS_Message_Homer_Instance_destroy> removeInstance(List<UUID> instance_ids) {
        return this.webSocketInterface.ask(new Request(new WS_Message_Homer_Instance_destroy().make_request(instance_ids)))
                .thenApplyAsync(message -> message.as(WS_Message_Homer_Instance_destroy.class), this.httpExecutionContext.current());
    }
}
