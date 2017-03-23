package utilities.cache;

import models.*;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;

import java.util.concurrent.TimeUnit;


public class Server_Cache {

    public static CacheManager cacheManager;
    private static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public static void initCache(){

        try {
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
         *  1) Cache for Board  - Max for 100K or 500MB
         *  2) Cache for Board Online status for 100K Devices - Expirate time set to 20 Minutes with Backup on harddisk
         */

            cacheManager.createCache(Model_Board.CACHE_MODEL, CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_Board.class,
                    ResourcePoolsBuilder.heap(100000).offheap(200, MemoryUnit.MB)).withExpiry(Expirations.timeToLiveExpiration(Duration.of(15, TimeUnit.MINUTES))).build());

            Model_Board.cache_model_board = Server_Cache.cacheManager.getCache(Model_Board.CACHE_MODEL, String.class, Model_Board.class);


            cacheManager.createCache(Model_Board.CACHE_ONLINE_STATE, CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Boolean.class,
                    ResourcePoolsBuilder.heap(100000).offheap(500, MemoryUnit.MB)).withExpiry(Expirations.timeToLiveExpiration(Duration.of(60, TimeUnit.MINUTES))).build());

            Model_Board.cache_online_status = Server_Cache.cacheManager.getCache(Model_Board.CACHE_ONLINE_STATE, String.class, Boolean.class);


        /*
           Model_Instance
           ---------------------------------------------------------------------------------------------------------------------------------------------------------------
         */

            // TOM TODO



        /*
         *  Model_HomerServer
         *  ---------------------------------------------------------------------------------------------------------------------------------------------------------------
         *  1) Cache for Model_HomerServer  - Max for 100 or 100MB Backup 500MB on disk
         */

            cacheManager.createCache(Model_HomerServer.CACHE_MODEL, CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_HomerServer.class,
                    ResourcePoolsBuilder.heap(100).offheap(100, MemoryUnit.MB)).withExpiry(Expirations.timeToLiveExpiration(Duration.of(15, TimeUnit.HOURS))).build());

            Model_HomerServer.cache_model_homer_server =Server_Cache.cacheManager.getCache(Model_HomerServer.CACHE_MODEL, String.class, Model_HomerServer.class);


        }catch (Exception e){
            logger.error("Error:: ", e);
        }
    }


    public static void stopCache(){

        cacheManager.close();
    }
}
