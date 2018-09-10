package utilities.cache;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.Controller_WebSocket;
import io.ebeaninternal.api.HashQuery;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.ehcache.expiry.Expiry;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import utilities.homer_auto_deploy.DigitalOceanTyrionService;
import utilities.homer_auto_deploy.models.common.Swagger_ServerRegistration_FormData;
import utilities.logger.Logger;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ServerCache {

    private static final Logger logger = new Logger(ServerCache.class);

    public static CacheManager cacheManager;

    /**
     * Method for initialization of cache layer.
     * It finds every annotated cache fields in models and populate them.
     */
    @SuppressWarnings("unchecked")
    public static void init() {

        logger.info("init - cache layer initiating");

        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);

        long start = System.currentTimeMillis();
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("models"))
                .setScanners(new FieldAnnotationsScanner()));

        logger.info("init - scanning classes took {} ms", System.currentTimeMillis() - start);

        Set<Field> caches = reflections.getFieldsAnnotatedWith(CacheField.class);

        caches.forEach(field -> {

            CacheField annotation = field.getAnnotation(CacheField.class);

            String name = annotation.name();

            if (name.equals("")) {
                name = field.getDeclaringClass().getSimpleName();
            }

            logger.debug("init - setting cache: {}", name);

            try {

                Duration duration = Duration.of(annotation.duration(), TimeUnit.SECONDS);

                field.set(null, cacheManager.createCache(name,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(annotation.keyType(), annotation.value(),
                                ResourcePoolsBuilder.heap(annotation.maxElements()))
                                .withExpiry(annotation.automaticProlonging() ? Expirations.timeToIdleExpiration(duration) : Expirations.timeToLiveExpiration(duration)).build()));

            } catch (Exception e) {
                logger.error("init - cache init failed:", e);
                System.exit(1);
            }
        });

        Set<Field> cacheFinders = reflections.getFieldsAnnotatedWith(CacheFinderField.class);

        cacheFinders.forEach(field -> {

            CacheFinderField annotation = field.getAnnotation(CacheFinderField.class);

            String name = annotation.name();

            if (name.equals("")) {
                name = field.getDeclaringClass().getSimpleName();
            }

            logger.debug("init - setting cache: {}", name);

            try {

                Duration duration = Duration.of(annotation.duration(), TimeUnit.SECONDS);

                CacheFinder cacheFinder = (CacheFinder) field.get(null);

                cacheFinder.setCache(cacheManager.createCache(name,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(annotation.keyType(), annotation.value(),
                                ResourcePoolsBuilder.heap(annotation.maxElements()))
                                .withExpiry(annotation.automaticProlonging() ? Expirations.timeToIdleExpiration(duration) : Expirations.timeToLiveExpiration(duration)).build()));

                cacheFinder.setQueryCache(cacheManager.createCache(name + "_query",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Integer.class, UUID.class,
                                ResourcePoolsBuilder.heap(annotation.maxElements()))
                                .withExpiry(annotation.automaticProlonging() ? Expirations.timeToIdleExpiration(duration) : Expirations.timeToLiveExpiration(duration)).build()));


            } catch (Exception e) {
                logger.error("init - cache init failed:", e);
            }
        });

        // Sets token cache for web socket connections
        Controller_WebSocket.tokenCache = cacheManager.createCache("WS_TokenCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(UUID.class, UUID.class,
                        ResourcePoolsBuilder.heap(1000))
                        .withExpiry(Expirations.timeToLiveExpiration(Duration.of(25, TimeUnit.SECONDS))).build());

        // Sets token cache for web socket connections
        DigitalOceanTyrionService.tokenCache = cacheManager.createCache("Digital_Ocean_server_sizes",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Swagger_ServerRegistration_FormData.class,
                        ResourcePoolsBuilder.heap(3))
                        .withExpiry(Expirations.timeToLiveExpiration(Duration.of(12, TimeUnit.HOURS))).build());
    }

    /**
     * Stops the cache
     */
    public static void close() {
        cacheManager.close();
    }
}
