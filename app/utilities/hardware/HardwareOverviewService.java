package utilities.hardware;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import exceptions.FailedMessageException;
import exceptions.NeverConnectedException;
import exceptions.ServerOfflineException;
import models.Model_Hardware;
import org.ehcache.Cache;
import utilities.cache.CacheService;
import utilities.logger.Logger;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_overview_Board;

import java.util.UUID;

@Singleton
public class HardwareOverviewService {

    private static final Logger logger = new Logger(HardwareOverviewService.class);

    private final HardwareService hardwareService;
    private final Cache<UUID, WS_Message_Hardware_overview_Board> cache;

    @Inject
    public HardwareOverviewService(CacheService cacheService, HardwareService hardwareService) {
        this.cache = cacheService.getCache("HardwareOverview", UUID.class, WS_Message_Hardware_overview_Board.class, 500, 86400, true);
        this.hardwareService = hardwareService;
    }

    public synchronized WS_Message_Hardware_overview_Board getOverview(Model_Hardware hardware) {
        try {
            if (!this.cache.containsKey(hardware.getId())) {
                this.cache.put(hardware.getId(), this.hardwareService.getInterface(hardware).getOverview());
            }
            return this.cache.get(hardware.getId());
        } catch (FailedMessageException e) {
            logger.internalServerError(e);
            return null;
        } catch (ServerOfflineException | NeverConnectedException e) {
            return null;
        }
    }

    public synchronized void invalidate(UUID id) {
        this.cache.remove(id);
    }

    // TODO this class should keep cached state of the hardware for frontend usage, think about robust solution (cache invalidation, etc.)
}
