package utilities.homer;

import exceptions.FailedMessageException;
import models.Model_HardwareUpdate;
import models.Model_HomerServer;
import models.Model_Instance;
import websocket.Message;
import websocket.Request;
import websocket.WebSocketInterface;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_online_status;
import websocket.messages.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Command;
import websocket.messages.homer_with_tyrion.*;
import websocket.messages.homer_with_tyrion.configuration.WS_Message_Homer_Get_homer_server_configuration;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class HomerInterface {

    private final Model_HomerServer server;
    private final WebSocketInterface webSocketInterface;

    public HomerInterface(Model_HomerServer server, WebSocketInterface webSocketInterface) {
        this.server = server;
        this.webSocketInterface = webSocketInterface;
    }

    public WS_Message_Homer_ping ping() {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Homer_ping().make_request()));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        } else {
            return response.as(WS_Message_Homer_ping.class);
        }
    }

    public WS_Message_Homer_Get_homer_server_configuration getOverview() {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Homer_Get_homer_server_configuration().make_request()));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        } else {
            return response.as(WS_Message_Homer_Get_homer_server_configuration.class);
        }
    }

    public void bulkUpdate(List<Model_HardwareUpdate> updates) {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Hardware_UpdateProcedure_Command().make_request(updates.stream().map(u -> u.get_brief_for_update_homer_server()).collect(Collectors.toList()))));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        }
    }

    // TODO is this needed?
    public WS_Message_Hardware_online_status device_online_synchronization_ask(List<UUID> list) {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Hardware_online_status().make_request(list)));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        } else {
            return response.as(WS_Message_Hardware_online_status.class);
        }
    }

    public WS_Message_Homer_Instance_list getInstanceList() {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Homer_Instance_list().make_request()));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        } else {
            return response.as(WS_Message_Homer_Instance_list.class);
        }
    }

    public WS_Message_Homer_Hardware_list getHardwareList() {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Homer_Hardware_list().make_request()));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        } else {
            return response.as(WS_Message_Homer_Hardware_list.class);
        }
    }

    public WS_Message_Homer_Instance_number getInstanceCount() {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Homer_Instance_number().make_request()));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        } else {
            return response.as(WS_Message_Homer_Instance_number.class);
        }
    }

    public WS_Message_Homer_Instance_add addInstance(Model_Instance instance) {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Homer_Instance_add().make_request(instance.id)));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        } else {
            return response.as(WS_Message_Homer_Instance_add.class);
        }
    }

    public WS_Message_Homer_Instance_destroy removeInstance(List<UUID> instance_ids) {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Homer_Instance_destroy().make_request(instance_ids)));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        } else {
            return response.as(WS_Message_Homer_Instance_destroy.class);
        }
    }
}
