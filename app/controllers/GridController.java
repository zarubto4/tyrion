package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import utilities.loginEntities.Secured;
import utilities.response.GlobalResult;

@Security.Authenticated(Secured.class)
public class GridController extends play.mvc.Controller {

    @BodyParser.Of(BodyParser.Json.class)
    public Result new_M_Program() {
        try{
            JsonNode json = request().body().asJson();


            return GlobalResult.okResult( Json.toJson("asdf") );

        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e, "projectName - String", "projectDescription - TEXT");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }


}
