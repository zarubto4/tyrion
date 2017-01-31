package utilities.loginEntities;

import models.person.Model_Person;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;
import utilities.response.GlobalResult;


public class Secured_API extends Security.Authenticator {


    @Override
    public String getUsername(Context ctx) {

        Model_Person person = null;

        String[] authTokenHeaderValues = ctx.request().headers().get("X-AUTH-TOKEN");

        try {

            person = Model_Person.findByAuthToken(authTokenHeaderValues[0]); // TODO do Cache!!!

            if (person != null) {
                ctx.args.put("person", person);
                return person.id;
            }

            return null;

        }catch (NullPointerException e){
            return null;
        }
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return GlobalResult.result_Unauthorized();
    }

}
