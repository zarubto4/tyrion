package utilities.project;

import com.google.inject.Inject;
import exceptions.FailedMessageException;
import exceptions.NotFoundException;
import exceptions.ServerOfflineException;
import models.Model_Hardware;
import models.Model_HomerServer;
import models.Model_Instance;
import models.Model_Project;
import utilities.enums.NetworkStatus;
import utilities.homer.HomerInterface;
import utilities.homer.HomerService;
import utilities.network.NetworkStatusService;
import utilities.swagger.output.Swagger_ProjectStats;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_online_status;

import java.util.List;
import java.util.UUID;

public class ProjectService {

    private final NetworkStatusService networkStatusService;
    private final HomerService homerService;

    @Inject
    public ProjectService(NetworkStatusService networkStatusService, HomerService homerService) {
        this.networkStatusService = networkStatusService;
        this.homerService = homerService;
    }

    public Swagger_ProjectStats getOverview(Model_Project project) {

        Swagger_ProjectStats stats = new Swagger_ProjectStats();
        stats.hardware = project.getHardware().size();
        stats.b_programs = project.getBProgramsIds().size();
        stats.c_programs = project.getCProgramsIds().size();
        stats.libraries = project.getLibrariesIds().size();
        stats.grid_projects = project.getGridProjectsIds().size();
        stats.hardware_groups = project.getHardwareGroupsIds().size();
        stats.widgets = project.getWidgetsIds().size();
        stats.blocks = project.getBlocksIds().size();
        stats.instances = project.getInstancesIds().size();
        stats.servers = project.getHomerServerIds().size();

        stats.hardware_online = 0;
        stats.instance_online = 0;
        stats.servers_online = 0;

        List<UUID> serverIds = Model_Hardware.find.query().where()
                .eq("project.id", project.getId())
                .eq("dominant_entity", true)
                .isNotNull("connected_server_id")
                .select("connected_server_id")
                .setDistinct(true)
                .findSingleAttributeList();

        for (UUID id : serverIds) {
            try {
                Model_HomerServer server = Model_HomerServer.find.byId(id);
                HomerInterface homerInterface = this.homerService.getInterface(server);
                WS_Message_Hardware_online_status response = homerInterface.device_online_synchronization_ask(Model_Hardware.find.query().where().eq("project.id", id).eq("dominant_entity", true).eq("connected_server_id", id).select("id").findSingleAttributeList());

                for (WS_Message_Hardware_online_status.DeviceStatus status : response.hardware_list) {

                    if (status.online_status) {
                        ++stats.hardware_online;
                    }

                    try {
                        Model_Hardware hardware = Model_Hardware.find.byId(UUID.fromString(status.uuid));
                        this.networkStatusService.setStatus(hardware, status.online_status ? NetworkStatus.ONLINE : NetworkStatus.OFFLINE);
                    } catch (Exception e){
                        // just skip
                    }
                }

            } catch (NotFoundException | ServerOfflineException | FailedMessageException e) {
                // Nothing
            }
        }

        for (Model_HomerServer server : project.getHomerServers()) {
            if (this.networkStatusService.getStatus(server) == NetworkStatus.ONLINE) {
                ++stats.servers_online;
            }
        }

        for (Model_Instance instance : project.getInstances()) {
            if (this.networkStatusService.getStatus(instance) == NetworkStatus.ONLINE) {
                ++stats.instance_online;
            }
        }

        return stats;
    }

    public void activate(Model_Project project) {}

    public void deactivate(Model_Project project) {}
}
