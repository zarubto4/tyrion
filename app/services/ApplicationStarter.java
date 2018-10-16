package services;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import javax.inject.*;

import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.typesafe.config.Config;
import play.api.db.evolutions.ApplicationEvolutions;
import play.inject.ApplicationLifecycle;
import play.libs.Json;
import utilities.Server;
import utilities.cache.CacheService;
import utilities.enums.ServerMode;
import utilities.logger.ServerLogger;
import utilities.permission.PermissionFilter;
import utilities.permission.PermissionHandlerInstantiator;
import utilities.scheduler.SchedulerController;

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
    private final SchedulerController scheduler;
    private final CacheService cache;

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("application");

    @Inject
    public ApplicationStarter(Clock clock, ApplicationLifecycle appLifecycle, Config configuration, Injector injector, SchedulerController scheduler, CacheService cache, ApplicationEvolutions applicationEvolutions) {

        this.clock = clock;
        this.appLifecycle = appLifecycle;
        this.configuration = configuration;
        this.scheduler = scheduler;
        this.cache = cache;
        try {

            ServerLogger.init(configuration);

            // TODO ugly!!! should be completely reworked to use DI
            Server.configuration = configuration;
            Server.mode = configuration.getEnum(ServerMode.class,"server.mode");

            this.cache.initialize();

            // For dependency injected serializer for permissions
            Json.mapper()
                    .setFilterProvider(new SimpleFilterProvider().addFilter("permission", injector.getInstance(PermissionFilter.class)))
                    .setHandlerInstantiator(injector.getInstance(PermissionHandlerInstantiator.class));

            Server.start(injector);
            this.scheduler.start();
        } catch (Exception e) {
            logger.error("Error starting the application", e);
            System.exit(1);
        }

        // This code is called when the application starts.
        start = clock.instant();
        logger.info("ApplicationTimer demo: Starting application at " + start);

        // When the application starts, register a stop hook with the
        // ApplicationLifecycle object. The code inside the stop hook will
        // be run when the application stops.
        appLifecycle.addStopHook(() -> {
            this.scheduler.stop();
            this.cache.close();
            Server.stop();
            Instant stop = clock.instant();
            Long runningTime = stop.getEpochSecond() - start.getEpochSecond();
            logger.info("ApplicationTimer demo: Stopping application at " + clock.instant() + " after " + runningTime + "s.");
            return CompletableFuture.completedFuture(null);
        });
    }
}
