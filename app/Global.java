import play.Application;
import play.Configuration;
import play.GlobalSettings;
import play.Logger;
import play.mvc.Action;
import play.mvc.Http;
import utilities.GlobalValue;
import utilities.webSocket.ClientThreadChecker;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class Global extends GlobalSettings {

    List<ClientThreadChecker> blockoServers = new ArrayList<>();

    @Override
    public void onStart(Application app) {

       try {
           Logger.warn("Nastavuji globální proměné");
           GlobalValue.onStart();


           Logger.warn("Zapínám server - startuji proceduru připojování se k serveru BLOCKO ");
           if (Configuration.root().getBoolean("Servers.blocko.server1.run")) {
               ClientThreadChecker clientThreadChecker = new ClientThreadChecker()
                       .setIDentificator(Configuration.root().getString("Servers.blocko.server1.name"))
                       .setPeriodReconnectionTime(Configuration.root().getInt("Servers.blocko.server1.periodicTime"))
                       .setReconnection(true)
                       .setServerAddress(Configuration.root().getString("Servers.blocko.server1.url"))
                       .connectToServer();
           }

       }catch (Exception e){
         e.printStackTrace();
       }

    }


    @Override
    public void onStop(Application app){
        for( ClientThreadChecker clientThreadChecker :blockoServers){
         //   clientThreadChecker.stop();
        }
    }


    @Override
    public Action onRequest(Http.Request request, Method actionMethod) {
        System.out.println(request.toString());
        return super.onRequest(request, actionMethod);
    }

}

