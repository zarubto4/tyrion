package controllers;

import play.Logger;
import play.Play;
import play.libs.F;
import play.mvc.Security;
import utilities.loggy.*;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.loginEntities.Secured;


public class LoggyController extends Controller {

    public Result mainViewDefault() {
        return mainView(Play.application().configuration().getInt("Loggy.fastCapacity"));
    }

    public Result mainView(int errorCount) {
        return ok(utilities.loggy.html.loggy.render(Loggy.getErrors(errorCount)));
    }

    public F.Promise<Result> upload(int id) {
        return Loggy.upload(id);
    }

    public Result deleteAll() {
        Loggy.deleteFast();
        Loggy.deleteFile();
        return redirect("/loggy");
    }

    //@Security.Authenticated(Secured.class)
    public Result error(String description) {
        Logger.error(SecurityController.getPerson() == null?"not logged":"logged");
        try {
            String s = null;
            return ok(""+s.length());
        }
        catch (Exception e) {
            return Loggy.internalServerError(e, request());
        }
    }

    public Result error(String summary, String description) {
        Loggy.error(summary, description);
        return redirect("/loggy");
    }

    public F.Promise<Result> login () {
        return Loggy.login();
    }
}
