package utilities.hardware;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import exceptions.NeverConnectedException;
import exceptions.ServerOfflineException;
import models.Model_Hardware;
import org.ehcache.Cache;
import play.libs.concurrent.HttpExecutionContext;
import utilities.cache.CacheService;
import utilities.logger.Logger;
import utilities.model.EchoService;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_overview_Board;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public class HardwareOverviewService {

    private static final Logger logger = new Logger(HardwareOverviewService.class);

    private final HttpExecutionContext httpExecutionContext;
    private final HardwareService hardwareService;
    private final EchoService echoService;

    private final Cache<UUID, WS_Message_Hardware_overview_Board> cache;

    @Inject
    public HardwareOverviewService(CacheService cacheService, HardwareService hardwareService, HttpExecutionContext httpExecutionContext, EchoService echoService) {
        this.cache = cacheService.getCache("HardwareOverview", UUID.class, WS_Message_Hardware_overview_Board.class, 500, 86400, true);
        this.echoService = echoService;
        this.hardwareService = hardwareService;
        this.httpExecutionContext = httpExecutionContext;
    }

    public synchronized WS_Message_Hardware_overview_Board getOverview(Model_Hardware hardware) {
        logger.debug("getOverview - getting overview for hardware: {}", hardware.getId());
        if (this.cache.containsKey(hardware.getId())) {
            return this.cache.get(hardware.getId());
        } else {
            this.requestOverview(hardware);
            return null;
        }
    }

    public synchronized void invalidate(UUID id) {
        this.cache.remove(id);
    }

    private synchronized void setOverview(Model_Hardware hardware, WS_Message_Hardware_overview_Board overview) {
        if (overview != null) {
            logger.debug("getOverview - setting overview for hardware: {}", hardware.getId());
            this.cache.put(hardware.getId(), overview);
            // TODO this.echoService.onUpdated(hardware);
        }
    }

    private void requestOverview(Model_Hardware hardware) {
        logger.debug("requestOverview - requesting overview for hardware: {}", hardware.getId());
        try {
            this.hardwareService.getInterface(hardware).getOverviewAsync()
                    .thenAccept(overview -> this.setOverview(hardware, overview));
        } catch (ServerOfflineException|NeverConnectedException e) {
            // nothing
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }
}
