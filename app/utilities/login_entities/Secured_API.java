package utilities.login_entities;

import models.person.Model_Person;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;
import utilities.response.GlobalResult;

import static play.mvc.Controller.request;


public class Secured_API extends Security.Authenticator {

    // Logger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    @Override
    public String getUsername(Context ctx) {

        Model_Person person = null;

        String token = null;

        if(ctx.request().cookies().get("authToken") != null ){

            token =  request().cookies().get("authToken").value();

        }else if(ctx.request().headers().get("X-AUTH-TOKEN") != null) {
            token = request().headers().get("X-AUTH-TOKEN")[0];
        }

        logger.debug("Security Token:: " + token);

        //TODO Přepsat do Try and Catche - aby se odstranili podmínkya v případě null poitnexception se vracel null
        if ((token != null)) {

            person = Model_Person.findByAuthToken(token); // TODO do Cache!!!

            if (person != null) {
                ctx.args.put("person", person);
                return person.id;
            }
        }

        return null;
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return GlobalResult.result_Unauthorized();
    }

}
