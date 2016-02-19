
package utilities.deadbolt.understand;

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import controllers.SecurityController;
import models.persons.Person;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import utilities.loginEntities.Secured;
import utilities.response.GlobalResult;

import java.util.Optional;


public class DefaultHandler extends AbstractDeadboltHandler
{

    public F.Promise<Optional<Result>> beforeAuthCheck(final Http.Context context) {

            if (Secured.isLoggedIn(context)) {
                // user is logged in
                return F.Promise.pure(Optional.empty());
            } else {
                // user is not logged in

                return F.Promise.promise(new F.Function0<Optional<Result>>()
                {
                    @Override
                    public Optional<Result> apply() throws Throwable
                    {
                        return Optional.ofNullable( GlobalResult.forbidden_Global() );
                    }
                });
            }

    }


    public F.Promise<Optional<Subject>> getSubject(final Http.Context context) {


        System.out.println("DefaultHandler.getSubject");
        Person person1 = SecurityController.getPerson(context);
        if(person1 == null ) System.out.println("uživatel = null");


        return F.Promise.promise(() -> Optional.ofNullable(SecurityController.getPerson(context)));
    }


    public F.Promise<Optional<DynamicResourceHandler>> getDynamicResourceHandler(final Http.Context context) {
        System.out.println("DefaultHandler.getDynamicResourceHandler");
        Person person1 = SecurityController.getPerson(context);
        if(person1 == null ) System.out.println("uživatel = null");


        return F.Promise.promise(() -> Optional.of(new DefaultPermission()));
    }

    @Override
    public F.Promise<Result> onAuthFailure(final Http.Context context, final String content) {
        System.out.println("DefaultHandler.onAuthFailure");

        return F.Promise.promise(() -> GlobalResult.forbidden_Global("You haven't permissions"));
    }
}
