package utilities.cache;

import models.*;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import play.Configuration;
import utilities.Server;
import utilities.cache.helps_objects.IdsList;

import java.util.concurrent.TimeUnit;


public class Server_Cache {

    public static CacheManager cacheManager;
    private static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public static void initCache(){

        try {

            cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);


        /*
         *  Person / Token
         *  ---------------------------------------------------------------------------------------------------------------------------------------------------------------
         *
         */

            logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Person Model");
            Model_Person.token_cache = cacheManager.createCache(Model_Person.CACHE_TOKEN,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + ".Model_Person.CACHE_TOKEN")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(2, TimeUnit.HOURS))).build());

            logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Person Tokens");
            Model_Person.cache = cacheManager.createCache(Model_Person.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_Person.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + ".Model_Person.CACHE_TOKEN")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(1, TimeUnit.HOURS))).build());





        /*
         *  Project Hierarchy ( Project, B_Program, C_Program, M_Program,
         *  ---------------------------------------------------------------------------------------------------------------------------------------------------------------
         *
         */

            logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Project Frontend Connection Person_ID_Tokens");
            Model_Project.token_cache = cacheManager.createCache(Model_Project.CACHE_BECKI_CONNECTED_PERSONS,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, IdsList.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + ".Model_Project.CACHE_BECKI_CONNECTED_PERSONS")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(4, TimeUnit.HOURS))).build());







        /*
         *  Model_Board
         *  ---------------------------------------------------------------------------------------------------------------------------------------------------------------
         *  Cache for Board
         *  Cache for Board Online status
         */

            logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Board Models");
            Model_Board.cache = cacheManager.createCache(Model_Board.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_Board.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + ".Model_Board.CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(60, TimeUnit.MINUTES))).build());

            logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Board status");
            Model_Board.cache_status = cacheManager.createCache(Model_Board.CACHE_STATUS,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Boolean.class,
                    ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + ".Model_Board.CACHE_STATUS")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(1, TimeUnit.HOURS))).build());




        /*
         *  Updates
         *  ---------------------------------------------------------------------------------------------------------------------------------------------------------------
         *    Cache for Model_CProgramUpdatePlan, Model_ActualizationProcedure
         */

            logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Update procedure");
            Model_CProgramUpdatePlan.cache_model_update_plan = cacheManager.createCache(Model_CProgramUpdatePlan.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_CProgramUpdatePlan.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + ".Model_CProgramUpdatePlan.CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(7, TimeUnit.MINUTES))).build());




        /*
         *  Model_Instance
         *  ---------------------------------------------------------------------------------------------------------------------------------------------------------------
         *    Cache for Model_HomerInstance
         */

            logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Instance status");
            Model_HomerInstance.cache_status = cacheManager.createCache(Model_HomerInstance.CACHE_STATUS,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Boolean.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + ".Model_HomerInstance.CACHE_STATUS")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(1, TimeUnit.HOURS))).build());

            logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Instance status");
            Model_HomerInstance.cache = cacheManager.createCache(Model_HomerInstance.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_HomerInstance.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + ".Model_HomerInstance.CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(30, TimeUnit.MINUTES))).build());





        /*
         *  Model_HomerServer
         *  ---------------------------------------------------------------------------------------------------------------------------------------------------------------
         *  Cache for Model_HomerServer
         *
         *  Homer_Status (Online / offline) is not necessary - you can check websocket.containsKey()
         *
         */
            logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Homer Server Models");
            Model_HomerServer.cache = cacheManager.createCache(Model_HomerServer.CACHE, CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_HomerServer.class,
                    ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + ".Model_HomerServer.CACHE")))
                    .withExpiry(Expirations.timeToIdleExpiration(Duration.of(12, TimeUnit.HOURS))).build());




        }catch (Exception e){
            logger.error("Server_Cache:: Error: ", e);
        }
    }

    public static void stopCache(){
        cacheManager.close();
    }
}
