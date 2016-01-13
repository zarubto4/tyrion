package utilities;

import play.mvc.Controller;

public class CoreResponse extends Controller {

    public static void cors() {
        response().setHeader("Access-Control-Allow-Origin", "*"); // Zde bude web se kterým to může komunikovat (url frontendu)
        response().setHeader("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS");
        response().setHeader("Access-Control-Allow-Headers", "Content-Type, x-auth-token");
        response().setHeader("Byzance-Technical-Version", "1.06");
        response().setHeader("Access-Control-Max-Age", "72000");
    }

}
