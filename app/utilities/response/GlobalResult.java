package utilities.response;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.logger.Class_Logger;
import utilities.response.response_objects.*;

public class GlobalResult extends Controller {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(GlobalResult.class);

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

    public static Result result_BadRequest(){

        Result_BadRequest result = new Result_BadRequest();

        CoreResponse.cors();
        return Controller.badRequest(Json.toJson(result));

    }

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
        result.code = 477;
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

    // Používá se výhradně pro odmítnutí uživatele při přihlášení, pokud nemá validovaný účet
    public static Status result_NotValidated(){
        CoreResponse.cors();

        Result_NotValidated result_notValidated = new Result_NotValidated();

        return Controller.status(705, Json.toJson(result_notValidated));
    }

    // Používá se výhradně pro odmítnutí uživatele při přihlášení, pokud nemá validovaný účet
    public static Status result_NotValidated(String message){
        CoreResponse.cors();

        Result_NotValidated result_notValidated = new Result_NotValidated();
        result_notValidated.message = message;

        return Controller.status(705, Json.toJson(result_notValidated));
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
             result.state     = "Json Unrecognized parameters or Invalid data";
             result.message   = "Your Json had some unrecognized fields or data is incorrect. Look at exception parameter. If it is possible, we will try return example of data inputs.";
             result.exception = json;

            return Controller.badRequest( Json.toJson(result) );
    }

//**********************************************************************************************************************

    public static Result result_InternalServerError(){
        CoreResponse.cors();
        Result_InternalServerError result_internalServerError = new Result_InternalServerError();
        return Controller.internalServerError(Json.toJson(result_internalServerError));
    }

    public static Result result_InternalServerError(String message){
        CoreResponse.cors();
        Result_InternalServerError result_internalServerError = new Result_InternalServerError();
        result_internalServerError.message = message;
        return Controller.internalServerError(Json.toJson(result_internalServerError));
    }

}
