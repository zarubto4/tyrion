import play.Application;
import play.Configuration;
import play.GlobalSettings;
import play.mvc.Action;
import play.mvc.Http;
import utilities.webSocket.ClientThreadChecker;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class Global extends GlobalSettings {

    List<ClientThreadChecker> blockoServers = new ArrayList<>();

    @Override
    public void onStart(Application app) {

       // Nastavení práv

        // Nastartování Připojení

        System.out.println("Zapínám server - startuji proceduru připojování se k serveru BLOCKO \n");

        ClientThreadChecker clientThreadChecker = new ClientThreadChecker()
                                                    .setIDentificator( Configuration.root().getString("Servers.blocko.server1.name"))
                                                    .setPeriodReconnectionTime(15500)
                                                    .setReconnection(true)
                                                    .setServerAddress( Configuration.root().getString("Servers.blocko.server1.url"))
                                                    .connectToServer();

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

