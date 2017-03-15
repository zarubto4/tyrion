package utilities.login_entities;

import models.Model_Person;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import static play.mvc.Controller.request;

public class Secured_Admin extends Security.Authenticator {


    @Override
    public String getUsername(Http.Context ctx) {

        try{


            Model_Person person = null;

            String token = null;

            if(ctx.request().cookies().get("authToken") != null ){

                token =  request().cookies().get("authToken").value();

            }else if(ctx.request().headers().get("X-AUTH-TOKEN") != null) {
                token = request().headers().get("X-AUTH-TOKEN")[0];
            }

            person = Model_Person.findByAuthToken(token); // TODO do Cache!!!

            if (person != null) {

                ctx.args.put("person", person);

                //if (person.has_permission("Byzance_employee"))
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
