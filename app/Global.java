import controllers.WebSocketController_Incoming;
import play.Application;
import play.GlobalSettings;
import play.mvc.Action;
import play.mvc.Http;
import utilities.Server;
import utilities.UtilTools;

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

           //3
           logger.warn("Setting system Permission");
           Server.setPermission();

           //4
           logger.warn("Setting logback configuration");
           Server.set_Logback();

           //4
           logger.warn("Setting Directory for Files");
           Server.setDirectory();

           logger.warn("Starting actualization threads");
           Server.startThreads();


    //****************************************************************************************************************************
            UtilTools.set_Developer_objects(); // TODO bude smazáno - slouží jen k vytvoření prvního uživatele
            UtilTools.set_Homer_Server();
            UtilTools.set_Compilation_Server();
            UtilTools.set_Type_of_board();


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
    }


    @Override
    public Action onRequest(Http.Request request, Method actionMethod) {
        logger.debug(request.toString());
        logger.info(request.toString());
        return super.onRequest(request, actionMethod);
    }


}

