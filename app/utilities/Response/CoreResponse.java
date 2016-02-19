package utilities.response;

import play.Configuration;
import play.mvc.Controller;

public class CoreResponse extends Controller {

    /**
     * Html hlavička, jež se přikládá ke všem Api požadavkům.
     * Viz dokumentace CORS hmlt.
     */
    public static void cors() {
        response().setHeader("Access-Control-Allow-Origin", "*"); // Zde bude web se kterým to může komunikovat (url frontendu)
        response().setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, PATCH, OPTIONS");
        response().setHeader("Accept-Encoding", "gzip, deflate, sdch");
        response().setHeader("Accept", "*");
        response().setHeader("Access-Control-Allow-Headers", "content-Type, api_key, Authorization, x-auth-token, accept, appid, appname, authorization, content-type");
        response().setHeader("Byzance-Technical-Version_Object", Configuration.root().getString("Server.version"));
        response().setHeader("Content-Type", "application/json");
        response().setHeader("Access-Control-Max-Age", "72000");
    }



}
