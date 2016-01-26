import play.GlobalSettings;
import play.mvc.Action;
import play.mvc.Http;

import java.lang.reflect.Method;




public class Global extends GlobalSettings {


    @Override
    public Action onRequest(Http.Request request, Method actionMethod) {
        System.out.println(request.toString());
        return super.onRequest(request, actionMethod);
    }

}

