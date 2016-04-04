package controllers;

import play.Configuration;
import play.Play;
import play.libs.F;
import utilities.loggy.*;
import play.mvc.Controller;
import play.mvc.Result;
import java.util.Base64;


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

    public Result error(String description) {
        Loggy.error(description);
        return redirect("/loggy");
    }

    public Result error(String summary, String description) {
        Loggy.error(summary, description);
        return redirect("/loggy");
    }

    public F.Promise<Result> login () {
        return Loggy.login();
    }
}
