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
import mongo.ModelMongo_NetworkStatus;
import org.ehcache.Cache;
import org.mongodb.morphia.query.FindOptions;
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

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public class NetworkStatusService {

    private static final Logger logger = new Logger(NetworkStatusService.class);

    private final Cache<UUID, NetworkStatus> cache;

    private final NotificationService notificationService;
    private final Provider<HardwareService> hardwareServiceProvider;
    private final Provider<InstanceService> instanceServiceProvider;
    private final CompilerService compilerService;
    private final HomerService homerService;

    @Inject
    public NetworkStatusService(CacheService cacheService, Provider<HardwareService> hardwareServiceProvider, Provider<InstanceService> instanceServiceProvider,
                                CompilerService compilerService, HomerService homerService, NotificationService notificationService) {
        this.cache = cacheService.getCache("NetworkStatusCache", UUID.class, NetworkStatus.class, 1000, 3600, true);
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
        if (this.cache.containsKey(networkable.getId())) {
            logger.trace("getStatus - getting status from cache");
            return this.cache.get(networkable.getId());
        } else {
            this.requestStatus(networkable);
            this.cache.put(networkable.getId(), NetworkStatus.SYNCHRONIZATION_IN_PROGRESS);
            return NetworkStatus.SYNCHRONIZATION_IN_PROGRESS;
        }
    }

    public void setStatus(Networkable networkable, NetworkStatus networkStatus) {
        logger.debug("setStatus - setting status: {} for: {}, id: {}", networkStatus, networkable.getEntityType(), networkable.getId());

        this.cache.put(networkable.getId(), networkStatus);

        UUID projectId = null;

        if (networkable instanceof UnderProject) {
            projectId = ((UnderProject) networkable).getProject() != null ? ((UnderProject) networkable).getProject().id : null;
        }

        this.notificationService.networkStatusChanged(networkable.getClass(), networkable.getId(), networkStatus, projectId);

        ModelMongo_NetworkStatus.create_record(networkable, networkStatus);
    }

    // TODO should be async with echo update
    public Long getLastOnline(Networkable networkable) {
        ModelMongo_NetworkStatus networkStatus = ModelMongo_NetworkStatus.find.query()
                .field("networkable_id").equal(networkable.getId().toString())
                .field("status").equal(NetworkStatus.ONLINE)
                .field("server_type").equal(Server.mode)
                .order("created").get(new FindOptions().batchSize(1));

        if (networkStatus != null) {
            return networkStatus.created;
        } else  {
            return null;
        }
    }

    private void requestStatus(Networkable networkable) {
        logger.debug("requestStatus - requesting status for: {}, id: {}", networkable.getEntityType(), networkable.getId());
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        switch (networkable.getEntityType()) {
                            case HARDWARE: {
                                return this.hardwareServiceProvider.get().getInterface((Model_Hardware) networkable).getNetworkStatus();
                            }
                            case INSTANCE: {
                                return this.instanceServiceProvider.get().getInterface((Model_Instance) networkable).getNetworkStatus();
                            }
                            case COMPILER: {
                                try {
                                    this.compilerService.getInterface((Model_CompilationServer) networkable);
                                    return NetworkStatus.ONLINE;
                                } catch (ServerOfflineException e) {
                                    return NetworkStatus.OFFLINE;
                                }
                            }
                            case HOMER: {
                                try {
                                    this.homerService.getInterface((Model_HomerServer) networkable);
                                    return NetworkStatus.ONLINE;
                                } catch (ServerOfflineException e) {
                                    return NetworkStatus.OFFLINE;
                                }
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
