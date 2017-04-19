import controllers.Controller_WebSocket;
import play.Application;
import play.GlobalSettings;
import play.mvc.Action;
import play.mvc.Http;
import utilities.Server;
import utilities.cache.Server_Cache;
import utilities.enums.Enum_Tyrion_Server_mode;
import utilities.loggy.Loggy;
import utilities.request_counter.RequestCounter;
import utilities.scheduler.CustomScheduler;

import java.lang.reflect.Method;
import java.util.Date;


public class Global extends GlobalSettings {

    private static play.Logger.ALogger logger = play.Logger.of("Loggy");

    @Override
    public void onStart(Application app) {

        try {

           logger.warn("Global:: onStart: Starting the server on {}", new Date());

           //1
           logger.warn("Global:: onStart: Setting logback configuration");
           Server.setLogback();

           //2
           logger.warn("Global:: onStart: Setting global values");
           Server.setServerValues();

           //3
           logger.warn("Global:: onStart: Setting system Permission");
           Server.setPermission();

           //4
           logger.warn("Global:: onStart: Setting Directory for Files");
           Server.setDirectory();

           //5
           logger.warn("Global:: onStart: Starting threads");
           Server.startThreads();

           //6
           logger.warn("Global:: onStart: Starting all scheduler threads");
           Server.startSchedulingProcedures();

           //7
           logger.warn("Global:: onStart: Initializing the cache layer");
           Server.initCache();

           //8
           logger.warn("Global:: onStart: Creating Administrator");
           Server.setAdministrator();

       }catch (Exception e){
           Loggy.internalServerError("Global:: onStart:",e);
       }

    }

    @Override
    public void onStop(Application app){

        logger.warn("Global:: onStop: Shutting down the server on {}", new Date());

        logger.warn("Global:: onStop: Disconnecting all Blocko Servers");
        Controller_WebSocket.disconnect_all_homer_Servers();

        logger.warn("Global:: onStop: Disconnecting all Compilation Servers");
        Controller_WebSocket.disconnect_all_Compilation_Servers();

        logger.warn("Global:: onStop: Closing cache layer");
        Server_Cache.stopCache();

        if(Server.server_mode == Enum_Tyrion_Server_mode.developer ||Server.server_mode == Enum_Tyrion_Server_mode.stage){
            try {

                logger.warn("Global:: onStop: You have developer version - System removes CRON task from your RAM");
                CustomScheduler.stopScheduler();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        System.err.println(" ");
        System.err.println(" ");
        System.err.println("  _____________ __                                             ___                   __        ________                                                              ");
        System.err.println("  YMMMM9MMMMMMM MM                                             `MM                  69MM      `MMMMMMMb.                                                             ");
        System.err.println("        MM      MM                                              MM                 6M' `       MM    `Mb                                                             ");
        System.err.println("        MM      MM  __     ____           ____  ___  __     ____MM         _____  _MM__        MM     MM ___  __   _____     __     ___  __    ___   ___  __    __   ");
        System.err.println("        MM      MM 6MMb   6MMMMb         6MMMMb `MM 6MMb   6MMMMMM        6MMMMMb MMMMM        MM     MM `MM 6MM  6MMMMMb   6MMbMMM `MM 6MM  6MMMMb  `MM 6MMb  6MMb  ");
        System.err.println("        MM      MMM9 `Mb 6M'  `Mb       6M'  `Mb MMM9 `Mb 6M'  `MM       6M'   `Mb MM          MM    .M9  MM69 I 6M'   `Mb 6M'`Mb    MM69 I 8M'  `Mb  MM69 `MM69 `Mb ");
        System.err.println("        MM      MM'   MM MM    MM       MM    MM MM'   MM MM    MM       MM     MM MM          MMMMMMM9'  MM'    MM     MM MM  MM    MM'        ,oMM  MM'   MM'   MM ");
        System.err.println("        MM      MM    MM MMMMMMMM       MMMMMMMM MM    MM MM    MM       MM     MM MM          MM         MM     MM     MM YM.,M9    MM     ,6MM9'MM  MM    MM    MM ");
        System.err.println("        MM      MM    MM MM             MM       MM    MM MM    MM       MM     MM MM          MM         MM     MM     MM  YMM9     MM     MM'   MM  MM    MM    MM ");
        System.err.println("        MM      MM    MM YM    d9       YM    d9 MM    MM YM.  ,MM       YM.   ,M9 MM          MM         MM     YM.   ,M9 (M        MM     MM.  ,MM  MM    MM    MM ");
        System.err.println("       _MM_    _MM_  _MM_MMYMMMM9        YMMMM9 _MM_  _MM_ YMMMMMM_       YMMMMM9 _MM_        _MM_       _MM_     YMMMMM9   YMMMMb. _MM_    `YMMM9'Yb_MM_  _MM_  _MM_");
        System.err.println("                                                                                                                           6M    Yb                                  ");
        System.err.println("                                                                                                                           YM.   d9                                  ");
        System.err.println("                                                                                                                            YMMMM9                                   ");
        System.err.println("");

    }

    @Override
    public Action onRequest(Http.Request request, Method actionMethod) {

        logger.debug(request.path());
        RequestCounter.count(actionMethod.getName());

        return super.onRequest(request, actionMethod);
    }
}