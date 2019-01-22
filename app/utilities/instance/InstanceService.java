package utilities.instance;

import com.google.inject.Inject;
import exceptions.NotFoundException;
import exceptions.ServerOfflineException;
import models.*;
import play.libs.concurrent.HttpExecutionContext;
import utilities.enums.FirmwareType;
import utilities.enums.NetworkStatus;
import utilities.enums.UpdateType;
import utilities.hardware.update.UpdateService;
import utilities.homer.HomerInterface;
import utilities.homer.HomerService;
import utilities.logger.Logger;
import utilities.network.NetworkStatusService;
import utilities.notifications.NotificationService;
import websocket.WebSocketService;
import websocket.interfaces.Homer;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Message_Homer_Hardware_ID_UUID_Pair;

import java.util.*;

public class InstanceService {

    private static final Logger logger = new Logger(InstanceService.class);

    private final WebSocketService webSocketService;
    private final HomerService homerService;
    private final UpdateService updateService;
    private final NetworkStatusService networkStatusService;
    private final NotificationService notificationService;
    private final HttpExecutionContext httpExecutionContext;

    @Inject
    public InstanceService(WebSocketService webSocketService, HomerService homerService, UpdateService updateService,
                           NetworkStatusService networkStatusService, NotificationService notificationService, HttpExecutionContext httpExecutionContext) {
        this.webSocketService = webSocketService;
        this.homerService = homerService;
        this.updateService = updateService;
        this.networkStatusService = networkStatusService;
        this.notificationService = notificationService;
        this.httpExecutionContext = httpExecutionContext;
    }

    public InstanceInterface getInterface(Model_Instance instance) {
        Model_HomerServer server = instance.getServer();

        Homer homer = this.webSocketService.getInterface(server.id);
        if (homer != null) {
            return new InstanceInterface(instance, homer, this.httpExecutionContext);
        } else {
            throw new ServerOfflineException();
        }
    }

    public void deploy(Model_InstanceSnapshot snapshot) {
        this.deploy(snapshot, true);
    }

    public void deploy(Model_InstanceSnapshot snapshot, boolean forceHardwareUpdate) {

        try {

            Model_Instance instance = snapshot.getInstance();
            instance.current_snapshot_id = snapshot.id;
            instance.update();


            HomerInterface homerInterface = this.homerService.getInterface(instance.getServer());

            if(!homerInterface.getInstanceList().instance_ids.contains(instance.id)) {
                homerInterface.addInstance(instance);
            }


            this.networkStatusService.setStatus(instance, NetworkStatus.ONLINE);

            InstanceInterface instanceInterface = this.getInterface(instance);
            instanceInterface.setProgram();

            List<Model_Hardware> hardware = snapshot.getRequiredHardware();

            List<WS_Message_Homer_Hardware_ID_UUID_Pair> pairs = new ArrayList<>();

            hardware.forEach(hw -> {
                WS_Message_Homer_Hardware_ID_UUID_Pair pair = new WS_Message_Homer_Hardware_ID_UUID_Pair();
                pair.full_id = hw.full_id;
                pair.uuid = hw.id.toString(); // Must be string!
                pairs.add(pair);
            });

            instanceInterface.setHardware(pairs);
            // TODO instanceInterface.setTerminals();

            // TODO think if this code shouldn't be elsewhere
            if (forceHardwareUpdate && snapshot.getProgram().interfaces.size() > 0) {
                Map<UUID, List<Model_Hardware>> updates = new HashMap<>();

                snapshot.getProgram().interfaces.forEach(iface -> {
                    if (!updates.containsKey(iface.interface_id)) {
                        updates.put(iface.interface_id, new ArrayList<>());
                    }

                    if (iface.type.equals("hardware")) {
                        try {
                            Model_Hardware hw = Model_Hardware.find.byId(iface.target_id);
                            if (!updates.get(iface.interface_id).contains(hw)) {
                                updates.get(iface.interface_id).add(hw);
                            }
                        } catch (NotFoundException e) {
                            logger.warn("deploy - not found hw, id: {}", iface.target_id);
                        }
                    } else if (iface.type.equals("group")) {
                        try {
                            Model_HardwareGroup group = Model_HardwareGroup.find.byId(iface.target_id);
                            group.getHardware().forEach(hw -> {
                                if (!updates.get(iface.interface_id).contains(hw)) {
                                    updates.get(iface.interface_id).add(hw);
                                }
                            });
                        } catch (NotFoundException e) {
                            logger.warn("deploy - not found hw group, id: {}", iface.target_id);
                        }
                    }
                });

                updates.keySet().forEach(interfaceId -> {
                    try {
                        Model_CProgramVersion version = Model_CProgramVersion.find.byId(interfaceId);

                        this.updateService.bulkUpdate(updates.get(interfaceId), version, FirmwareType.FIRMWARE, UpdateType.MANUALLY_BY_INSTANCE, snapshot.getId());
                    } catch (NotFoundException e) {
                        logger.warn("deploy - not found firmware version, id: {}, skipping update", interfaceId);
                    }
                });
            }

        } catch (ServerOfflineException e) {
            this.notificationService.send(snapshot.getProject(), snapshot.notificationServerOffline());
        }
    }

    public void shutdown(Model_Instance instance) {

        if(instance.current_snapshot() != null) {
            instance.current_snapshot().getRequiredHardware().forEach(hardware -> {
                hardware.connected_instance_id = null;
                hardware.update();
            });
        }

        instance.current_snapshot_id = null;
        this.networkStatusService.setStatus(instance, NetworkStatus.SHUT_DOWN);
        instance.update();

        try {
            HomerInterface homerInterface = this.homerService.getInterface(instance.getServer());
            homerInterface.removeInstance(Collections.singletonList(instance.id));

            this.networkStatusService.setStatus(instance, NetworkStatus.OFFLINE);

        } catch (ServerOfflineException e) {
            // nothing - sever is offline
        }
    }
}
