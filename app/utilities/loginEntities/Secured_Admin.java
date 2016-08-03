package utilities.loginEntities;

import models.person.Person;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

public class Secured_Admin extends Security.Authenticator {


    @Override
    public String getUsername(Http.Context ctx) {

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
    public Result onUnauthorized(Http.Context ctx) {
        return redirect("/login");
    }

}
