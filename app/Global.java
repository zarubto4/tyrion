import utilities.A_GlobalValue;
import play.Application;
import play.GlobalSettings;
import play.mvc.Action;
import play.mvc.Http;

import java.lang.reflect.Method;

public class Global extends GlobalSettings {

   // public static String server = Configuration.root().getString("serverName");

    @Override
    public void onStart(Application app){

        try {
            A_GlobalValue.onStart();

            //  PermissionController.onStartPermission();
            //  OverFlowController.onStartPermission();

            // PersonCreateController.onStartPermission(); TODO

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public Action onRequest(Http.Request request, Method actionMethod) {
        System.out.println(request.toString());
        return super.onRequest(request, actionMethod);
    }

}