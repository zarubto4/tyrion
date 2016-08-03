package utilities.loginEntities;

import models.person.Person;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;
import utilities.response.GlobalResult;


public class Secured_API extends Security.Authenticator {


    @Override
    public String getUsername(Context ctx) {

        Person person = null;

        String[] authTokenHeaderValues = ctx.request().headers().get("X-AUTH-TOKEN");

        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null)) {

            person = Person.findByAuthToken(authTokenHeaderValues[0]);

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
