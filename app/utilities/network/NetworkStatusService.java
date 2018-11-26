package utilities.network;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import exceptions.NeverConnectedException;
import exceptions.NotSupportedException;
import exceptions.ServerOfflineException;
import models.Model_Hardware;
import org.ehcache.Cache;
import utilities.cache.CacheService;
import utilities.enums.NetworkStatus;
import utilities.hardware.HardwareService;
import utilities.logger.Logger;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public class NetworkStatusService {

    private static final Logger logger = new Logger(NetworkStatusService.class);

    private final Cache<UUID, NetworkStatus> cache;

    private final HardwareService hardwareService;

    @Inject
    public NetworkStatusService(CacheService cacheService, HardwareService hardwareService) {
        this.cache = cacheService.getCache("NetworkStatusCache", UUID.class, NetworkStatus.class, 1000, 3600, true);
        this.hardwareService = hardwareService;
    }

    /**
     * Get either cached status or request for the status.
     * @param networkable for which the status is gotten
     * @return status
     */
    public NetworkStatus getStatus(Networkable networkable) {
        if (this.cache.containsKey(networkable.getId())) {
            return this.cache.get(networkable.getId());
        } else {
            this.requestStatus(networkable);
            this.cache.put(networkable.getId(), NetworkStatus.SYNCHRONIZATION_IN_PROGRESS);
            return NetworkStatus.SYNCHRONIZATION_IN_PROGRESS;
        }
    }

    public void setStatus(Networkable networkable, NetworkStatus networkStatus) {
        this.cache.put(networkable.getId(), networkStatus);
        // TODO send it to Becki via WebSocket
    }

    // TODO
    public Long getLastOnline(Networkable networkable) {
        return 0L;
    }

    private void requestStatus(Networkable networkable) {
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        switch (networkable.getEntityType()) {
                            case HARDWARE: {
                                return this.hardwareService.getInterface((Model_Hardware) networkable).getNetworkStatus();
                            }
                            default: throw new NotSupportedException();
                        }
                    } catch (ServerOfflineException e) {
                        return NetworkStatus.UNKNOWN_LOST_CONNECTION_WITH_SERVER;
                    } catch (NeverConnectedException e) {
                        return NetworkStatus.NOT_YET_FIRST_CONNECTED;
                    } catch (Exception e) {
                        logger.internalServerError(e);
                        return NetworkStatus.UNKNOWN_LOST_CONNECTION_WITH_SERVER;
                    }
                })
                .thenAccept(status -> this.setStatus(networkable, status));
    }
}
