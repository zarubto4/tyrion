package utilities.cache;

import models.*;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;

import java.util.concurrent.TimeUnit;


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


        /*
         *  Model_Board
         *  ---------------------------------------------------------------------------------------------------------------------------------------------------------------
         * // Cache for Board  - Max 100 Entries
         * // Cache for Board Online status for 100 Devices - Expiration time set to 60 Minutes
         */

        Model_Board.cache = cacheManager.createCache(Model_Board.CACHE,
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_Board.class,
                        ResourcePoolsBuilder.heap(100)).withExpiry(Expirations.timeToIdleExpiration(Duration.of(15, TimeUnit.MINUTES))).build());


        Model_Board.cache_status = cacheManager.createCache(Model_Board.CACHE_STATUS,
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Boolean.class,
                        ResourcePoolsBuilder.heap(1000)).withExpiry(Expirations.timeToIdleExpiration(Duration.of(60, TimeUnit.MINUTES))).build());


        /*
         *  Model_Instance
         *  ---------------------------------------------------------------------------------------------------------------------------------------------------------------
         */

        Model_HomerInstance.cache_status = cacheManager.createCache(Model_HomerInstance.CACHE_STATUS,
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Boolean.class,
                        ResourcePoolsBuilder.heap(1000)).withExpiry(Expirations.timeToIdleExpiration(Duration.of(60, TimeUnit.MINUTES))).build());

        //cacheManager.createCache(,
        //        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_HomerInstanceRecord.class,
        //                ResourcePoolsBuilder.heap(5000)).withExpiry(Expirations.timeToIdleExpiration(Duration.of(15, TimeUnit.MINUTES))).build());



        /**
         *  Model_HomerServer
         *  ---------------------------------------------------------------------------------------------------------------------------------------------------------------
         */
/*        cacheManager.createCache("TODO........",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_HomerServer.class,
                        ResourcePoolsBuilder.heap(10000)).build());

*/


    }

    public static void stopCache(){

        cacheManager.close();
    }
}
