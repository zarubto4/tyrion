package utilities.cache;

import models.Model_FloatingPersonToken;
import models.Model_Person;
import models.Model_Project;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

public class Server_Cache {

    public static CacheManager cacheManager;

    public static void initCache(){

        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);

        cacheManager.createCache("person_token",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_Person.class,
                        ResourcePoolsBuilder.heap(100)).build());

        cacheManager.createCache("person_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_Person.class,
                        ResourcePoolsBuilder.heap(100)).build());

        cacheManager.createCache("project",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_Project.class,
                        ResourcePoolsBuilder.heap(100)).build());

        cacheManager.createCache("token",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_FloatingPersonToken.class,
                        ResourcePoolsBuilder.heap(100)).build());

        cacheManager.createCache("instance_state",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_FloatingPersonToken.class,
                        ResourcePoolsBuilder.heap(100)).build());


    }

    public static void stopCache(){

        cacheManager.close();
    }
}
