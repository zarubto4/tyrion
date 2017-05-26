package utilities.login_entities;

import models.Model_FloatingPersonToken;
import models.Model_Person;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;
import utilities.logger.Class_Logger;
import utilities.response.GlobalResult;

import static play.mvc.Controller.request;


public class Secured_API extends Security.Authenticator {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/
    
private static final Class_Logger terminal_logger = new Class_Logger(Secured_API.class);

/* METHOD  -------------------------------------------------------------------------------------------------------------*/

    @Override   // We are using person_token not username!!!!
    public String getUsername(Context ctx) {

        String token = null;

        if(ctx.request().cookies().get("authToken") != null ){
           token = request().cookies().get("authToken").value();

        }else if(ctx.request().headers().get("X-AUTH-TOKEN") != null) {
            token = request().headers().get("X-AUTH-TOKEN")[0];
        }

        if(token == null){
            terminal_logger.debug("is empty - return null - login required");
            return null;
        }


        // Zjistím, zda v Cache už token není Pokud není - vyhledám Token objekt a ověřím jeho platnost
        if(!Model_Person.token_cache.containsKey(token)){

            Model_FloatingPersonToken model_token = Model_FloatingPersonToken.find.where().eq("authToken", token).findUnique();
            if(model_token == null || !model_token.isValid()){
                terminal_logger.warn("" + token + " is not t is no longer valid according time");
                return null;
            }

            if(model_token.person != null) {
                Model_Person.token_cache.put(token, model_token.person.id);
            }else {
                terminal_logger.warn("getUsername:: Model_FloatingPersonToken not contains Person!");
            }

        }


        ctx.args.put("person_token", token);

        return Model_Person.token_cache.get(token);

    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return GlobalResult.result_unauthorized();
    }

}
