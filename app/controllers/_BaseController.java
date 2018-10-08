package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import exceptions.ForbiddenException;
import exceptions.NotFoundException;
import exceptions.NotSupportedException;
import io.swagger.annotations.ApiModel;
import models.Model_Person;
import play.Environment;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import responses.*;
import utilities.errors.Exceptions.*;
import utilities.logger.Logger;
import utilities.logger.ServerLogger;
import utilities.logger.YouTrack;
import utilities.model.BaseModel;
import utilities.model.JsonSerializable;
import utilities.permission.Action;
import utilities.permission.PermissionService;
import utilities.scheduler.SchedulerController;
import utilities.server_measurement.RequestLatency;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * This class provides some common API for Tyrion REST Controller.
 * Creates results with given content.
 */
public abstract class _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(_BaseController.class);

// COMMON CONSTRUCTOR ###################################################################################################

    protected final _BaseFormFactory baseFormFactory;
    protected final WSClient ws;
    protected final Environment environment;
    protected final YouTrack youTrack;
    protected final Config config;
    protected final SchedulerController scheduler;
    protected final PermissionService permissionService;

    @Inject
    public _BaseController(Environment environment, WSClient ws, _BaseFormFactory formFactory, YouTrack youTrack, Config config, SchedulerController scheduler, PermissionService permissionService) {
        this.environment = environment;
        this.ws = ws;
        this.baseFormFactory = formFactory;
        this.youTrack = youTrack;
        this.config = config;
        this.scheduler = scheduler;
        this.permissionService = permissionService;
    }

    public Result create(BaseModel model) throws ForbiddenException, NotSupportedException {
        this.permissionService.checkCreate(person(), model);
        model.save();
        return created(model);
    }

    public Result read(BaseModel model) throws ForbiddenException, NotSupportedException {
        this.permissionService.checkRead(person(), model);
        return ok(model);
    }

    public Result update(BaseModel model) throws ForbiddenException, NotSupportedException {
        try {
            this.permissionService.checkUpdate(person(), model);
        } catch (ForbiddenException e) {
            model.refresh();
            throw e;
        }
        model.update();
        return ok(model);
    }

    public Result delete(BaseModel model) throws ForbiddenException, NotSupportedException {
        this.permissionService.checkDelete(person(), model);
        model.delete();
        return ok();
    }

    public void checkCreatePermission(BaseModel model) throws ForbiddenException, NotSupportedException {
        this.permissionService.checkCreate(person(), model);
    }

    public void checkReadPermission(BaseModel model) throws ForbiddenException, NotSupportedException {
        this.permissionService.checkRead(person(), model);
    }

    public void checkUpdatePermission(BaseModel model) throws ForbiddenException, NotSupportedException {
        this.permissionService.checkUpdate(person(), model);
    }

    public void checkDeletePermission(BaseModel model) throws ForbiddenException, NotSupportedException {
        this.permissionService.checkDelete(person(), model);
    }

    public void checkActivatePermission(BaseModel model) throws ForbiddenException, NotSupportedException {
        this.permissionService.check(person(), model, Action.ACTIVATE);
    }

    public void checkPublishPermission(BaseModel model) throws ForbiddenException, NotSupportedException {
        this.permissionService.check(person(), model, Action.PUBLISH);
    }

    public void checkInvitePermission(BaseModel model) throws ForbiddenException, NotSupportedException {
        this.permissionService.check(person(), model, Action.INVITE);
    }

// PERSON OPERATIONS ###################################################################################################

    /**
     * Shortcuts for automatic validation and parsing of incoming JSON to MODEL class
     * @param clazz
     * @param <T>
     * @return
     * @throws _Base_Result_Exception
     */
    public <T> T formFromRequestWithValidation(Class<T> clazz) throws _Base_Result_Exception {
        return baseFormFactory.formFromRequestWithValidation(clazz);
    }

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

    /**
     * Checks whether the currently logged user is admin.
     * @return true if he is admin
     */
    public boolean isAdmin() {
        return isAdmin(person());
    }

    /**
     * Checks whether the given person is admin.
     * @param person to check
     * @return true if he is admin
     */
    public boolean isAdmin(Model_Person person) {
        return this.permissionService.isAdmin(person);
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
     * @param object of BaseModel to send
     * @return 201 result
     */
    public static Result created(JsonSerializable object) {
        return Controller.created(object.json());
    }

    /*public static Result created(_Swagger_Abstract_Default object) {
        return Controller.created(object.json());
    }*/
// OK JSON! - 200 ######################################################################################################

    /**
     * Create an empty ok result.
     *
     * @return 200 result
     */
    public static Result ok() {
        return Controller.ok(Json.toJson(new Result_Ok()));
    }

    public static Result ok(List <? extends JsonSerializable> objects){
        check_latency();
        return Controller.ok(Json.toJson(objects)); // TODO tato metoda je nesystemová a list by neměl v tyrionovi být - Oprava TZ!
    }

    /**
     * Create stream result
     *
     * @return 200 result
     */
    public static Result ok(InputStream stream, long content_length) {
        return Controller.ok(stream, content_length);
    }

    /**
     * Creates a result with the code 200 and provided json as the body.
     *
     * @param object BaseModel serialized object
     * @return 200 result ok with json
     */
    public static Result ok(JsonSerializable object) {
        check_latency();
        return Controller.ok(object.json());
    }

    /**
     * Creates a result with the code 200 and provided json as the body.
     *
     * @param object _Swagger_Abstract_Default serialized object
     * @return 200 result ok with json
     */
    /*public static Result ok(_Swagger_Abstract_Default object) {
        check_latency();
        return Controller.ok(object.json());
    }*/


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
     * @param cls model what is missing
     * @return 404 result with message
     */
    public static Result notFound(Class cls) {
        try {
            logger.error("notFound - class: {}", cls.getSimpleName());
            // Get Swagger Name from Annotation and return ii with name and description

            if (cls.isAnnotationPresent(ApiModel.class)) {
                ApiModel annotation = (ApiModel) cls.getAnnotation(ApiModel.class);
                return Controller.notFound(Json.toJson(new Result_NotFound("Object " + annotation.value() + " not found.")));
            }

            // Return name of object if Anotations is missing
            logger.warn("Returning result notFound for incoming request, but given class does not have ApiModel annotation");

        } catch (Exception e){
            logger.internalServerError(e);
        }

        return Controller.notFound(Json.toJson(new Result_NotFound("Object " + cls.getSimpleName().replace("Model_", "").replace("Swagger_", "") + " not found.")));
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
        try {

            if (error instanceof Result_Error_BadRequest) {

                return badRequest(error.getMessage());

            } else if (error instanceof Result_Error_InvalidBody) {

                return badRequest(Json.toJson(new Result_InvalidBody(((Result_Error_InvalidBody) error).getForm_error())));

            } else if (error instanceof NotFoundException) {

                return notFound(((NotFoundException) error).getEntity());

            } else if (error instanceof Result_Error_PermissionDenied) {

                return forbidden();

            } else if (error instanceof ForbiddenException) {

                return forbidden();

            } else if (error instanceof Result_Error_Unauthorized) {

                return unauthorized();

            } else if (error instanceof Result_Error_NotSupportedException) {

                return badRequest(Json.toJson(new Result_UnsupportedException()));
            }

            return internalServerError(error);

        } catch (Exception e) {
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
        error.printStackTrace();
        StackTraceElement current_stack = Thread.currentThread().getStackTrace()[2]; // Find the caller origin

        try {
            ServerLogger.error(error, current_stack.getClassName() + "::" + current_stack.getMethodName(), Controller.request());
        } catch (Exception e) {
            ServerLogger.error(error, current_stack.getClassName() + "::" + current_stack.getMethodName(), null);
        }
        return Controller.internalServerError(Json.toJson(new Result_InternalServerError()));
    }

    //**********************************************************************************************************************

    public static void check_latency(){
        try{
            if(Http.Context.current().args.containsKey("tyrion_response_measurement_time")){
                RequestLatency.count_end( (String) Http.Context.current().args.get("tyrion_response_measurement_method"), (String) Http.Context.current().args.get("tyrion_response_measurement_path"), ( new Date().getTime() - (long) Http.Context.current().args.get("tyrion_response_measurement_time")));
            }
        }catch (Exception e){
            // nothing !
        }
    }


    //**********************************************************************************************************************

}
