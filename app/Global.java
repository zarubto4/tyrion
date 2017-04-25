import controllers.Controller_WebSocket;
import models._Model_ExampleModelName;
import play.Application;
import play.GlobalSettings;
import play.mvc.Action;
import play.mvc.Http;
import utilities.Server;
import utilities.cache.Server_Cache;
import utilities.enums.Enum_Terminal_Color;
import utilities.enums.Enum_Tyrion_Server_mode;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.request_counter.RequestCounter;
import utilities.scheduler.CustomScheduler;

import java.lang.reflect.Method;
import java.util.Date;


public class Global extends GlobalSettings {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    static play.Logger.ALogger  terminal_logger =  play.Logger.of("TYRION");

/* On Start  -----------------------------------------------------------------------------------------------------------*/

    @Override
    public void onStart(Application app) {

        try {

            // Set Logs
            terminal_logger.warn(Enum_Terminal_Color.ANSI_YELLOW + "Global:: onStart: Starting the server on  {}" + Enum_Terminal_Color.ANSI_RESET, new Date());
           Server_Logger.set_Logger();

           //1
            terminal_logger.warn( Enum_Terminal_Color.ANSI_YELLOW + "Global:: onStart: Setting logback configuration" + Enum_Terminal_Color.ANSI_RESET);
           Server.setLogback();

           //2
            terminal_logger.warn( Enum_Terminal_Color.ANSI_YELLOW +  "Global:: onStart: Setting global values" + Enum_Terminal_Color.ANSI_RESET);
           Server.setServerValues();

           //3
            terminal_logger.warn( Enum_Terminal_Color.ANSI_YELLOW + "Global:: onStart: Setting system Permission" + Enum_Terminal_Color.ANSI_RESET);
           Server.setPermission();

           //4
            terminal_logger.warn( Enum_Terminal_Color.ANSI_YELLOW + "Global:: onStart: Setting Directory for Files" + Enum_Terminal_Color.ANSI_RESET);
           Server.setDirectory();

           //5
            terminal_logger.warn( Enum_Terminal_Color.ANSI_YELLOW + "Global:: onStart: Starting threads" + Enum_Terminal_Color.ANSI_RESET);
           Server.startThreads();

           //6
            terminal_logger.warn( Enum_Terminal_Color.ANSI_YELLOW + "Global:: onStart: Starting all scheduler threads" + Enum_Terminal_Color.ANSI_RESET);
           Server.startSchedulingProcedures();

           //7
            terminal_logger.warn( Enum_Terminal_Color.ANSI_YELLOW + "Global:: onStart: Initializing the cache layer" + Enum_Terminal_Color.ANSI_RESET);
           Server.initCache();

           //8
            terminal_logger.warn( Enum_Terminal_Color.ANSI_YELLOW +"Global:: onStart: Creating Administrator" + Enum_Terminal_Color.ANSI_RESET);
           Server.setAdministrator();


       }catch (Exception e){
            System.out.println("");
            System.out.println("");
            System.out.println("");
            terminal_logger.error("#########################################################################################");
            terminal_logger.error("##                                                                                     ##");
            terminal_logger.error("##       Tyrion is not configured properly!!!!                                         ##");
            terminal_logger.error("##       Please - Check Global Class!!!!                                               ##");
            terminal_logger.error("##                                                                                     ##");
            terminal_logger.error("##                                                                                     ##");
            terminal_logger.error("#########################################################################################");
            System.out.println("");
            System.out.println("");
            System.out.println("");
            terminal_logger.error("onStart Error", e);
       }

    }


/* On Stop   -----------------------------------------------------------------------------------------------------------*/

    @Override
    public void onStop(Application app){

        terminal_logger.warn(Enum_Terminal_Color.ANSI_RED +"Global:: onStop: Shutting down the server on {}" + Enum_Terminal_Color.ANSI_RESET, new Date());

        terminal_logger.warn(Enum_Terminal_Color.ANSI_RED + "Global:: onStop: Disconnecting all Blocko Servers" + Enum_Terminal_Color.ANSI_RESET);
        Controller_WebSocket.disconnect_all_homer_Servers();

        terminal_logger.warn(Enum_Terminal_Color.ANSI_RED + "Global:: onStop: Disconnecting all Compilation Servers" + Enum_Terminal_Color.ANSI_RESET);
        Controller_WebSocket.disconnect_all_Compilation_Servers();

        terminal_logger.warn(Enum_Terminal_Color.ANSI_RED + "Global:: onStop: Closing cache layer" + Enum_Terminal_Color.ANSI_RESET);
        Server_Cache.stopCache();

        if(Server.server_mode == Enum_Tyrion_Server_mode.developer || Server.server_mode == Enum_Tyrion_Server_mode.stage){
            try {

                terminal_logger.warn("Global:: onStop: You have developer version - System removes CRON task from your RAM");
                CustomScheduler.stopScheduler();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        System.err.println(Enum_Terminal_Color.ANSI_RED + " ");
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
        System.err.println("" + Enum_Terminal_Color.ANSI_RESET);

    }

/* On Request   -----------------------------------------------------------------------------------------------------------*/

    @Override
    public Action onRequest(Http.Request request, Method actionMethod) {

        terminal_logger.debug(request.path());
        RequestCounter.count(actionMethod.getName());

        return super.onRequest(request, actionMethod);
    }
}