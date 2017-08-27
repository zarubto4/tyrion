package utilities.login_entities;

import models.Model_FloatingPersonToken;
import models.Model_Person;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utilities.logger.Class_Logger;

import static play.mvc.Controller.request;

public class Secured_Admin extends Security.Authenticator {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Secured_Admin.class);

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
                terminal_logger.debug("getUsername:: is empty - return null - login required");
                return null;
            }


            // Zjistím, zda v Cache už token není Pokud není - vyhledám Token objekt a ověřím jeho platnost
            if(!Model_Person.token_cache.containsKey(token)){


                Model_FloatingPersonToken model_token = Model_FloatingPersonToken.find.where().eq("authToken", token).findUnique();
                if(model_token == null || !model_token.isValid()){
                    terminal_logger.debug("Security Token:: " + token + " is not t is no longer valid according time");
                    return null;
                }


                Model_Person.token_cache.put(token, model_token.person.id);

            }

            ctx.args.put("person_token", token);

            if(!Model_Person.get_byAuthToken(token).admin_permission()){
                terminal_logger.warn("getUsername:: User does not have permissions");
                return null;
            }


            return Model_Person.token_cache.get(token);

        }catch (NullPointerException e){
            terminal_logger.internalServerError("getUsername:", e);
            return null;
        }
    }

    @Override
    public Result onUnauthorized(Http.Context ctx)
    {
        return redirect("/admin/login");
    }

}
