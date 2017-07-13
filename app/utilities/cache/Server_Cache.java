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
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(72, TimeUnit.HOURS))).build());

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

            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Project");
            Model_Project.cache = cacheManager.createCache(Model_Project.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_Project.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_Project.class.getSimpleName() + ".CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(2, TimeUnit.HOURS))).build());


            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Project Frontend Connection List of Person_ID_Tokens");
            Model_Project.token_cache = cacheManager.createCache(Model_Project.CACHE_BECKI_CONNECTED_PERSONS,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, IdsList.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_Project.class.getSimpleName() + ".CACHE_BECKI_CONNECTED_PERSONS")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(4, TimeUnit.HOURS))).build());

            // M_Project
            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for MProject Model");
            Model_MProject.cache = cacheManager.createCache(Model_MProject.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_MProject.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_MProject.class.getSimpleName() + ".CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(4, TimeUnit.HOURS))).build());

            // M_Program
            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for MProgram Model");
            Model_MProgram.cache = cacheManager.createCache(Model_MProgram.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_MProgram.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_MProgram.class.getSimpleName() + ".CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(4, TimeUnit.HOURS))).build());

            // C_Program
            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for C_Program Model");
            Model_CProgram.cache = cacheManager.createCache(Model_CProgram.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_CProgram.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_CProgram.class.getSimpleName() + ".CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(4, TimeUnit.HOURS))).build());

            // B_Program
            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for B_Program Model");
            Model_BProgram.cache = cacheManager.createCache(Model_BProgram.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_BProgram.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_BProgram.class.getSimpleName() + ".CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(4, TimeUnit.HOURS))).build());

            // Version
            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for VersionObject Model");
            Model_VersionObject.cache = cacheManager.createCache(Model_VersionObject.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_VersionObject.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_VersionObject.class.getSimpleName() + ".CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(4, TimeUnit.HOURS))).build());


            // Library
            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Library Model");
            Model_Library.cache = cacheManager.createCache(Model_Library.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_Library.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_Library.class.getSimpleName() + ".CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(4, TimeUnit.HOURS))).build());


        /*
         *  Type of Blocks && Type of Widgets
         *  ---------------------------------------------------------------------------------------------------------------------------------------------------------------
         *  Cache for Type of Blocks
         *  Cache for Blocko Block & Blocks Versions
         *
         *  Cache for Type of Widgets
         *  Cache for Widgets & Widgets Versions
         */

            // Type of Blocks
            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Type of Block Model");
            Model_TypeOfBlock.cache = cacheManager.createCache(Model_TypeOfBlock.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_TypeOfBlock.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_TypeOfBlock.class.getSimpleName() + ".CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(4, TimeUnit.HOURS))).build());

            // Blocko Block
            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Blocko Block Model");
            Model_BlockoBlock.cache = cacheManager.createCache(Model_BlockoBlock.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_BlockoBlock.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_BlockoBlock.class.getSimpleName() + ".CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(4, TimeUnit.HOURS))).build());

            //Blocko Block Version
            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Blocko Block Version Model");
            Model_BlockoBlockVersion.cache = cacheManager.createCache(Model_BlockoBlockVersion.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_BlockoBlockVersion.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_BlockoBlockVersion.class.getSimpleName() + ".CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(4, TimeUnit.HOURS))).build());



            // Type of Widget
            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Type of Widget Model");
            Model_TypeOfWidget.cache = cacheManager.createCache(Model_TypeOfWidget.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_TypeOfWidget.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_TypeOfWidget.class.getSimpleName() + ".CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(4, TimeUnit.HOURS))).build());

            // Grid Widget
            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for GridWidget Model");
            Model_GridWidget.cache = cacheManager.createCache(Model_GridWidget.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_GridWidget.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_GridWidget.class.getSimpleName() + ".CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(4, TimeUnit.HOURS))).build());

            // Grid Widget Version
            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for GridWidget Version Model");
            Model_GridWidgetVersion.cache = cacheManager.createCache(Model_GridWidgetVersion.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_GridWidgetVersion.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_GridWidgetVersion.class.getSimpleName() + ".CACHE")))
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
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." +  Model_Board.class.getSimpleName() + ".CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(60, TimeUnit.MINUTES))).build());

            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Board status");
            Model_Board.cache_status = cacheManager.createCache(Model_Board.CACHE_STATUS,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Boolean.class,
                    ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + ".Model_Board.CACHE_STATUS")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(60, TimeUnit.MINUTES))).build());


            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Library Model");
            Model_TypeOfBoard.cache = cacheManager.createCache(Model_TypeOfBoard.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_TypeOfBoard.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_TypeOfBoard.class.getSimpleName() + ".CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(24, TimeUnit.HOURS))).build());

        /*
         *  Updates
         *  ---------------------------------------------------------------------------------------------------------------------------------------------------------------
         *    Cache for Model_CProgramUpdatePlan, Model_ActualizationProcedure
         */
            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Update procedure");
            Model_ActualizationProcedure.cache = cacheManager.createCache(Model_ActualizationProcedure.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_ActualizationProcedure.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_ActualizationProcedure.class.getSimpleName() + ".CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(15, TimeUnit.MINUTES))).build());

            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Update procedure");
            Model_CProgramUpdatePlan.cache = cacheManager.createCache(Model_CProgramUpdatePlan.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_CProgramUpdatePlan.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_CProgramUpdatePlan.class.getSimpleName() + ".CACHE")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(7, TimeUnit.MINUTES))).build());

        /*
         *  Model_Instance
         *  ---------------------------------------------------------------------------------------------------------------------------------------------------------------
         *    Cache for Model_HomerInstance
         */


            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Instance status");
            Model_HomerInstance.cache_status = cacheManager.createCache(Model_HomerInstance.CACHE_STATUS,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Boolean.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_HomerInstance.class.getSimpleName() + ".CACHE_STATUS")))
                            .withExpiry(Expirations.timeToIdleExpiration(Duration.of(2, TimeUnit.HOURS))).build());


            terminal_logger.info("Tyrion Configuration:: Server Cache:: Set Cache for Instance status");
            Model_HomerInstance.cache = cacheManager.createCache(Model_HomerInstance.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_HomerInstance.class,
                            ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_HomerInstance.class.getSimpleName() + ".CACHE")))
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
            Model_HomerServer.cache = cacheManager.createCache(Model_HomerServer.CACHE,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Model_HomerServer.class,
                    ResourcePoolsBuilder.heap( Configuration.root().getInt("Cache." + Server.server_mode.name() + "." + Model_HomerServer.class.getSimpleName() + ".CACHE")))
                    .withExpiry(Expirations.timeToIdleExpiration(Duration.of(12, TimeUnit.HOURS))).build());



        }catch (Exception e){
            e.printStackTrace();
            terminal_logger.internalServerError("initCache:", e);

        }
    }

    public static void stopCache(){
        if(cacheManager != null) cacheManager.close();
    }
}
