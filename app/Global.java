import controllers.Controller_WebSocket;
import play.Application;
import play.GlobalSettings;
import play.mvc.Action;
import play.mvc.Http;
import utilities.Server;
import utilities.cache.Server_Cache;
import utilities.document_db.DocumentDB;
import utilities.enums.Enum_Terminal_Color;
import utilities.enums.Enum_Tyrion_Server_mode;
import utilities.hardware_registration_auhtority.Batch_Registration_Authority;
import utilities.hardware_registration_auhtority.Hardware_Registration_Authority;
import utilities.logger.Class_Logger;
import utilities.logger.ServerLogger;
import utilities.request_counter.RequestCounter;
import utilities.scheduler.CustomScheduler;
import utilities.slack.Slack;

import java.lang.reflect.Method;
import java.util.Date;


public class Global extends GlobalSettings {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Global.class);

/* On Start  -----------------------------------------------------------------------------------------------------------*/

    @Override
    public void onStart(Application app) {
        try {

            //1
            System.out.println(Enum_Terminal_Color.ANSI_YELLOW + "onStart - 1) Setting LogBack" + Enum_Terminal_Color.ANSI_RESET);
            Thread.sleep(2000);
            Server.setLogback();

            //2 Set Logs
            System.out.println(Enum_Terminal_Color.ANSI_YELLOW + "onStart - 2) Setting Logger for Tyrion" + Enum_Terminal_Color.ANSI_RESET);
            ServerLogger.setLogger();

            //3
            terminal_logger.warn( Enum_Terminal_Color.ANSI_YELLOW + "onStart - 3) Setting global values" + Enum_Terminal_Color.ANSI_RESET);
            Server.setServerValues();

            //4
            terminal_logger.warn( Enum_Terminal_Color.ANSI_YELLOW + "onStart - 4) Setting system Permission" + Enum_Terminal_Color.ANSI_RESET);
            Server.setPermission();

            //5
            terminal_logger.warn( Enum_Terminal_Color.ANSI_YELLOW + "onStart - 5) Setting Directory for Files" + Enum_Terminal_Color.ANSI_RESET);
            Server.setDirectory();

            //6
            terminal_logger.warn( Enum_Terminal_Color.ANSI_YELLOW + "onStart - 6) Starting threads" + Enum_Terminal_Color.ANSI_RESET);
            Server.startThreads();

            //7
            terminal_logger.warn( Enum_Terminal_Color.ANSI_YELLOW + "onStart - 7) Starting all scheduler threads" + Enum_Terminal_Color.ANSI_RESET);
            Server.startSchedulingProcedures();

            //8
            terminal_logger.warn( Enum_Terminal_Color.ANSI_YELLOW + "onStart - 8) Initializing the cache layer" + Enum_Terminal_Color.ANSI_RESET);
            Server.initCache();

            //9
            terminal_logger.warn( Enum_Terminal_Color.ANSI_YELLOW + "onStart - 9) Initializing the NO SQL Database" + Enum_Terminal_Color.ANSI_RESET);
            DocumentDB.set_no_SQL_collection();

            //10
            terminal_logger.warn( Enum_Terminal_Color.ANSI_YELLOW + "onStart - 10) Creating Administrator" + Enum_Terminal_Color.ANSI_RESET);
            Server.setAdministrator();

            //11
            terminal_logger.warn( Enum_Terminal_Color.ANSI_YELLOW + "onStart - 11) Creating Default Block and Widget Object" + Enum_Terminal_Color.ANSI_RESET);
            Server.setWidgetAnDBlock();

            //12
            terminal_logger.warn( Enum_Terminal_Color.ANSI_YELLOW + "onStart - 12) Synchronize production batch with Registration Authority" + Enum_Terminal_Color.ANSI_RESET);
            Batch_Registration_Authority.synchronize_batch_with_authority();

            //13
            terminal_logger.warn( Enum_Terminal_Color.ANSI_YELLOW + "onStart - 13) Synchronize all registered Hardware with Registration Authority" + Enum_Terminal_Color.ANSI_RESET);
            Hardware_Registration_Authority.synchronize_device_with_authority();



            if (Server.server_mode != Enum_Tyrion_Server_mode.developer) Slack.post("Tyrion server in Mode " + Server.server_mode.name() + " version: " + Server.server_version + " started on " + new Date().toString() + ".");

       }catch (Exception e){
            System.out.println();
            System.out.println();
            System.out.println();
            System.err.println("#########################################################################################");
            System.err.println("##                                                                                     ##");
            System.err.println("##       Tyrion is not configured properly!!!!                                         ##");
            System.err.println("##       Please - Check Global Class!!!!                                               ##");
            System.err.println("##                                                                                     ##");
            System.err.println("#########################################################################################");
            System.out.println();
            System.out.println();
            System.out.println();
            terminal_logger.internalServerError(e);
       }

    }


/* On Stop   -----------------------------------------------------------------------------------------------------------*/

    @Override
    public void onStop(Application app){

        terminal_logger.warn(Enum_Terminal_Color.ANSI_RED + "onStop - Shutting down the server on {}" + Enum_Terminal_Color.ANSI_RESET, new Date());

        terminal_logger.warn(Enum_Terminal_Color.ANSI_RED + "onStop - Disconnecting all Becki applications" + Enum_Terminal_Color.ANSI_RESET);
        Controller_WebSocket.disconnectBeckiApplications();

        terminal_logger.warn(Enum_Terminal_Color.ANSI_RED + "onStop - Disconnecting all Blocko Servers" + Enum_Terminal_Color.ANSI_RESET);
        Controller_WebSocket.disconnectHomerServers();

        terminal_logger.warn(Enum_Terminal_Color.ANSI_RED + "onStop - Disconnecting all Compilation Servers" + Enum_Terminal_Color.ANSI_RESET);
        Controller_WebSocket.disconnectCodeServers();

        terminal_logger.warn(Enum_Terminal_Color.ANSI_RED + "onStop - Closing cache layer" + Enum_Terminal_Color.ANSI_RESET);
        Server_Cache.stopCache();

        if(Server.server_mode == Enum_Tyrion_Server_mode.developer){
            try {

                terminal_logger.warn("onStop - You have developer version - System removes CRON task from your RAM");
                CustomScheduler.stop();

            } catch (Exception e) {
                terminal_logger.internalServerError(e);
            }
        }

        if (Server.server_mode != Enum_Tyrion_Server_mode.developer) Slack.post("Tyrion " + Server.server_mode.name() + " server stopped on " + new Date().toString() + ".");

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

        terminal_logger.debug("request - " + request.method() + " " + request.path());
        RequestCounter.count(actionMethod.getName());

        return super.onRequest(request, actionMethod);
    }
}