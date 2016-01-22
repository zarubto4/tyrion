import play.Application;
import play.GlobalSettings;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import utilities.GlobalValue;

import java.lang.reflect.Method;

public class Global extends GlobalSettings {

   // public static String server = Configuration.root().getString("serverName");
    // For CORS
    private class ActionWrapper extends Action.Simple {

       public ActionWrapper(Action<?> action) {
           this.delegate = action;
       }

       @Override
       public F.Promise<Result> call(Http.Context ctx) throws java.lang.Throwable {
           F.Promise<Result> result = this.delegate.call(ctx);
           Http.Response response = ctx.response();
           response.setHeader("Access-Control-Allow-Origin", "*");
           return result;
       }
   }

    @Override
    public void onStart(Application app){

        try {
            GlobalValue.onStart();

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