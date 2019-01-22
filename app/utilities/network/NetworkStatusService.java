package utilities.network;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import exceptions.NeverConnectedException;
import exceptions.NotSupportedException;
import exceptions.ServerOfflineException;
import models.Model_CompilationServer;
import models.Model_Hardware;
import models.Model_HomerServer;
import models.Model_Instance;
import mongo.ModelMongo_LastOnline;
import mongo.ModelMongo_NetworkStatus;
import org.ehcache.Cache;
import xyz.morphia.query.FindOptions;
import play.libs.concurrent.HttpExecutionContext;
import utilities.Server;
import utilities.cache.CacheService;
import utilities.compiler.CompilerService;
import utilities.enums.NetworkStatus;
import utilities.hardware.HardwareService;
import utilities.homer.HomerService;
import utilities.instance.InstanceService;
import utilities.logger.Logger;
import utilities.model.UnderProject;
import utilities.notifications.NotificationService;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public class NetworkStatusService {

    private static final Logger logger = new Logger(NetworkStatusService.class);

    private final Cache<UUID, NetworkStatus> networkStatusCache;
    private final Cache<UUID, LocalDateTime> lastOnlineCache;

    private final HttpExecutionContext httpExecutionContext;
    private final NotificationService notificationService;
    private final Provider<HardwareService> hardwareServiceProvider;
    private final Provider<InstanceService> instanceServiceProvider;
    private final CompilerService compilerService;
    private final HomerService homerService;

    @Inject
    public NetworkStatusService(CacheService cacheService, Provider<HardwareService> hardwareServiceProvider, Provider<InstanceService> instanceServiceProvider,
                                CompilerService compilerService, HomerService homerService, NotificationService notificationService, HttpExecutionContext httpExecutionContext) {

        this.networkStatusCache = cacheService.getCache("NetworkStatusCache", UUID.class, NetworkStatus.class, 1000, 3600, true);
        this.lastOnlineCache = cacheService.getCache("LastOnlineCache", UUID.class, LocalDateTime.class, 1000, 3600, true);

        this.httpExecutionContext = httpExecutionContext;
        this.notificationService = notificationService;
        this.hardwareServiceProvider = hardwareServiceProvider;
        this.instanceServiceProvider = instanceServiceProvider;
        this.compilerService = compilerService;
        this.homerService = homerService;
    }

    /**
     * Get either cached status or request for the status.
     * @param networkable for which the status is gotten
     * @return status
     */
    public NetworkStatus getStatus(Networkable networkable) {
        logger.debug("getStatus - getting status for: {}, id: {}", networkable.getEntityType(), networkable.getId());
        if (this.networkStatusCache.containsKey(networkable.getId())) {
            logger.trace("getStatus - getting status from cache");
            return this.networkStatusCache.get(networkable.getId());
        } else {
            return this.requestStatus(networkable);
        }
    }

    public void setStatus(Networkable networkable, NetworkStatus networkStatus) {
        logger.debug("setStatus - setting status: {} for: {}, id: {}", networkStatus, networkable.getEntityType(), networkable.getId());

        if (this.networkStatusCache.containsKey(networkable.getId())) {
            if (this.networkStatusCache.get(networkable.getId()).equals(NetworkStatus.ONLINE) && !networkStatus.equals(NetworkStatus.ONLINE)) {
                CompletableFuture.runAsync(() -> {
                    ModelMongo_LastOnline lastOnline = ModelMongo_LastOnline.create_record(networkable);
                    this.setLastOnline(networkable, lastOnline.created);
                }, httpExecutionContext.current());
            }
        } else {
            CompletableFuture.runAsync(() -> {
                ModelMongo_NetworkStatus mongoNetworkStatus = ModelMongo_NetworkStatus.find.query()
                        .field("networkable_id").equal(networkable.getId().toString())
                        .field("server_type").equal(Server.mode)
                        .order("created").get(new FindOptions().batchSize(1));

                if (mongoNetworkStatus != null && mongoNetworkStatus.status.equals(NetworkStatus.ONLINE) && !networkStatus.equals(NetworkStatus.ONLINE)) {
                    ModelMongo_LastOnline lastOnline = ModelMongo_LastOnline.create_record(networkable);
                    this.setLastOnline(networkable, lastOnline.created);
                }
            }, httpExecutionContext.current());
        }

        this.networkStatusCache.put(networkable.getId(), networkStatus);

        ModelMongo_NetworkStatus.create_record(networkable, networkStatus);

        UUID projectId = null;

        if (networkable instanceof UnderProject) {
            projectId = ((UnderProject) networkable).getProject() != null ? ((UnderProject) networkable).getProject().id : null;
        }

        this.notificationService.networkStatusChanged(networkable.getClass(), networkable.getId(), networkStatus, projectId);
    }

    public LocalDateTime getLastOnline(Networkable networkable) {
        logger.debug("getLastOnline - getting last online for: {}, id: {}", networkable.getEntityType(), networkable.getId());
        if (this.lastOnlineCache.containsKey(networkable.getId())) {
            logger.trace("getLastOnline - getting last online from cache");
            return this.lastOnlineCache.get(networkable.getId());
        } else {
            this.lastOnlineCache.put(networkable.getId(), LocalDateTime.MIN);
            this.requestLastOnline(networkable);
            return LocalDateTime.MIN;
        }
    }

    private NetworkStatus requestStatus(Networkable networkable) {
        logger.debug("requestStatus - requesting status for: {}, id: {}", networkable.getEntityType(), networkable.getId());

        NetworkStatus networkStatus = NetworkStatus.SYNCHRONIZATION_IN_PROGRESS;

        try {
            switch (networkable.getEntityType()) {
                case HARDWARE: {
                    if (!((Model_Hardware) networkable).dominant_entity) {
                        networkStatus = NetworkStatus.FREEZED;
                    } else {
                        this.hardwareServiceProvider.get().getInterface((Model_Hardware) networkable).getNetworkStatusAsync()
                                .whenComplete((status, exception) -> {
                                    if (exception != null) {
                                        logger.internalServerError(exception);
                                    } else {
                                        this.setStatus(networkable, status);
                                    }
                                });
                    }
                    break;
                }
                case INSTANCE: {
                    if (((Model_Instance) networkable).current_snapshot_id == null) {
                        networkStatus = NetworkStatus.SHUT_DOWN;
                    } else {
                        this.instanceServiceProvider.get().getInterface((Model_Instance) networkable).getNetworkStatusAsync()
                                .whenComplete((status, exception) -> {
                                    if (exception != null) {
                                        logger.internalServerError(exception);
                                    } else {
                                        this.setStatus(networkable, status);
                                    }
                                });
                    }
                    break;
                }
                case COMPILER: {
                    try {
                        this.compilerService.getInterface((Model_CompilationServer) networkable);
                        networkStatus = NetworkStatus.ONLINE;
                    } catch (ServerOfflineException e) {
                        networkStatus = NetworkStatus.OFFLINE;
                    }
                    break;
                }
                case HOMER: {
                    try {
                        this.homerService.getInterface((Model_HomerServer) networkable);
                        networkStatus = NetworkStatus.ONLINE;
                    } catch (ServerOfflineException e) {
                        networkStatus = NetworkStatus.OFFLINE;
                    }
                    break;
                }
                default: throw new NotSupportedException();
            }
        } catch (ServerOfflineException e) {
            logger.warn("requestStatus - server is offline");
            networkStatus = NetworkStatus.UNKNOWN_LOST_CONNECTION_WITH_SERVER;
        } catch (NeverConnectedException e) {
            networkStatus = NetworkStatus.NOT_YET_FIRST_CONNECTED;
        } catch (Exception e) {
            logger.internalServerError(e);
            networkStatus = NetworkStatus.UNKNOWN_LOST_CONNECTION_WITH_SERVER;
        }

        this.networkStatusCache.put(networkable.getId(), networkStatus);
        return networkStatus;
    }

    private void requestLastOnline(Networkable networkable) {
        logger.debug("requestLastOnline - requesting last online for: {}, id: {}", networkable.getEntityType(), networkable.getId());
        CompletableFuture.supplyAsync(() -> {

            try {
                ModelMongo_LastOnline lastOnline = ModelMongo_LastOnline.find.query()
                        .field("networkable_id").equal(networkable.getId().toString())
                        .field("server_type").equal(Server.mode)
                        .order("created").get(new FindOptions().batchSize(1));

                if (lastOnline != null) {
                    return lastOnline.created;
                }
            } catch (Exception e) {
                logger.internalServerError(e);
            }

            return LocalDateTime.MIN;

        }).thenAccept(last -> this.setLastOnline(networkable, last));
    }

    private void setLastOnline(Networkable networkable, LocalDateTime lastOnline) {
        logger.debug("setLastOnline - setting last online: {} for: {}, id: {}", lastOnline, networkable.getEntityType(), networkable.getId());

        this.lastOnlineCache.put(networkable.getId(), lastOnline);

        if (networkable instanceof UnderProject) {
            UUID projectId = ((UnderProject) networkable).getProject() != null ? ((UnderProject) networkable).getProject().id : null;
            if (projectId != null) {
                this.notificationService.modelUpdated(networkable.getClass(), networkable.getId(), projectId);
            }
        }
    }
}
