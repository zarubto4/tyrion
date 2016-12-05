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

            String token = null;

            if(ctx.request().cookies().get("authToken") != null ){

                token =  request().cookies().get("authToken").value();

            }else if(ctx.request().headers().get("X-AUTH-TOKEN") != null) {
                token = request().headers().get("X-AUTH-TOKEN")[0];
            }

            person = Person.findByAuthToken(token); // TODO do Cache!!!

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
