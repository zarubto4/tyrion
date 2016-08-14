import controllers.WebSocketController_Incoming;
import org.quartz.SchedulerException;
import play.Application;
import play.GlobalSettings;
import play.mvc.Action;
import play.mvc.Http;
import utilities.Server;
import utilities.UtilTools;
import utilities.demo_data.Basic_Data;

import java.lang.reflect.Method;
import java.util.Date;


public class Global extends GlobalSettings {

   static play.Logger.ALogger logger = play.Logger.of("Loggy");

    @Override
    public void onStart(Application app) {
       try {

           //1
           logger.warn("Setting global values");
           Server.set_Server_address();

           //2
           logger.warn("Setting system Permission");
           Server.setPermission();

           //3
           logger.warn("Setting logback configuration");
           Server.set_Logback();

           //4
           logger.warn("Setting Directory for Files");
           Server.setDirectory();

           //5
           logger.warn("Starting actualization threads");
           Server.startThreads();

           //6
           logger.warn("Starting all scheduler threads");
           Server.startScheduling_procedures();

           //7
           logger.warn("Creating default system data in database");
           Basic_Data.set_default_objects();

           //8
           logger.warn("Creating Administrator");
           UtilTools.set_Developer_objects();
    //****************************************************************************************************************************

           logger.warn("Creating demo data");
           Basic_Data.set_basic_demo_data();

       }catch (Exception e){
          logger.error( "Server Start Exception - Global Settings",e);
       }
    }


    @Override
    public void onStop(Application app){

        logger.warn("Restarting Server - Time: " + new Date());

        logger.warn("Disconnection all Homers");
        WebSocketController_Incoming.disconnect_all_homers();

        logger.warn("Disconnection all Terminals");
        WebSocketController_Incoming.disconnect_all_mobiles();

        logger.warn("Disconnection all Blocko Servers");
        WebSocketController_Incoming.disconnect_all_Blocko_Servers();

        logger.warn("Disconnection all Compilation Servers");
        WebSocketController_Incoming.disconnect_all_Compilation_Servers();



        if(Server.server_mode){
            try {

                logger.warn("You have developer version - System remove CRON task from your RAM");
                Server.scheduler.clear();

            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }



        System.err.println(" ");
        System.err.println(" ");
        System.err.println("  __________    __                                             ___                   __        ________                                                              ");
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
        logger.debug(request.toString());
        logger.info(request.toString());
        return super.onRequest(request, actionMethod);
    }


}

