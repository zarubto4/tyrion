package utilities.loginEntities;

import models.person.Person;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import static play.mvc.Controller.request;

public class Secured_Admin extends Security.Authenticator {


    @Override
    public String getUsername(Http.Context ctx) {

        try{

            Person person = null;
            String token =  request().cookies().get("authToken").value();
            person = Person.findByAuthToken(token);

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
    public Result onUnauthorized(Http.Context ctx)
    {
        return redirect("/admin/login");
    }

}
