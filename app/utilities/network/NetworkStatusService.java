package utilities.network;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.ehcache.Cache;
import utilities.cache.CacheService;
import utilities.enums.NetworkStatus;
import websocket.WebSocketService;

import java.util.UUID;

@Singleton
public class NetworkStatusService {

    private final WebSocketService webSocketService;

    private final Cache<UUID, NetworkStatus> cache;

    @Inject
    public NetworkStatusService(WebSocketService webSocketService, CacheService cacheService) {
        this.webSocketService = webSocketService;
        this.cache = cacheService.getCache("NetworkStatusCache", UUID.class, NetworkStatus.class, 1000, 3600, true);
    }

    public NetworkStatus getStatus() {
        return null;
    }
}
