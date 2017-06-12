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

    public static Result result_custom(int statusCode, String message){
        CoreResponse.cors();
        Result_Custom result = new Result_Custom();
        result.code = statusCode;
        result.message = message;
        return status(statusCode, Json.toJson(result));
    }
//**********************************************************************************************************************

    // Vracím objekty
    public static Result result_ok(JsonNode json){
        CoreResponse.cors();
        return ok(json);
    }

    // Vracím pouze OK 200 state
    public static Result result_ok(){
        CoreResponse.cors();
        return ok(  Json.toJson(new Result_Ok())  );
    }

    // Vracím pouze OK 200 state se zprávou
    public static Result result_ok(String message){

        Result_Ok resultOk = new Result_Ok();
        resultOk.message = message;

        CoreResponse.cors();
        return ok(Json.toJson(resultOk));

    }

//**********************************************************************************************************************

    public static Result redirect(String path){

        CoreResponse.cors();
        return Controller.redirect(path);

    }


//**********************************************************************************************************************

    public static Result result_pdfFile(byte[] byte_array, String  file_name){

        CoreResponse.cors_pdf_file();
        response().setHeader("filename", file_name);

        return ok(byte_array);
    }


//**********************************************************************************************************************

    // Vracím při vytvoření objekt, jedinná změna je, že code = 201!
    public static Status result_created(JsonNode o){
        CoreResponse.cors();
        return Controller.created(o);
    }


//**********************************************************************************************************************

    public static Result result_badRequest(){

        Result_BadRequest result = new Result_BadRequest();

        CoreResponse.cors();
        return Controller.badRequest(Json.toJson(result));

    }

    // Různé varianty, když se něco nepovede
    public static Result result_badRequest(String message){

        Result_BadRequest result = new Result_BadRequest();
        result.message = message;

        CoreResponse.cors();
        return Controller.badRequest(Json.toJson(result));

    }

    public static Result result_badRequest(String message, String state){

        Result_BadRequest result = new Result_BadRequest();
        result.message = message;
        result.state = state;

        CoreResponse.cors();
        return Controller.badRequest(Json.toJson(result));

    }

    public static Result result_badRequest(JsonNode o){

        CoreResponse.cors();
        return Controller.badRequest(Json.toJson(o));


    }

//**********************************************************************************************************************
    public static Result result_buildErrors(JsonNode o){

         CoreResponse.cors();
         return Controller.status(422, Json.toJson(o));

    }


//**********************************************************************************************************************

    public static Result result_externalServerIsOffline(String message){

        CoreResponse.cors();
        Result_ServerOffline result = new Result_ServerOffline();
        result.message = message;
        return Controller.status(477, Json.toJson(result));

    }


    public static Result result_externalServerError(JsonNode o){

        CoreResponse.cors();
        Result_ExternalServerSideError result = new Result_ExternalServerSideError();

        return Controller.status(478, o);

    }

    public static Result result_externalServerError(String message){


        CoreResponse.cors();
        Result_ExternalServerSideError result = new Result_ExternalServerSideError();
        result.message = message;

        return Controller.status(478, Json.toJson(result));

    }

//**********************************************************************************************************************

    // 400
    // Různé varianty, když se něco nepovede
    public static Result result_notFound(String message){
        CoreResponse.cors();

        Result_NotFound result = new Result_NotFound();
        result.message = message;

        return Controller.notFound(Json.toJson(result));
    }

//**********************************************************************************************************************

    // Výlučně pro odmítnutí nepřihlášeného uživatele
    //  Což je zajišťováno anotací ---->  @Security.Authenticated(Secured.class)
    public static Result result_unauthorized(){
        CoreResponse.cors();
        return Controller.unauthorized(Json.toJson( new Result_Unauthorized()));
    }

    // Používá se výhradně pro odmítnutí uživatelovi akce z bezečnostních důvodů
    // Například nemá oprávnění (Klíč) přistupovat k projektům ostatních uživatelů
    public static Status result_forbidden(){
        CoreResponse.cors();
        return Controller.forbidden(Json.toJson(new Result_Forbidden()));
    }


    // Používá se výhradně pro odmítnutí uživatelovi akce z bezečnostních důvodů
    // Například nemá oprávnění (Klíč) přistupovat k projektům ostatních uživatelů
    public static Status result_forbidden(String message){
        CoreResponse.cors();

        Result_Forbidden result = new Result_Forbidden();
        result.message = message;

        return Controller.forbidden(Json.toJson(result));
    }

    // Používá se výhradně pro odmítnutí uživatele při přihlášení, pokud nemá validovaný účet
    public static Status result_notValidated(){
        CoreResponse.cors();

        Result_NotValidated result_notValidated = new Result_NotValidated();

        return Controller.status(705, Json.toJson(result_notValidated));
    }

    // Používá se výhradně pro odmítnutí uživatele při přihlášení, pokud nemá validovaný účet
    public static Status result_notValidated(String message){
        CoreResponse.cors();

        Result_NotValidated result_notValidated = new Result_NotValidated();
        result_notValidated.message = message;

        return Controller.status(705, Json.toJson(result_notValidated));
    }

//**********************************************************************************************************************

    // Používáno pouze pro vrácení nevalidně přijatých FORM pokud se body Json transformuje na objekt
    // Hlídáno anotacemi viz Wiki:
    public static Result result_invalidBody(JsonNode json){
             CoreResponse.cors();

             Result_InvalidBody result = new Result_InvalidBody();
             result.state     = "Invalid body";
             result.message   = "Provided body is invalid. If it is possible, the reason will be returned in exception field.";
             result.exception = json;

            return Controller.badRequest( Json.toJson(result) );
    }

//**********************************************************************************************************************

    public static Result result_internalServerError(){
        CoreResponse.cors();
        Result_InternalServerError result_internalServerError = new Result_InternalServerError();
        return Controller.internalServerError(Json.toJson(result_internalServerError));
    }

    public static Result result_internalServerError(String message){
        CoreResponse.cors();
        Result_InternalServerError result_internalServerError = new Result_InternalServerError();
        result_internalServerError.message = message;
        return Controller.internalServerError(Json.toJson(result_internalServerError));
    }

}
