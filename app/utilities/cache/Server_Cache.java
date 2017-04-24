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
import utilities.logger.Class_Logger;

import java.util.concurrent.TimeUnit;


public class Server_Cache {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Server_Cache.class);


/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public static CacheManager cacheManager;


/* OPERATION  -----------------------------------------------------------------------------------------------------*/

    public static void initCache(){

        try {

            cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);


        /*
         *  Person / Token
         *  ---------------------------------------------------------------------------------------------------------------------------------------------------------------
         *
         */

            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Person Model");
            Model_Person.token_cache = cacheManager.createCache(Model_Person.CACHE_TOKEN,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + ".Model_Person.CACHE_TOKEN")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(2, TimeUnit.HOURS))).build());

            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Person Tokens");
            Model_Person.cache = cacheManager.createCache(Model_Person.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_Person.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + ".Model_Person.CACHE_TOKEN")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(1, TimeUnit.HOURS))).build());





        /*
         *  Project Hierarchy ( Project, B_Program, C_Program, M_Program,
         *  ---------------------------------------------------------------------------------------------------------------------------------------------------------------
         *
         */

            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Project Frontend Connection List of Person_ID_Tokens");
            Model_Project.token_cache = cacheManager.createCache(Model_Project.CACHE_BECKI_CONNECTED_PERSONS,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, IdsList.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_Project.class.getSimpleName() + ".CACHE_BECKI_CONNECTED_PERSONS")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(4, TimeUnit.HOURS))).build());

            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for C_Program Model");
            Model_CProgram.cache = cacheManager.createCache(Model_CProgram.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_CProgram.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_CProgram.class.getSimpleName() + ".CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(4, TimeUnit.HOURS))).build());

            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for C_Program Model Version under C_program");
            Model_CProgram.cache_versions = cacheManager.createCache(Model_CProgram.CACHE_VERSION,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_VersionObject.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_CProgram.class.getSimpleName() + ".CACHE_VERSION")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(4, TimeUnit.HOURS))).build());


        /*
         *  Model_Board
         *  ---------------------------------------------------------------------------------------------------------------------------------------------------------------
         *  Cache for Board
         *  Cache for Board Online status
         */

            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Board Models");
            Model_Board.cache = cacheManager.createCache(Model_Board.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_Board.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + ".Model_Board.CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(60, TimeUnit.MINUTES))).build());

            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Board status");
            Model_Board.cache_status = cacheManager.createCache(Model_Board.CACHE_STATUS,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Boolean.class,
                    ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + ".Model_Board.CACHE_STATUS")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(1, TimeUnit.HOURS))).build());




        /*
         *  Updates
         *  ---------------------------------------------------------------------------------------------------------------------------------------------------------------
         *    Cache for Model_CProgramUpdatePlan, Model_ActualizationProcedure
         */

            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Update procedure");
            Model_CProgramUpdatePlan.cache_model_update_plan = cacheManager.createCache(Model_CProgramUpdatePlan.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_CProgramUpdatePlan.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + ".Model_CProgramUpdatePlan.CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(7, TimeUnit.MINUTES))).build());




        /*
         *  Model_Instance
         *  ---------------------------------------------------------------------------------------------------------------------------------------------------------------
         *    Cache for Model_HomerInstance
         */

            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Instance status");
            Model_HomerInstance.cache_status = cacheManager.createCache(Model_HomerInstance.CACHE_STATUS,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Boolean.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + ".Model_HomerInstance.CACHE_STATUS")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(1, TimeUnit.HOURS))).build());

            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Instance status");
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
            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Homer Server Models");
            Model_HomerServer.cache = cacheManager.createCache(Model_HomerServer.CACHE, CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_HomerServer.class,
                    ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + ".Model_HomerServer.CACHE")))
                    .withExpiry(Expirations.timeToIdleExpiration(Duration.of(12, TimeUnit.HOURS))).build());




        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }
    }

    public static void stopCache(){
        cacheManager.close();
    }
}
