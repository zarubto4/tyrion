package utilities.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.response.response_objects.*;

public class GlobalResult extends Controller {

    public static Result okResult(JsonNode json){
        CoreResponse.cors();
        return ok(json);
    }


    public static Result okResult(){

        CoreResponse.cors();
        return ok(Json.toJson(new Result_ok()));
    }

    public static Result okResult(String message){

        Result_ok _resultOk = new Result_ok();
        _resultOk.message = message;

        CoreResponse.cors();
        return ok(Json.toJson(_resultOk));

    }



    public static Result unauthorizedResult(){
        CoreResponse.cors();
        return Controller.unauthorized(Json.toJson( new Result_Unauthorized()));
    }

    public static Result nullPointerResult(Exception e){

        System.out.println("CHYBA " + e.getMessage());

        ObjectNode result = Json.newObject();
        result.put("state", "error");
        result.put("message", e.getMessage());
        CoreResponse.cors();
        return badRequest(result);
    }

    public static Status nullPointerResult(String message){

        ObjectNode result = Json.newObject();
        result.put("state", "error");
        result.put("message", message);
        CoreResponse.cors();
        return Controller.badRequest(result);
    }

    public static Result nullPointerResult(Exception e, String... args){

        JsonValueMissing result = new JsonValueMissing();
        result.code = 400;
        result.state = "error";
        result.message = e.getMessage();
        for(String arg : args) result.required_jSON_parameter.add(arg);

        CoreResponse.cors();
        return Controller.badRequest(Json.toJson(result));
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
        Result_NotFound result = new Result_NotFound();

        System.out.println("Not found object");

        return Controller.badRequest(Json.toJson(result));
    }


    public static Status forbidden_Global(){
        CoreResponse.cors();
        return Controller.forbidden(Json.toJson(new Result_PermissionRequired() ) );    }


    public static Status forbidden_Global(String message){
        CoreResponse.cors();
        Result_PermissionRequired resultPermissionRequired = new Result_PermissionRequired();
        resultPermissionRequired.message = message;
        return Controller.forbidden(Json.toJson(resultPermissionRequired));
    }

}
