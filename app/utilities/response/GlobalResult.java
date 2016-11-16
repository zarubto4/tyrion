package utilities.response;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.response.response_objects.*;

public class GlobalResult extends Controller {


//**********************************************************************************************************************

    // Vracím objekty
    public static Result result_ok(JsonNode json){
        CoreResponse.cors();
        return ok(json);
    }

    // Vracím pouze OK 200 state
    public static Result result_ok(){
        CoreResponse.cors();
        return ok(  Json.toJson(new Result_ok())  );
    }

    // Vracím pouze OK 200 state se zprávou
    public static Result result_ok(String message){

        Result_ok resultOk = new Result_ok();
        resultOk.message = message;

        CoreResponse.cors();
        return ok(Json.toJson(resultOk));

    }


//**********************************************************************************************************************

    public static Result result_pdf_file(byte[] byte_array, String  file_name){

        CoreResponse.cors_pdf_file();
        response().setHeader("filename", file_name);

        return ok(byte_array);
    }


//**********************************************************************************************************************

    // Vracím při vytvoření objekt, jedinná změna je, že code = 201!
    public static Status created(JsonNode o){
        CoreResponse.cors();
        return Controller.created(o);
    }


//**********************************************************************************************************************

    // Různé varianty, když se něco nepovede
    public static Result result_BadRequest(String message){

        Result_BadRequest result = new Result_BadRequest();
        result.message = message;

        CoreResponse.cors();
        return Controller.badRequest(Json.toJson(result));

    }

    public static Result result_BadRequest(String message, String state){

        Result_BadRequest result = new Result_BadRequest();
        result.message = message;
        result.state = state;

        CoreResponse.cors();
        return Controller.badRequest(Json.toJson(result));

    }

    public static Result result_BadRequest(JsonNode o){

        CoreResponse.cors();
        return Controller.badRequest(Json.toJson(o));


    }

//**********************************************************************************************************************
    public static Result result_buildErrors(JsonNode o){

         CoreResponse.cors();
         return Controller.status(422, Json.toJson(o));

    }


//**********************************************************************************************************************

    public static Result result_external_server_is_offline(String message){

        CoreResponse.cors();
        Result_NotFound result = new Result_NotFound();
        result.message = message;
        return Controller.status(477, Json.toJson(result));

    }


    public static Result result_external_server_error(JsonNode o){

        CoreResponse.cors();
        return Controller.status(478, o);

    }

    public static Result result_external_server_error(String message){

        CoreResponse.cors();
        return Controller.status(478, message);

    }

//**********************************************************************************************************************

    // 400
    // Různé varianty, když se něco nepovede
    public static Result notFoundObject(String message){
        CoreResponse.cors();

        Result_NotFound result = new Result_NotFound();
        result.message = message;

        System.out.println("Not found object");

        return Controller.badRequest(Json.toJson(result));
    }

//**********************************************************************************************************************

    // Výlučně pro odmítnutí nepřihlášeného uživatele
    //  Což je zajišťováno anotací ---->  @Security.Authenticated(Secured.class)
    public static Result result_Unauthorized(){
        CoreResponse.cors();
        return Controller.unauthorized(Json.toJson( new Result_Unauthorized()));
    }

    // Používá se výhradně pro odmítnutí uživatelovi akce z bezečnostních důvodů
    // Například nemá oprávnění (Klíč) přistupovat k projektům ostatních uživatelů
    public static Status forbidden_Permission(){
        CoreResponse.cors();
        return Controller.forbidden(Json.toJson(new Result_PermissionRequired() ) );
    }


    // Používá se výhradně pro odmítnutí uživatelovi akce z bezečnostních důvodů
    // Například nemá oprávnění (Klíč) přistupovat k projektům ostatních uživatelů
    public static Status forbidden_Permission(String message){
        CoreResponse.cors();

        Result_PermissionRequired resultPermissionRequired = new Result_PermissionRequired();
        resultPermissionRequired.message = message;

        return Controller.forbidden(Json.toJson(resultPermissionRequired));
    }

//**********************************************************************************************************************


    public static Status external_server_is_offline(String message){
        CoreResponse.cors();

        Result_serverIsOffline serverIsOffline = new Result_serverIsOffline();
        serverIsOffline.message = message;

        return Controller.badRequest(Json.toJson(serverIsOffline));
    }

    public static Status external_server_is_offline(){
        CoreResponse.cors();

        Result_serverIsOffline serverIsOffline = new Result_serverIsOffline();

        return Controller.badRequest(Json.toJson(serverIsOffline));
    }

//**********************************************************************************************************************

    // Používáno pouze pro vrácení nevalidně přijatých FORM pokud se body Json transformuje na objekt
    // Hlídáno anotacemi viz Wiki:
    public static Result formExcepting(JsonNode json){
             CoreResponse.cors();

             Result_JsonValueMissing result = new Result_JsonValueMissing();
             result.state     = "Json Unrecognized Values";
             result.message   = "Your Json had some unrecognized fields. Please look at this example, or you can report it";
             result.exception = json;

            return Controller.badRequest( Json.toJson(result) );
    }

//**********************************************************************************************************************

    public static StatusHeader internalServerError(){
        CoreResponse.cors();
        return Controller.internalServerError();
    }

}
