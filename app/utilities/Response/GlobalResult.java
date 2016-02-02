package utilities.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class GlobalResult extends Controller {

    public static Result okResult(JsonNode json){
        CoreResponse.cors();
        return ok(json);
    }


    public static Result okResult(){
        ObjectNode result = Json.newObject();
        result.put("state", "ok");
        CoreResponse.cors();
        return ok(result);
    }

    public static Result okResult(String message){

        ObjectNode result = Json.newObject();
        result.put("state", "ok");
        result.put("message", message);
        CoreResponse.cors();
        return ok(result);

    }

    public static Result  okResultWithId(String id){
        ObjectNode result = Json.newObject();
        result.put("state", "ok");
        result.put("id", id);
        CoreResponse.cors();
        return ok(result);
    }

    public static Result badRequest(Exception e){

        System.out.println("CHYBA " + e.getMessage());

        ObjectNode result = Json.newObject();
        result.put("state", "error");
        result.put("message", e.getMessage());
        CoreResponse.cors();
        return badRequest(result);
    }

    public static Result badRequest(Exception e, String... args){
        ObjectNode result = Json.newObject();
        result.put("state", "error");
        result.put("message", e.getMessage());

        int i = 1;
        for(String arg : args) result.put("Required JSON parameter "+i++, arg);

        CoreResponse.cors();
        return Controller.badRequest(result);
    }

    public static StatusHeader internalServerError(){
        CoreResponse.cors();
        return Controller.internalServerError();
    }

    public static Status created(JsonNode o){
        CoreResponse.cors();
        return Controller.created(o);
    }

    public static Status update(JsonNode o){
        CoreResponse.cors();
        return Controller.created(o);
    }

    public static Result notFoundObject(){
        CoreResponse.cors();
        ObjectNode result = Json.newObject();
        result.put("state", "error");
        result.put("message", "Object not found");
        return Controller.badRequest(result);
    }

    public static StatusHeader forbidden(){
        CoreResponse.cors();
        return Controller.forbidden();
    }

    public static Status forbidden(String message){
        CoreResponse.cors();
        return Controller.forbidden(message);
    }

}
