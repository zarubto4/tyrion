package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Model_Person;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import responses.*;
import utilities.authentication.Attributes;
import utilities.logger.Logger;
import utilities.logger.ServerLogger;

import java.util.UUID;

/**
 * This class provides some common API for Tyrion REST Controller.
 * Creates results with given content.
 */
public abstract class BaseController extends Controller {

    private static final Logger logger = new Logger(BaseController.class);

    /**
     * Pulls up the current user from the request context.
     * Calling this method in non-authenticated context will throw an exception.
     * @return current person {@link Model_Person}
     */
    public static Model_Person person() {
        try {
            return (Model_Person) ctx().args.get("person");
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    /**
     * Pulls up the current user from the request context.
     * Calling this method in non-authenticated context will throw an exception.
     * @return current person id {@link UUID}
     */
    public static UUID personId() {
        try {
            return ((Model_Person) ctx().args.get("person")).id;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    /**
     * Returns true if there is a authenticated person in the context.
     * @return current person id {@link UUID}
     */
    public static boolean isAuthenticated() {
        try {
            return ctx().args.containsKey("person");
        } catch (Exception e) {
            logger.internalServerError(e);
            return false;
        }
    }

    /**
     * Creates a result based on the provided status code.
     * @param statusCode integer of
     * @param message string to send in result
     * @return result with status code and message
     */
    public static Result customResult(int statusCode, String message) {
        Result_Custom result = new Result_Custom();
        result.code = statusCode;
        result.message = message;
        return status(statusCode, Json.toJson(result));
    }

    /**
     * Create an empty ok result.
     * @return 200 result
     */
    public static Result okEmpty() {
        return ok(Json.toJson(new Result_Ok()));
    }

    /**
     * Creates a result with the code 200 and provided json as the body.
     * @param json JsonNode serialized object
     * @return 200 result ok with json
     */
    public static Result ok(JsonNode json) {
        return Controller.ok(json);
    }

    /**
     * Creates an ok result with given message.
     * @param message string to be sent
     * @return 200 result with message
     */
    public static Result ok(String message) {
        return ok(Json.toJson(new Result_Ok(message)));
    }

    /**
     * Creates redirect result.
     * @param url to redirect to
     * @return 303 result
     */
    public static Result redirect(String url) {
        return Controller.redirect(url);
    }

    public static Result pdfFile(byte[] byte_array, String  file_name) {
        response().setHeader("filename", file_name);
        return ok(byte_array);
    }

    public static Result binFile(byte[] byte_array, String  file_name) {
        response().setHeader("filename", file_name);
        return ok(byte_array);
    }

    /**
     * Creates result created. Body of this result is some object itself instead of classic result json.
     * @param json to send
     * @return 201 result
     */
    public static Result created(JsonNode json) {
        return Controller.created(json);
    }

    /**
     * Creates result bad request, when there is a client error.
     * @return 400 result
     */
    public static Result badRequestEmpty() {
        return badRequest(Json.toJson(new Result_BadRequest()));
    }

    /**
     * Creates result bad request with message, when there is a client error.
     * @param message to send
     * @return 400 result with message
     */
    public static Result badRequest(String message) {
        return badRequest(Json.toJson(new Result_BadRequest(message)));
    }

    /**
     * Creates result bad request with provided json as a body, when there is a client error.
     * @param json to send
     * @return 400 result with message
     */
    public static Result badRequest(JsonNode json) {
        return Controller.badRequest(Json.toJson(json));
    }

    /**
     * Creates result when compilation was unsuccessful.
     * @param json describing errors
     * @return 422 result
     */
    public static Result buildErrors(JsonNode json) {
        return status(422, Json.toJson(json));
    }

    /**
     * Creates result when target server is offline
     * @param message to send
     * @return 477 result
     */
    public static Result externalServerOffline(String message) {
        return status(477, Json.toJson(new Result_ServerOffline(message)));
    }

    /**
     * Creates result signaling that error occurred outside this server.
     * @return 478 result
     */
    public static Result externalServerError() {
        return status(478, Json.toJson(new Result_ExternalServerSideError()));
    }

    /**
     * Creates result signaling that error occurred outside this server.
     * @param message describing error
     * @return 478 result with message
     */
    public static Result externalServerError(String message) {
        return status(478, Json.toJson(new Result_ExternalServerSideError(message)));
    }

    /**
     * Creates result signaling that error occurred outside this server.
     * @param json with error
     * @return 478 result with json body
     */
    public static Result externalServerError(JsonNode json) {
        return status(478, json);
    }

    /**
     * Creates a not found result with message.
     * @param message to be sent
     * @return 404 result with message
     */
    public static Result notFound(String message) {
        return notFound(Json.toJson(new Result_NotFound(message)));
    }

    /**
     * Creates result unauthorized.
     * Signals that user have to be logged in.
     * @return 401 result
     */
    public static Result unauthorizedEmpty() {
        return unauthorized(Json.toJson(new Result_Unauthorized()));
    }

    /**
     * Creates result forbidden
     * @return 403 result
     */
    public static Result forbiddenEmpty() {
        return forbidden(Json.toJson(new Result_Forbidden()));
    }

    /**
     * Creates result forbidden with custom message.
     * @param message to send
     * @return 403 result with message
     */
    public static Result forbidden(String message) {
        return forbidden(Json.toJson(new Result_Forbidden(message)));
    }

    /**
     * Creates result to reject the user, when his email is not validated.
     * @return 705 result
     */
    public static Result notValidated() {
        return status(705, Json.toJson(new Result_NotValidated()));
    }

    /**
     * Creates result with custom message to reject the user, when his email is not validated.
     * @return 705 result with message
     */
    public static Result notValidated(String message) {
        return status(705, Json.toJson(new Result_NotValidated(message)));
    }

    /**
     * Creates result with code 400. Used when binding incoming json to form.
     * If form has error, they are returned in the exception field.
     * @param errors JsonNode with errors
     * @return 400 result
     */
    public static Result invalidBody(JsonNode errors) {
        return badRequest(Json.toJson(new Result_InvalidBody(errors)));
    }

    /**
     * Creates result internal server error and also saves the error to the database.
     * @param error that was thrown
     * @return 500 result
     */
    public static Result internalServerError(Throwable error) {
        StackTraceElement current_stack = Thread.currentThread().getStackTrace()[2]; // Find the caller origin
        ServerLogger.error(error, current_stack.getClassName() + "::" + current_stack.getMethodName(), request());
        return internalServerError(Json.toJson(new Result_InternalServerError()));
    }
}
