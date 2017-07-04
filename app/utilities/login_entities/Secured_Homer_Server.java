package utilities.login_entities;

import models.Model_FloatingPersonToken;
import models.Model_Person;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utilities.logger.Class_Logger;
import utilities.response.GlobalResult;
import web_socket.services.WS_HomerServer;

import static play.mvc.Controller.request;

public class Secured_Homer_Server extends Security.Authenticator {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Secured_Homer_Server.class);

/* METHOD  -------------------------------------------------------------------------------------------------------------*/

    @Override
    public String getUsername(Http.Context ctx) {

        try{

            String token = null;

            if(ctx.request().cookies().get("authToken") != null ){
                token = request().cookies().get("authToken").value();

            }else if(ctx.request().headers().get("X-AUTH-TOKEN") != null) {
                token = request().headers().get("X-AUTH-TOKEN")[0];
            }

            if(token == null){
                terminal_logger.debug("Secured_Homer_Server: X-AUTH-TOKEN:: is empty - return null - token required");
                return null;
            }


            // Zjistím, zda v Cache už token není Pokud není - vyhledám Token objekt a ověřím jeho platnost
            if(WS_HomerServer.token_hash.containsKey(token)){
                 return token;
            }else{
                return null;
            }

        }catch (NullPointerException e){
            terminal_logger.internalServerError("getUsername:", e);
            return null;
        }
    }

    @Override
    public Result onUnauthorized(Http.Context ctx){
        return GlobalResult.badRequest("Unauthorized access");
    }

}
