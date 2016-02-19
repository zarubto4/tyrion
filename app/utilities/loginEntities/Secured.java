package utilities.loginEntities;

import models.persons.Person;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;
import utilities.response.GlobalResult;


public class Secured extends Security.Authenticator {


    @Override
    public String getUsername(Context ctx) {

        System.out.println("Kontrola uživatele");
        Person person = null;

        String[] authTokenHeaderValues = ctx.request().headers().get("X-AUTH-TOKEN");


        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null)) {
            person = Person.findByAuthToken(authTokenHeaderValues[0]);
            if (person != null) {
                ctx.args.put("person", person);
                return person.id;
            }
        }

        System.out.println("Vracím null");
        return null;
    }

    public static boolean isLoggedIn(Context ctx){
        System.out.println("Kontrola isLoggedIn");

        String[] authTokenHeaderValues = ctx.request().headers().get("X-AUTH-TOKEN");


        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null)) {
            if (Person.findByAuthToken(authTokenHeaderValues[0])!= null) {
                return true;
            }
        }
       return false;
    }


    @Override
    public Result onUnauthorized(Context ctx) {
        return GlobalResult.unauthorizedResult();
    }

}
