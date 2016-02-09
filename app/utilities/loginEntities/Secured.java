package utilities.loginEntities;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.SecurityController;
import models.login.Person;
import play.libs.Json;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;
import utilities.response.CoreResponse;


public class Secured extends Security.Authenticator {


    @Override
    public String getUsername(Context ctx) {

        Person person = null;
        String[] authTokenHeaderValues = ctx.request().headers().get(SecurityController.AUTH_TOKEN_HEADER);


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

        ObjectNode result = Json.newObject();
        result.put("code", "401");
        result.put("message", "Unauthorized access - please log in");

        CoreResponse.cors();
        return unauthorized(result);
    }

}
