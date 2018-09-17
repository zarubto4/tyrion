package services;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import javax.inject.*;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.typesafe.config.Config;
import play.inject.ApplicationLifecycle;
import utilities.Server;
import utilities.cache.ServerCache;
import utilities.logger.ServerLogger;
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
    private final ServerCache cache;

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("application");

    @Inject
    public ApplicationStarter(Clock clock, ApplicationLifecycle appLifecycle, Config configuration, Injector injector, SchedulerController scheduler, ServerCache cache) {

        this.clock = clock;
        this.appLifecycle = appLifecycle;
        this.configuration = configuration;
        this.scheduler = scheduler;
        this.cache = cache;
        try {

            ServerLogger.init(configuration);

            this.cache.initialize();

            Server.start(configuration, injector);
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
