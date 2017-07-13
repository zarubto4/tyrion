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

            terminal_logger.debug("Secured_Homer_Server: X-AUTH-TOKEN:: Token {}", token);

            if(WS_HomerServer.token_hash.containsKey(token)){

                terminal_logger.debug("Secured_Homer_Server: Token found!");
                return token;

            }else{

                terminal_logger.debug("Secured_Homer_Server: Token not found!");

                for(String key :WS_HomerServer.token_hash.keySet()){
                    terminal_logger.debug("Secured_Homer_Server: Keys: inside:: " + key);
                }

                return null;
            }

        }catch (NullPointerException e){
            terminal_logger.internalServerError("getUsername:", e);
            return null;
        }
    }

    @Override
    public Result onUnauthorized(Http.Context ctx){
        return GlobalResult.result_unauthorized();
    }

}