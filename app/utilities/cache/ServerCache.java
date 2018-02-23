package utilities.cache;

import controllers.Controller_WebSocket;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
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

                if(annotation.automaticProlonging()) {
                    field.set(null, cacheManager.createCache(name,
                            CacheConfigurationBuilder.newCacheConfigurationBuilder(annotation.keyType(), annotation.value(),
                                    ResourcePoolsBuilder.heap(annotation.maxElements()))
                                    .withExpiry(Expirations.timeToIdleExpiration(Duration.of(annotation.duration(), TimeUnit.SECONDS))).build()));
                }else {
                    field.set(null, cacheManager.createCache(name,
                            CacheConfigurationBuilder.newCacheConfigurationBuilder(annotation.keyType(), annotation.value(),
                                    ResourcePoolsBuilder.heap(annotation.maxElements()))
                                    .withExpiry(Expirations.timeToLiveExpiration(Duration.of(annotation.duration(), TimeUnit.SECONDS))).build()));
                }

            } catch (Exception e) {
                logger.error("init - cache init failed:", e);
                System.exit(1);
            }
        });

        // Sets token cache for web socket connections
        Controller_WebSocket.tokenCache = cacheManager.createCache("WS_TokenCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(UUID.class, UUID.class,
                        ResourcePoolsBuilder.heap(1000))
                        .withExpiry(Expirations.timeToLiveExpiration(Duration.of(25, TimeUnit.SECONDS))).build());
    }

    /**
     * Stops the cache
     */
    public static void close() {
        cacheManager.close();
    }
}
