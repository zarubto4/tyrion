package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import models.Model_Person;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import responses.*;
import utilities.errors.Exceptions.*;
import utilities.logger.Logger;
import utilities.logger.ServerLogger;
import utilities.server_measurement.RequestLatency;
import utilities.swagger.input.Swagger_Project_New;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.UUID;

/**
 * This class provides some common API for Tyrion REST Controller.
 * Creates results with given content.
 */
public abstract class _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(_BaseController.class);

// PERSON OPERATIONS ###################################################################################################

    /**
     * Pulls up the current user from the request context.
     * Calling this method in non-authenticated context will throw an exception.
     *
     * @return current person {@link Model_Person}
     */
    public static Model_Person person() throws Result_Error_Unauthorized {
        try {

            Model_Person person = (Model_Person) Controller.ctx().args.get("person");

            if (person != null) {
                return person;
            } else {
                throw new Result_Error_Unauthorized();
            }
        } catch (Exception e) {
            throw new Result_Error_Unauthorized();
        }
    }

    /**
     * Pulls up the current user from the request context.
     * Calling this method in non-authenticated context will throw an exception.
     *
     * @return current person id {@link UUID}
     */
    public static UUID personId() throws _Base_Result_Exception {
        try {
            UUID id = ((Model_Person) Controller.ctx().args.get("person")).id;
            if(id != null) {
                return id;
            } else {
                throw new Result_Error_Unauthorized();
            }
        } catch (Exception e) {
            throw new Result_Error_Unauthorized();
        }
    }

    /**
     * Returns true if there is a authenticated person in the context.
     *
     * @return boolean true if there is a person
     */
    public static boolean isAuthenticated() {
        try {
            return Controller.ctx().args.containsKey("person");
        } catch (Exception e) {
            return false;
        }
    }



// REQUEST OPERATIONS ###################################################################################################

    /**
     * Returns true if there is a authenticated person in the context
     * and if he has specified permission.
     *
     * @return boolean true if person is permitted
     */
    public JsonNode getBodyAsJson() {
        try {
            return Controller.request().body().asJson();
        } catch (Exception e) {
            logger.error(" getBodyAsJson:: ERROR EXCEPTION");
            // logger.internalServerError(e);
            throw new NullPointerException();
        }
    }


// RESPONSE OPERATIONS ##################################################################################################

    /**
     * Creates a result based on the provided status code.
     *
     * @param statusCode integer of
     * @param message    string to send in result
     * @return result with status code and message
     */
    public static Result customResult(int statusCode, String status, String message) {
        Result_Custom result = new Result_Custom();
        result.code = statusCode;
        result.state = status;
        result.message = message;
        return Controller.status(statusCode, Json.toJson(result));
    }

    public static Result customResult(int statusCode, String message) {
        Result_Custom result = new Result_Custom();
        result.code = statusCode;
        result.state = "Unknown";
        result.message = message;
        return Controller.status(statusCode, Json.toJson(result));
    }

// CREATE JSON! - 201 ##################################################################################################

    /**
     * Creates result created. Body of this result is some object itself instead of classic result json.
     *
     * @param json to send
     * @return 201 result
     */
    public static Result created(JsonNode json) {
        return Controller.created(json);
    }

// OK JSON! - 200 ######################################################################################################

    /**
     * Create an empty ok result.
     *
     * @return 200 result
     */

    public static Result ok() {
        return Controller.ok(Json.toJson(new Result_Ok()));
    }

    /**
     * Creates a result with the code 200 and provided json as the body.
     *
     * @param json JsonNode serialized object
     * @return 200 result ok with json
     */
    public static Result ok(JsonNode json) {
        check_latency();
        return Controller.ok(json);
    }

    /**
     * Creates an ok result with given message.
     *
     * @param message string to be sent
     * @return 200 result with message
     */
    public static Result ok(String message) {
        return Controller.ok(Json.toJson(new Result_Ok(message)));
    }

    /**
     * Creates an ok result with given File.
     * @param file
     * @return
     */
    public static Result ok(File file) {
        check_latency();
        return Controller.ok(file);
    }

// FILES - 200 #########################################################################################################

    /**
     * Create response with File in PDF
     * @param byte_array
     * @param file_name
     * @return 200 result with file in body
     */
    public static Result file(byte[] byte_array, String file_name) {
        Controller.response().setHeader("filename", file_name);
        return Controller.ok(byte_array);
    }

// REDIRECT 303 ########################################################################################################

    /**
     * Creates redirect result.
     *
     * @param url to redirect to
     * @return 303 result
     */
    public static Result redirect(String url) {
        return Controller.redirect(url);
    }

// BAD REQUEST - JSON! 400 #############################################################################################

    /**
     * Creates result bad request, when there is a client error.
     *
     * @return 400 result
     */
    public static Result badRequest() {
        return badRequest(Json.toJson(new Result_BadRequest()));
    }

    /**
     * Creates result bad request with message, when there is a client error.
     *
     * @param message to send
     * @return 400 result with message
     */
    public static Result badRequest(String message) {
        return badRequest(Json.toJson(new Result_BadRequest(message)));
    }

    /**
     * Creates result bad request with provided json as a body, when there is a client error.
     *
     * @param json to send
     * @return 400 result with message
     */
    public static Result badRequest(JsonNode json) {
        return Controller.badRequest(Json.toJson(json));
    }

// BAD REQUEST - JSON! 404 #############################################################################################

    /**
     * Creates a not found result with message from Class where code try to find annotation for Swagger
     *
     * @param class_type model what is missing
     * @return 404 result with message
     */
    public static Result notFound(Class class_type) {
        try {
            logger.error("notFound:: Call:: Class Name:: {}", class_type.getSimpleName());
            // Get Swagger Name from Annotation and return ii with name and description

            for (Annotation annotation : class_type.getAnnotations()) {
                Class<? extends Annotation> type = annotation.annotationType();
                if(type.getSimpleName().equals(ApiModel.class.getSimpleName())) {
                    for (Method method : type.getDeclaredMethods()) {
                        Object value = method.invoke(annotation, (Object[]) null);

                        if(method.getName().equals("value")) {
                            return Controller.notFound(Json.toJson(new Result_NotFound("Object  notFound. Probably from " + value + " model type.")));
                        }
                    }
                }
            }


            // Return name of object if Anotations is missing
            logger.error("Returning result notFound for incoming request, but class in constructor not contain ApiModel annotation");
            return Controller.notFound(Json.toJson(new Result_NotFound("Not Found Object. Probably from " + class_type.getSimpleName().replace("Model_", "") + " model type.")));

        } catch (Exception e){
            return Controller.notFound(Json.toJson(new Result_NotFound("Not Found Object. Probably from " + class_type.getSimpleName().replace("Model_", "") + " model type.")));
        }
    }

    /**
     * Creates a not found result with message from Class where code try to find annotation for Swagger
     * @param message
     * @return
     */
    public static Result notFound(String message) {
        return Controller.notFound(Json.toJson(new Result_NotFound(message)));
    }

// FOR COMPILATOR ######################################################################################################

    /**
     * Creates result when compilation was unsuccessful.
     *
     * @param json describing errors
     * @return 422 result
     */
    public static Result buildErrors(JsonNode json) {
        return Controller.status(422, Json.toJson(json));
    }

    /**
     * Creates result when target server is offline
     *
     * @param message to send
     * @return 477 result
     */
    public static Result externalServerOffline(String message) {
        return Controller.status(477, Json.toJson(new Result_ServerOffline(message)));
    }

    /**
     * Creates result signaling that error occurred outside this server.
     *
     * @return 478 result
     */
    public static Result externalServerError() {
        return Controller.status(478, Json.toJson(new Result_ExternalServerSideError()));
    }

    /**
     * Creates result signaling that error occurred outside this server.
     *
     * @param message describing error
     * @return 478 result with message
     */
    public static Result externalServerError(String message) {
        return Controller.status(478, Json.toJson(new Result_ExternalServerSideError(message)));
    }

    /**
     * Creates result signaling that error occurred outside this server.
     *
     * @param json with error
     * @return 478 result with json body
     */
    public static Result externalServerError(JsonNode json) {
        return Controller.status(478, json);
    }


// UNAUTHORIZED 401 - when token is missing (user login required) ##########################################################

    /**
     * Creates result unauthorized.
     * Signals that user have to be logged in.
     *
     * @return 401 result
     */
    public static Result unauthorized() {
        return Controller.unauthorized(Json.toJson(new Result_Unauthorized()));
    }

// FORBIDEN 403 - when user do illegal operations ######################################################################

    /**
     * Creates result forbidden
     *
     * @return 403 result
     */
    public static Result forbidden() {
        return Controller.forbidden(Json.toJson(new Result_Forbidden()));
    }

    /**
     * Creates result forbidden with custom message.
     *
     * @param message to send
     * @return 403 result with message
     */
    public static Result forbidden(String message) {
        return Controller.forbidden(Json.toJson(new Result_Forbidden(message)));
    }


// SPECIAL RESPONSE 70x for Login, Registration etc. ###################################################################

    /**
     * Creates result to reject the user, when his email is not validated.
     *
     * @return 705 result
     */
    public static Result notValidated() {
        return Controller.status(705, Json.toJson(new Result_NotValidated()));
    }

    /**
     * Creates result with custom message to reject the user, when his email is not validated.
     *
     * @return 705 result with message
     */
    public static Result notValidated(String message) {
        return Controller.status(705, Json.toJson(new Result_NotValidated(message)));
    }

    /**
     * Creates result with code 400. Used when binding incoming json to form.
     * If form has error, they are returned in the exception field.
     *
     * @param errors JsonNode with errors
     * @return 400 result
     */
    public static Result invalidBody(JsonNode errors) {
        return badRequest(Json.toJson(new Result_InvalidBody(errors)));
    }


// EXCEPTION - ALL GENERAL EXCEPTIONS ##################################################################################

    /**
     * General Flow Exception for Controllers Method.
     * Here we recognized and logged all exception like Object not found, Incoming Json is not valid according Form Exception
     * @param error
     * @return
     */
    public static Result controllerServerError(Throwable error) {
        try{

            logger.error("controllerServerError:: Incoming Error: {} ", error.getClass().getSimpleName());

            // Result_Error_NotFound
            if(error.getClass().getSimpleName().equals(_Base_Result_Exception.class.getSimpleName())){
                logger.error("controllerServerError:: _Base_Result_Exception");
                _Base_Result_Exception badRequest = (_Base_Result_Exception) error;
                return badRequest(badRequest.getMessage());
            }

            // Result_Error_NotFound
            if(error.getClass().getSimpleName().equals(Result_Error_NotFound.class.getSimpleName())){
                logger.error("controllerServerError:: Result_Error_NotFound");
                Result_Error_NotFound not_found = (Result_Error_NotFound) error;
                return notFound(not_found.getClass_not_found());
            }

            // Result_Error_InvalidBody
            if(error.getClass().getSimpleName().equals(Result_Error_InvalidBody.class.getSimpleName())){
                logger.error("controllerServerError:: Result_Error_InvalidBody");
                Result_Error_InvalidBody invalid_body = (Result_Error_InvalidBody) error;
                try {
                    Result_InvalidBody invalidBody = new Result_InvalidBody(invalid_body.getForm_error());
                    return badRequest(Json.toJson(invalidBody));
                } catch (Exception e){
                    logger.error("controllerServerError:: Fatal Error when controllerServerError try to get Form Error Json for response");
                    return internalServerError(e);
                }
            }

            // Result_Error_Unauthorized
            if(error.getClass().getSimpleName().equals(Result_Error_Unauthorized.class.getSimpleName())){
                logger.error("controllerServerError:: Result_Error_Unauthorized");
                return unauthorized();
            }

            // Result_Error_PermissionDenied
            if(error.getClass().getSimpleName().equals(Result_Error_PermissionDenied.class.getSimpleName())){
                logger.error("controllerServerError:: Result_Error_PermissionDenied");
                return forbidden();
            }

            // Result_Error_NotSupportedException
            if(error.getClass().getSimpleName().equals(Result_Error_NotSupportedException.class.getSimpleName())){
                error.printStackTrace();
                return badRequest( Json.toJson(new Result_UnsupportedException()));
            }

            logger.error("controllerServerError::There is unExcepted Kind of Error. Now - its Critical!");
            return internalServerError(error);

        } catch (Exception e) {
            logger.error("controllerServerError:: Exception in Exception");
            logger.error("controllerServerError:: Exception in Exception");
            return internalServerError(e);
        }
    }

    /**
     * Creates result internal server error and also saves the error to the database.
     *
     * @param error that was thrown
     * @return 500 result
     */
    public static Result internalServerError(Throwable error) {
        StackTraceElement current_stack = Thread.currentThread().getStackTrace()[2]; // Find the caller origin
        ServerLogger.error(error, current_stack.getClassName() + "::" + current_stack.getMethodName(), Controller.request());
        return Controller.internalServerError(Json.toJson(new Result_InternalServerError()));
    }

    //**********************************************************************************************************************

    public static void check_latency(){
        try{
            if(Http.Context.current().args.containsKey("tyrion_response_measurement_time")){
                RequestLatency.count_end( (String) Http.Context.current().args.get("tyrion_response_measurement_method"), (String) Http.Context.current().args.get("tyrion_response_measurement_path"), ( new Date().getTime() - (long) Http.Context.current().args.get("tyrion_response_measurement_time")));
            }
        }catch (Exception e){
            logger.internalServerError(e);
        }
    }


    //**********************************************************************************************************************

}
