package utilities.homer;

import com.google.inject.Inject;
import models.Model_HomerServer;
import utilities.Server;
import utilities.enums.NetworkStatus;
import utilities.enums.ServerMode;
import utilities.network.NetworkStatusService;
import utilities.slack.Slack;

public class HomerEvents {

    private final NetworkStatusService networkStatusService;

    @Inject
    public HomerEvents(NetworkStatusService networkStatusService) {
        this.networkStatusService = networkStatusService;
    }

    public void connected(Model_HomerServer server) {
        server.make_log_connect();
        this.networkStatusService.setStatus(server, NetworkStatus.ONLINE);
    }

    public void disconnected(Model_HomerServer server) {

        server.make_log_disconnect(); // TODO injection

        this.networkStatusService.setStatus(server, NetworkStatus.OFFLINE);

        if (Server.mode == ServerMode.STAGE) {
            Slack.homer_server_offline(server); // TODO injection
        }
    }
}
