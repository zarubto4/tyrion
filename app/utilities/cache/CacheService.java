package utilities.cache;

import com.google.inject.Singleton;
import controllers.Controller_WebSocket;
import exceptions.NotSupportedException;
import org.ehcache.Cache;
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
import utilities.homer_auto_deploy.DigitalOceanTyrionService;
import utilities.homer_auto_deploy.models.common.Swagger_ServerRegistration_FormData;
import utilities.logger.Logger;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
public class CacheService {

    private static final Logger logger = new Logger(CacheService.class);

    private final CacheManager cacheManager;

    public CacheService() {
        this.cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    }

    /**
     * Method for initialization of cache layer.
     * It finds every annotated cache fields in models and populate them.
     */
    @SuppressWarnings("unchecked")
    public void initialize() {

        logger.info("init - cache layer initiating");

        long start = System.currentTimeMillis();
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("models"))
                .setScanners(new FieldAnnotationsScanner()));

        logger.info("init - scanning classes took {} ms", System.currentTimeMillis() - start);

        Set<Field> fields = reflections.getFieldsAnnotatedWith(InjectCache.class);

        fields.forEach(field -> {
            try {
                InjectCache annotation = field.getAnnotation(InjectCache.class);

                String name = annotation.name();

                if (name.equals("")) {
                    name = field.getDeclaringClass().getSimpleName();
                }

                logger.debug("init - setting cache: {}", name);

                Object obj = field.get(null);

                if (obj instanceof ModelCache) {
                    ModelCache modelCache = (ModelCache) obj;
                    modelCache.setCache(this.getCache(name, annotation.keyType(), annotation.value(), annotation.maxElements(), annotation.duration(), annotation.automaticProlonging()));
                    modelCache.setQueryCache(this.getCache(name + "_Query", Integer.class, annotation.keyType(), annotation.maxElements(), annotation.duration(), annotation.automaticProlonging()));
                } else if (field.getType().equals(Cache.class)) {
                    field.set(null, this.getCache(name, annotation.keyType(), annotation.value(), annotation.maxElements(), annotation.duration(), annotation.automaticProlonging()));
                } else {
                    throw new NotSupportedException("Cannot inject cache into " + obj.getClass() + ", because it does not implement ModelCache interface.");
                }

            } catch (Exception e) {
                logger.error("init - cache init failed:", e);
            }
        });

        // Sets token cache for web socket connections
        Controller_WebSocket.tokenCache = this.cacheManager.createCache("WS_TokenCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(UUID.class, UUID.class,
                        ResourcePoolsBuilder.heap(1000))
                        .withExpiry(Expirations.timeToLiveExpiration(Duration.of(25, TimeUnit.SECONDS))).build());

        // Sets token cache for web socket connections
        DigitalOceanTyrionService.tokenCache = this.cacheManager.createCache("Digital_Ocean_server_sizes",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Swagger_ServerRegistration_FormData.class,
                        ResourcePoolsBuilder.heap(3))
                        .withExpiry(Expirations.timeToLiveExpiration(Duration.of(12, TimeUnit.HOURS))).build());
    }

    public <K, V> Cache<K, V> getCache(String name, Class<K> keyType, Class<V> cachedType, long maxElements, long duration, boolean timeToIdle) {

        Duration duration1 = Duration.of(duration, TimeUnit.SECONDS);

        return this.cacheManager.createCache(name,
                CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(keyType, cachedType, ResourcePoolsBuilder.heap(maxElements))
                        .withExpiry(timeToIdle ? Expirations.timeToIdleExpiration(duration1) : Expirations.timeToLiveExpiration(duration1)).build());
    }

    /**
     * Stops the cache
     */
    public void close() {
        this.cacheManager.close();
    }
}
