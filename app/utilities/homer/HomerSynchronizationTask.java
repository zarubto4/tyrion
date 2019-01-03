package utilities.homer;

import com.google.inject.Inject;
import exceptions.FailedMessageException;
import models.Model_Hardware;
import models.Model_HomerServer;
import models.Model_Instance;
import play.libs.concurrent.HttpExecutionContext;
import utilities.enums.NetworkStatus;
import utilities.hardware.DominanceService;
import utilities.hardware.HardwareEvents;
import utilities.hardware.HardwareInterface;
import utilities.hardware.HardwareService;
import utilities.instance.InstanceService;
import utilities.logger.Logger;
import utilities.network.NetworkStatusService;
import utilities.synchronization.Task;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Message_Homer_Hardware_ID_UUID_Pair;
import websocket.messages.homer_with_tyrion.WS_Message_Homer_Hardware_list;
import websocket.messages.homer_with_tyrion.configuration.WS_Message_Homer_Get_homer_server_configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class HomerSynchronizationTask implements Task {
    
    private static final Logger logger = new Logger(HomerSynchronizationTask.class);

    private final NetworkStatusService networkStatusService;
    private final HttpExecutionContext httpExecutionContext;
    private final HomerService homerService;
    private final HardwareEvents hardwareEvents;
    private final InstanceService instanceService;
    private final DominanceService dominanceService;
    private final HardwareService hardwareService;

    private Model_HomerServer server;
    private HomerInterface homerInterface;

    private CompletableFuture<Void> future;

    @Inject
    public HomerSynchronizationTask(HomerService homerService, HardwareEvents hardwareEvents, InstanceService instanceService,
                                    HttpExecutionContext httpExecutionContext, DominanceService dominanceService,
                                    NetworkStatusService networkStatusService, HardwareService hardwareService) {
        this.networkStatusService = networkStatusService;
        this.httpExecutionContext = httpExecutionContext;
        this.homerService = homerService;
        this.hardwareEvents = hardwareEvents;
        this.instanceService = instanceService;
        this.dominanceService = dominanceService;
        this.hardwareService = hardwareService;
    }

    @Override
    public UUID getId() {
        return this.server.getId();
    }

    @Override
    public CompletionStage<Void> start() {
        logger.info("start - synchronization task begins");
        return future = CompletableFuture.runAsync(() -> {
            if (this.server == null || this.homerInterface == null) {
                throw new RuntimeException("You must set server before the task start.");
            }

            this.synchronizeSettings();
            this.synchronizeHardware();
            this.synchronizeInstances();

        }, this.httpExecutionContext.current());
    }

    @Override
    public void stop() {
        this.future.cancel(true);
    }

    public void setServer(Model_HomerServer server) {
        if (this.server == null) {
            this.server = server;
            this.homerInterface = this.homerService.getInterface(this.server);
        } else {
            throw new RuntimeException("Cannot set server twice.");
        }
    }

    private void synchronizeSettings() {
        try {

            logger.info("synchronizeSettings - synchronizing settings for server: {}", this.server.name);

            WS_Message_Homer_Get_homer_server_configuration overview = this.homerInterface.getOverview();

            if (server.mqtt_port != overview.mqtt_port || server.grid_port != overview.grid_port || server.web_view_port != overview.web_view_port ||  server.hardware_logger_port != overview.hw_logger_port ||  server.rest_api_port != overview.rest_api_port) {
                server.mqtt_port = overview.mqtt_port;   // 1881
                server.grid_port = overview.grid_port;   // 8503
                server.web_view_port = overview.web_view_port;  //8501
                server.hardware_logger_port = overview.hw_logger_port; // 8505
                server.rest_api_port = overview.rest_api_port; // 3000
                server.update();
            }

            if (server.server_version == null || !server.server_version.equals(overview.server_version)) {
                server.server_version = overview.server_version;
                server.update();
            }

            if (server.server_url == null ||  !server.server_url.equals(overview.server_url)) {
                server.server_url = overview.server_url;
                server.update();
            }

            logger.trace("synchronizeSettings - synchronization is done for server: {}", this.server.name);

        } catch (FailedMessageException e) {
            logger.internalServerError(e);
        }
    }

    private void synchronizeHardware() {
        try {

            logger.info("synchronizeHardware - synchronizing hardware on server: {}", this.server.name);

            WS_Message_Homer_Hardware_list hardwareList = this.homerInterface.getHardwareList();

            List<WS_Message_Homer_Hardware_ID_UUID_Pair> device_ids_on_server = hardwareList.list;
            logger.info("4. Number of registered or connected Devices on Server:: {} ", device_ids_on_server.size());

            for (WS_Message_Homer_Hardware_ID_UUID_Pair pair : device_ids_on_server) {
                try {

                    Model_Hardware hardware = this.dominanceService.getDominant(pair.full_id);

                    // Device je autorizován pro připojení, ale není k němu aktuálně žádná aktivní virtual entita
                    // s nastavenou dominancí
                    if (hardware == null) {
                        logger.info("synchronizeHardware - dominant hardware for id: {} not found", pair.full_id);
                        continue;
                    }

                    if (hardware.connected_server_id == null || !hardware.connected_server_id.equals(this.server.id)) {
                        logger.debug("synchronizeHardware - setting connected server for hardware,id {}", hardware.id);
                        hardware.connected_server_id = this.server.id;
                        hardware.update();
                    }

                    HardwareInterface hardwareInterface = this.hardwareService.getInterface(hardware);

                    // Homer server neměl spojení s Tyrionem a tak dočasně přiřadil uuid jako full id - proto hned zaměním
                    if (pair.uuid.length() < 25) {
                        logger.warn("synchronizeHardware - homer temporally assigned full id: {} as main id for hardware, id {}", pair.uuid, hardware.id);
                        hardwareInterface.changeUUIDOnServerAsync(pair.uuid)
                                .thenAccept(message -> {
                                    if (pair.online_state) {
                                        this.hardwareEvents.connected(hardware);
                                    } else {
                                        this.networkStatusService.setStatus(hardware, NetworkStatus.OFFLINE);
                                    }
                                });
                        continue;
                    }

                    // Zařízení má přiřazenou jinou UUID k Full ID než by měl mít
                    if (!hardware.id.equals(UUID.fromString(pair.uuid))) {
                        logger.warn("synchronizeHardware - homer temporally assigned random id: {} for hardware, id {}", pair.uuid, hardware.id);
                        hardwareInterface.changeUUIDOnServerAsync(UUID.fromString(pair.uuid))
                                .thenAccept(message -> {
                                    if (pair.online_state) {
                                        this.hardwareEvents.connected(hardware);
                                    } else {
                                        this.networkStatusService.setStatus(hardware, NetworkStatus.OFFLINE);
                                    }
                                });
                        continue;
                    }

                    if (pair.online_state) {
                        this.hardwareEvents.connected(hardware);
                    } else {
                        this.networkStatusService.setStatus(hardware, NetworkStatus.OFFLINE);
                    }

                } catch (Exception e) {
                    logger.internalServerError(e);
                }
            }

        } catch (FailedMessageException e) {
            logger.warn("synchronizeHardware - got error response: {}", e.getFailedMessage().getMessage());
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    private void synchronizeInstances() {
        try {

            logger.info("synchronizeInstances - synchronizing instances on server: {}", this.server.name);

            List<UUID> instances_required_by_tyrion = Model_Instance.find.query().where()
                    .eq("server_main.id", this.server.id)
                    .isNotNull("current_snapshot_id")
                    .eq("deleted", false)
                    .select("id")
                    .findSingleAttributeList();

            List<UUID> instances_actual_on_server = this.homerInterface.getInstanceList().instance_ids;

            List<UUID> instances_for_removing = new ArrayList<>();
            List<UUID> instances_for_add = new ArrayList<>();

            for (UUID instance_id : instances_required_by_tyrion) {
                if (!instances_actual_on_server.contains(instance_id)) {
                    instances_for_add.add(instance_id);
                }
            }

            for (UUID instance_id : instances_actual_on_server) {
                if (!instances_required_by_tyrion.contains(instance_id)) {
                    instances_for_removing.add(instance_id);
                }
            }

            if (!instances_for_removing.isEmpty()) {
                try {
                    this.homerInterface.removeInstance(instances_for_removing);
                } catch (FailedMessageException e) {
                    logger.warn("synchronizeInstances - failed to remove instances");
                }
            }

            if (!instances_for_add.isEmpty()) {
                for (UUID instance_id : instances_for_add) {
                    try {
                        this.instanceService.deploy(Model_Instance.find.byId(instance_id).current_snapshot());
                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }
            }

            logger.trace("synchronizeInstances - synchronization of instances is done for server: {}", this.server.name);

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }
}
