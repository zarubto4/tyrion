package utilities.response;

import play.mvc.Controller;
import utilities.Server;

public class CoreResponse extends Controller {

    /**
     * Html hlavička, jež se přikládá ke všem Api požadavkům.
     * Viz dokumentace CORS hmlt.
     */
    public static void cors() {
        try {

            // Kontrola Becki verze::

            response().setHeader("Access-Control-Allow-Origin", "*"); // Zde bude web se kterým to může komunikovat (url frontendu)
            response().setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
            response().setHeader("Access-Control-Max-Age", "72000");
            response().setHeader("Content-Type", "application/json");
            response().setHeader("Byzance-Api-Version", Server.server_version);
            response().setHeader("Accept", "*");
            response().setHeader("Access-Control-Allow-Headers", "Access-Control-Allow-Origin, Content-Type, api_key, Authorization, x-auth-token, accept, appid, appname, authorization, content-type, becki-version");
        }catch (Exception e){
            //TODO dodělat Loggy
            e.printStackTrace();
        }
    }

    public static void cors(String url) {
        try {

            response().setHeader("Access-Control-Link", url);
            response().setHeader("Access-Control-Allow-Origin", "*"); // Zde bude web se kterým to může komunikovat (url frontendu)
            response().setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
            response().setHeader("Access-Control-Max-Age", "72000");
            response().setHeader("Content-Type", "*");
            response().setHeader("Byzance-Api-Version", Server.server_version);
            response().setHeader("Accept", "*");
            response().setHeader("Access-Control-Allow-Headers", "Access-Control-Allow-Origin, Content-Type, Content-Type,  api_key, Authorization, x-auth-token, accept, appid, appname, authorization, content-type, becki-version");
        }catch (Exception e){
            //TODO dodělat Loggy
            e.printStackTrace();
        }
    }


    // Určeno jako cors pro centrum notifikací (Content-Type musí být text/event-stream
    public static void cors_pdf_file() {
        try {
            response().setHeader("Access-Control-Allow-Origin", "*"); // Zde bude web se kterým to může komunikovat (url frontendu)
            response().setHeader("Access-Control-Allow-Methods", "GET");
            response().setHeader("Access-Control-Max-Age", "72000");
            response().setHeader("Content-Type", "application/pdf");
            response().setHeader("Byzance-Api-Version", Server.server_version);
            response().setHeader("Accept", "*");
            response().setHeader("Access-Control-Allow-Headers", "Access-Control-Allow-Origin, content-Type, api_key, Authorization, x-auth-token, accept, appid, appname, authorization, content-type, becki-version");
        }catch (Exception e){
            //TODO dodělat Loggy
            e.printStackTrace();
        }

    }

}
