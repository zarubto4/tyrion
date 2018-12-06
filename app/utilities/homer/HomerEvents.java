package utilities.homer;

import com.google.inject.Inject;
import com.google.inject.Injector;
import models.Model_Hardware;
import models.Model_HomerServer;
import models.Model_Instance;
import utilities.Server;
import utilities.enums.NetworkStatus;
import utilities.enums.ServerMode;
import utilities.network.NetworkStatusService;
import utilities.slack.Slack;
import utilities.synchronization.SynchronizationService;

import java.util.List;

public class HomerEvents {

    private final Injector injector;
    private final NetworkStatusService networkStatusService;
    private final SynchronizationService synchronizationService;

    @Inject
    public HomerEvents(Injector injector, NetworkStatusService networkStatusService, SynchronizationService synchronizationService) {
        this.injector = injector;
        this.networkStatusService = networkStatusService;
        this.synchronizationService = synchronizationService;
    }

    public void connected(Model_HomerServer server) {
        this.networkStatusService.setStatus(server, NetworkStatus.ONLINE);

        HomerSynchronizationTask task = this.injector.getInstance(HomerSynchronizationTask.class);
        task.setServer(server);

        this.synchronizationService.submit(task);
    }

    public void disconnected(Model_HomerServer server) {
        this.networkStatusService.setStatus(server, NetworkStatus.OFFLINE);

        if (Server.mode == ServerMode.STAGE) {
            Slack.homer_server_offline(server); // TODO injection
        }

        List<Model_Instance> instances = Model_Instance.find.query().where().eq("server_main.id", server.id).isNotNull("current_snapshot_id").findList();
        instances.forEach(instance -> this.networkStatusService.setStatus(instance, NetworkStatus.UNKNOWN_LOST_CONNECTION_WITH_SERVER));

        List<Model_Hardware> hardwareList = Model_Hardware.find.query().where().eq("connected_server_id", server.id).eq("dominant_entity", true).findList();
        hardwareList.forEach(hardware -> this.networkStatusService.setStatus(hardware, NetworkStatus.UNKNOWN_LOST_CONNECTION_WITH_SERVER));
    }
}
