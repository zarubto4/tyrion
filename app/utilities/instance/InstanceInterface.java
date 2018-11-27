package utilities.instance;

import exceptions.FailedMessageException;
import models.Model_Instance;
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

public class InstanceInterface {

    private final Model_Instance instance;
    private final WebSocketInterface webSocketInterface;

    public InstanceInterface(Model_Instance instance, WebSocketInterface webSocketInterface) {
        this.instance = instance;
        this.webSocketInterface = webSocketInterface;
    }

    public NetworkStatus getNetworkStatus() {
        try {
            if (this.instance.current_snapshot_id == null) {
                return NetworkStatus.SHUT_DOWN;
            }

            return this.getStatus().get_status(this.instance.id).status ? NetworkStatus.ONLINE : NetworkStatus.OFFLINE;
        } catch (FailedMessageException e) {
            return NetworkStatus.OFFLINE;
        }
    }

    public WS_Message_Instance_status getStatus() {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Instance_status().make_request(Collections.singletonList(this.instance.id.toString()))));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        } else {
            return response.as(WS_Message_Instance_status.class);
        }
    }

    public WS_Message_Instance_set_program setProgram() {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Instance_set_program().make_request(this.instance.current_snapshot()))); // TODO maybe not current_snapshot()
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        } else {
            return response.as(WS_Message_Instance_set_program.class);
        }
    }

    public WS_Message_Instance_set_hardware setHardware(List<WS_Message_Homer_Hardware_ID_UUID_Pair> hardware) {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Instance_set_hardware().make_request(hardware)));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        } else {
            return response.as(WS_Message_Instance_set_hardware.class);
        }
    }

    public WS_Message_Instance_set_terminals setTerminals(List<UUID> terminalIds) {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Instance_set_terminals().make_request(terminalIds)));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        } else {
            return response.as(WS_Message_Instance_set_terminals.class);
        }
    }

    public WS_Message_Hardware_overview getHardwareOverview() {
        Message response = this.webSocketInterface.sendWithResponse(new Request(new WS_Message_Hardware_overview().make_request(this.instance.getHardwareIds())));
        if (response.isErroneous()) {
            throw new FailedMessageException(response);
        } else {
            return response.as(WS_Message_Hardware_overview.class);
        }
    }
}
