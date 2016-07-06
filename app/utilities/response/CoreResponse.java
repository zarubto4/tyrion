package utilities.response;

import play.Configuration;
import play.mvc.Controller;

public class CoreResponse extends Controller {

    /**
     * Html hlavička, jež se přikládá ke všem Api požadavkům.
     * Viz dokumentace CORS hmlt.
     */
    public static void cors() {
        try {
            response().setHeader("Access-Control-Allow-Origin", "*"); // Zde bude web se kterým to může komunikovat (url frontendu)
            response().setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, PATCH, OPTIONS");
            response().setHeader("Accept-Encoding", "gzip, deflate, sdch");
            response().setHeader("Access-Control-Max-Age", "72000");
            response().setHeader("Content-Type", "application/json");
            response().setHeader("Byzance-Api-Version", Configuration.root().getString("api.version"));
            response().setHeader("Accept", "*");
            response().setHeader("Access-Control-Allow-Headers", "content-Type, api_key, Authorization, x-auth-token, accept, appid, appname, authorization, content-type");
        }catch (Exception e){
            //TODO dodělat Loggy
            e.printStackTrace();
        }
    }

    public static void cors(String url) {
        try {
            response().setHeader("Access-Control-Link", url);
            response().setHeader("Access-Control-Allow-Origin", "*"); // Zde bude web se kterým to může komunikovat (url frontendu)
            response().setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, PATCH, OPTIONS");
            response().setHeader("Accept-Encoding", "gzip, deflate, sdch");
            response().setHeader("Access-Control-Max-Age", "72000");
            response().setHeader("Content-Type", "application/json");
            response().setHeader("Byzance-Api-Version", Configuration.root().getString("api.version"));
            response().setHeader("Accept", "*");
            response().setHeader("Access-Control-Allow-Headers", "content-Type, api_key, Authorization, x-auth-token, accept, appid, appname, authorization, content-type");
        }catch (Exception e){
            //TODO dodělat Loggy
            e.printStackTrace();
        }
    }



    // Určeno jako cors pro centrum notifikací (Content-Type musí být text/event-stream
    public static void cors_EventSource() {
        try {
            response().setHeader("Access-Control-Allow-Origin", "*"); // Zde bude web se kterým to může komunikovat (url frontendu)
            response().setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
            response().setHeader("Access-Control-Max-Age", "72000");
            response().setHeader("Content-Type", "text/event-stream");
            response().setHeader("Accept", "*");
            response().setHeader("Access-Control-Allow-Headers", "content-Type, api_key, Authorization, x-auth-token, accept, appid, appname, authorization, content-type");
        }catch (Exception e){
            //TODO dodělat Loggy
            e.printStackTrace();
        }

    }


}
