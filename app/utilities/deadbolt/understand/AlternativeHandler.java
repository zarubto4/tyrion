package utilities.deadbolt.understand;

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import controllers.SecurityController;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import utilities.response.GlobalResult;

import java.util.Optional;


public class AlternativeHandler extends AbstractDeadboltHandler {

    public F.Promise<Optional<Result>> beforeAuthCheck(final Http.Context context) {
       // new Secured().getUsername(context);
        return F.Promise.promise(Optional::empty);
    }

    public F.Promise<Optional<Subject>> getSubject(final Http.Context context) {
        System.out.println("DefaultHandler.getSubject");
        return F.Promise.promise(() -> Optional.ofNullable(SecurityController.getPerson(context)));
    }

    public F.Promise<Optional<DynamicResourceHandler>> getDynamicResourceHandler(final Http.Context context) {
        System.out.println("Jsem v before DefaultHandler.getDynamicResourceHandler");
        return F.Promise.promise(() -> Optional.of(new DefaultPermission()));
    }

    @Override
    public F.Promise<Result> onAuthFailure(final Http.Context context, final String content) {
        return F.Promise.promise(() -> GlobalResult.forbidden_Global("You haven't permissions"));
    }
}
