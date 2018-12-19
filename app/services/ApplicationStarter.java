package services;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import javax.inject.*;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.typesafe.config.Config;
import mongo.MongoDBConnector;
import play.api.db.evolutions.ApplicationEvolutions;
import play.inject.ApplicationLifecycle;
import play.libs.Json;
import utilities.Server;
import utilities.cache.CacheService;
import utilities.enums.ServerMode;
import utilities.logger.ServerLogger;
import utilities.model.DateSerializer;
import utilities.permission.PermissionFilter;
import common.InjectedHandlerInstantiator;
import utilities.permission.PermissionService;
import utilities.scheduler.SchedulerService;

/**
 * This class demonstrates how to run code when the
 * application starts and stops. It starts a timer when the
 * application starts. When the application stops it prints out how
 * long the application was running for.
 *
 * This class is registered for Guice dependency injection in the
 * {@link Module} class. We want the class to start when the application
 * starts, so it is registered as an "eager singleton". See the code
 * in the {@link Module} class to see how this happens.
 *
 * This class needs to run code when the server stops. It uses the
 * application's {@link ApplicationLifecycle} to register a stop hook.
 */
@Singleton
public class ApplicationStarter {
    
    private final Clock clock;
    private final ApplicationLifecycle appLifecycle;
    private final Config configuration;
    private final Instant start;

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("application");

    @Inject
    public ApplicationStarter(Clock clock, ApplicationLifecycle appLifecycle, Config configuration, Injector injector,
                              ApplicationEvolutions applicationEvolutions, ServerLogger serverLogger, CacheService cacheService,
                              MongoDBConnector mongoDBConnector, SchedulerService schedulerService, PermissionService permissionService) { // These unused parameters are important due to DI - don't remove them!

        this.clock = clock;
        this.appLifecycle = appLifecycle;
        this.configuration = configuration;
        try {

            // TODO ugly!!! should be completely reworked to use DI
            Server.configuration = configuration;
            Server.mode = configuration.getEnum(ServerMode.class,"server.mode");

            Json.mapper()
                    .setFilterProvider(new SimpleFilterProvider().addFilter("permission", injector.getInstance(PermissionFilter.class)))
                    .registerModule(new SimpleModule().addSerializer(Date.class, new DateSerializer())) // Override Date.class serialization
                    .setHandlerInstantiator(injector.getInstance(InjectedHandlerInstantiator.class)); // For dependency injected serializers

            Server.start(injector);

        } catch (Exception e) {
            logger.error("Error starting the application", e);
            System.exit(1);
        }

        // This code is called when the application starts.
        start = clock.instant();
        logger.info("ApplicationTimer: Starting application at " + start);

        // When the application starts, register a stop hook with the
        // ApplicationLifecycle object. The code inside the stop hook will
        // be run when the application stops.
        appLifecycle.addStopHook(() -> {
            Instant stop = clock.instant();
            Long runningTime = stop.getEpochSecond() - start.getEpochSecond();
            logger.info("ApplicationTimer: Stopping application at " + clock.instant() + " after " + runningTime + "s.");
            return CompletableFuture.completedFuture(null);
        });
    }
}
