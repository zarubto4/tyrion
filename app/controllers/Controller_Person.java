package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import io.swagger.annotations.*;
import models.*;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.Server;
import utilities.authentication.Authentication;
import utilities.emails.Email;
import utilities.enums.NotificationAction;
import utilities.logger.Logger;
import utilities.notifications.NotificationActionHandler;
import utilities.swagger.input.*;
import utilities.swagger.output.Swagger_Entity_Validation_Out;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck") // Překrývá nezdokumentované API do jednotné serverové kategorie ve Swaggeru.
public class Controller_Person extends _BaseController {
    
// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Person.class);

// CONTROLLER CONFIGURATION ############################################################################################

    private _BaseFormFactory baseFormFactory;
    private WSClient ws;

    @Inject public Controller_Person(_BaseFormFactory formFactory, WSClient ws) {
        this.baseFormFactory = formFactory;
        this.ws = ws;
    }

//######################################################################################################################

    @ApiOperation(value = "create Person",
            tags = {"Person"},
            notes = "create new Person with unique email and nick_name",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Person_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully created",    response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result person_create() {
        try {

            // Get and Validate Object
            Swagger_Person_New help = baseFormFactory.formFromRequestWithValidation(Swagger_Person_New.class);

            if (Model_Person.find.query().where().eq("nick_name", help.nick_name).findOne() != null)
                return badRequest("nick name is used");
            if (Model_Person.getByEmail(help.email) != null)
                return badRequest("Email is registered");

            Model_Person person = new Model_Person();

            person.nick_name =  help.nick_name;
            person.first_name = help.first_name;
            person.last_name = help.last_name;
            person.email = help.email;
            person.validated = false;

            person.setPassword(help.password);
            person.save();

            Model_Invitation invitation = Model_Invitation.find.query().where().eq("mail", person.email).findOne();

            if (invitation == null) {

                Model_ValidationToken validationToken = Model_ValidationToken.find.query().where().eq("email",help.email).findOne();
                if (validationToken!=null) validationToken.delete();
                validationToken = new Model_ValidationToken().setValidation(person.email);

                String link = Server.httpAddress + "/person/mail_authentication/" + validationToken.token;

                try {

                    new Email()
                            .text("Email verification is needed to complete your registration.")
                            .divider()
                            .link("Verify your email address",link)
                            .send(validationToken.email, "Email Verification");

                } catch (Exception e) {
                    logger.internalServerError(e);
                }

            } else {
                person.validated = true;
                person.update();

                try {
                    NotificationActionHandler.perform(NotificationAction.ACCEPT_PROJECT_INVITATION, invitation.id.toString());
                } catch (IllegalArgumentException e) {
                    person.notification_error(e.getMessage());
                } catch (Exception e) {
                    logger.internalServerError(e);
                }
            }

            return ok();
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "authenticate Email from registration",
            tags = {"Admin-Person"},
            notes = "sends authentication email, if user did not get the first one from the registration",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 404, message = "Not found object",        response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result person_emailAuthentication(String authToken) {
        try {

            Model_ValidationToken validationToken = Model_ValidationToken.find.query().where().eq("token", UUID.fromString(authToken)).findOne();

            if (validationToken == null) return redirect(Server.becki_mainUrl + "/" + Server.becki_redirectOk  );

            Model_Person person = Model_Person.find.query().where().eq("mail", validationToken.email).findOne();
            if (person == null) return redirect( Server.becki_mainUrl + "/" + Server.becki_redirectFail  );

            person.validated = true;
            person.update();

           validationToken.delete();

            return redirect( Server.becki_mainUrl + "/" + Server.becki_accountAuthorizedSuccessful );
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "send Email authentication",
            tags = {"Person"},
            notes = "sends authentication email, if user did not get the first one from the registration",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_EmailRequired",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 404, message = "Not found object",        response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result person_authenticationSendEmail() {
        try {

            // Get and Validate Object
            Swagger_EmailRequired help = baseFormFactory.formFromRequestWithValidation(Swagger_EmailRequired.class);

            Model_Person person = Model_Person.getByEmail(help.email);
            if (person == null) return notFound("No such user is registered");
            if (person.validated) return badRequest("This user is validated");

            Model_ValidationToken validationToken = Model_ValidationToken.find.query().where().eq("email", help.email).findOne();
            if (validationToken == null) return notFound("Validation token not found");

            String link = Server.httpAddress + "/person/mail_authentication/" + validationToken.token;

            try {

                new Email()
                        .text("Email verification is needed to complete your registration.")
                        .divider()
                        .link("Verify your email address",link)
                        .send(validationToken.email, "Email Verification");

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            return ok();
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "send Email password recovery email",
            tags = {"Access"},
            notes = "sends email with link for changing forgotten password",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_EmailRequired",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result person_passwordRecoverySendEmail() {
        try {

            // Get and Validate Object
            Swagger_EmailRequired help = baseFormFactory.formFromRequestWithValidation(Swagger_EmailRequired.class);

            String link;

            Model_Person person = Model_Person.getByEmail(help.email);
            if (person == null) return ok();

            Model_PasswordRecoveryToken previousToken = Model_PasswordRecoveryToken.find.query().where().eq("person_id", person.id).findOne();

            if (previousToken != null && new Date().getTime() - previousToken.created.getTime() > 900000) {
                previousToken.delete();
                previousToken = null;
            }

            if (previousToken == null) {

                Model_PasswordRecoveryToken passwordRecoveryToken = new Model_PasswordRecoveryToken();
                passwordRecoveryToken.setPasswordRecoveryToken();
                passwordRecoveryToken.person = person;
                passwordRecoveryToken.save();

                link = Server.becki_mainUrl + "/" +  Server.becki_passwordReset + "/" + passwordRecoveryToken.password_recovery_token;
            } else {
                link = Server.becki_mainUrl + "/" +  Server.becki_passwordReset + "/" + previousToken.password_recovery_token;
            }
            try {

                new Email()
                        .text("Password reset was requested for this email.")
                        .divider()
                        .link("Reset your password", link)
                        .send(help.email,"Password Reset");

            } catch (Exception e) {
                logger.internalServerError(e);
            }
            return ok();
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "restart Person password",
            tags = {"Access"},
            notes = "changes password if password_recovery_token is not older than 24 hours, deletes all FloatingPersonTokens",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Person_Password_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result person_passwordRecovery() {
        try {

            // Get and Validate Object
            Swagger_Person_Password_New help = baseFormFactory.formFromRequestWithValidation(Swagger_Person_Password_New.class);

            Model_Person person = Model_Person.getByEmail(help.email);

            Model_PasswordRecoveryToken passwordRecoveryToken = Model_PasswordRecoveryToken.find.query().where().eq("password_recovery_token", help.password_recovery_token).findOne();
            if (passwordRecoveryToken == null) return badRequest("Password change was unsuccessful");

            if (person == null || !passwordRecoveryToken.person.id.equals(person.id)) {
                passwordRecoveryToken.delete();
                return badRequest("Password change was unsuccessful");
            }

            if (new Date().getTime() - passwordRecoveryToken.created.getTime() > 86400000) {
                passwordRecoveryToken.delete();
                return badRequest("You must recover your password in 24 hours.");
            }

            for (Model_AuthorizationToken floatingPersonToken : Model_AuthorizationToken.find.query().where().eq("person.id",  personId()).findList()) {
                floatingPersonToken.delete();
            }

            person.refresh();
            person.setPassword(help.password);
            person.validated = true;
            person.update();

            passwordRecoveryToken.delete();

            try {
                new Email()
                        .text("Password was changed for your account.")
                        .send(help.email,"Password Reset");

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            return ok("Password was changed successfully");
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get Person",
            tags = {"Person"},
            notes = "get Person by id",
            produces = "application/json",
            protocols = "https"
    )
      @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",               response = Model_Person.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public  Result person_get(@ApiParam(value = "person_id String query", required = true)  String person_id) {
        try {

            Model_Person person = Model_Person.getById(person_id);
            if (person == null)  return notFound("Person person_id not found");
            return ok(Json.toJson(person));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete Person",
            tags = {"Admin-Person"},
            notes = "delete Person by id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public  Result person_delete(@ApiParam(value = "person_id String query", required = true) String person_id) {
        try {

            Model_Person person = Model_Person.getById(person_id);

            person.delete();

            return ok();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "remove  Connection_token All",
            tags = {"Admin-Person"},
            notes = "remove all connection tokens",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result person_removeAllConnections(@ApiParam(value = "person_id String query", required = true) String person_id) {
        try {

            for(Model_AuthorizationToken token : Model_AuthorizationToken.find.query().where().eq("person.id",  person().id).findList()) {
                token.delete();
            }

            return ok();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "activate Person",
            tags = {"Admin-Person"},
            notes = "activate Person by id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result person_activate(@ApiParam(value = "person_id String query", required = true) String person_id) {
        try {

            Model_Person person = Model_Person.getById(person_id);

            person.check_activation_permission();

            if (!person.frozen) return badRequest("Person is already active.");

            person.frozen = false;
            person.update();

            return ok();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "deactivate Person",
            tags = {"Admin-Person"},
            notes = "deactivate Person by id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result person_deactivate(@ApiParam(value = "person_id String query", required = true) String person_id) {
        try {

            Model_Person person = Model_Person.getById(person_id);

            person.check_activation_permission();

            if (person.frozen) return badRequest("Person is already deactivated.");

            person.frozen = true;

            for(Model_AuthorizationToken token : Model_AuthorizationToken.find.query().where().eq("person.id",  personId()).findList()) {
                token.delete();
            }

            person.update();

            return ok();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "valid Person email",
            tags = {"Admin-Person"},
            notes = "valid Person email by id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result person_validEmail(@ApiParam(value = "person_id String query", required = true) String person_id) {
        try {

            Model_Person person = Model_Person.getById(person_id);

            person.validated = true;
            person.update();

            return ok();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit Person",
            tags = {"Person"},
            notes = "Edit person basic information",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Person_Update",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully updated",    response = Model_Person.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public  Result person_update(@ApiParam(value = "person_id String query", required = true) String person_id) {
        try {

            // Get and Validate Object
            Swagger_Person_Update help = baseFormFactory.formFromRequestWithValidation(Swagger_Person_Update.class);

            Model_Person person = Model_Person.getById(person_id);

            person.nick_name  = help.nick_name;
            person.first_name = help.first_name;
            person.last_name  = help.last_name;

            if (help.country != null && (!help.country.equals("")))
                person.country = help.country;

            if (help.gender != null && !help.gender.equals("") && (help.gender.equals("male") || help.gender.equals("female")))
                person.gender = help.gender;

            person.update();

            return ok(person.json());

         } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get Person logged connections",
            tags = {"Person"},
            notes = "get all connections, where user is logged",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK result",               response = Model_AuthorizationToken.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not Found object",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public  Result person_getAllConnections() {
        try {

           return ok(Json.toJson( Model_AuthorizationToken.find.query().where().eq("person.id",  personId()).findList() ));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete Person logged connections",
            tags = {"Person"},
            notes = "You know where the user is logged in. And you can log out this connection. (Terminate token)",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK result",               response = Result_Ok.class),
            @ApiResponse(code = 404, message = "Not Found object",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public  Result remove_Person_Connection(@ApiParam(value = "connection_id String query", required = true) String connection_id) {
        try {

            Model_AuthorizationToken token = Model_AuthorizationToken.getById(connection_id);

            token.delete();

            return ok();
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "validation Entity",
            tags = {"Person"},
            notes = "for cyclical validation during registration, key contains 'mail' or 'nick_name'. Or can be used for 'vat_number' as a key.",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Entity_Validation_In",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Result if it is possible to use",       response = Swagger_Entity_Validation_Out.class),
            @ApiResponse(code = 400, message = "Something is wrong",                    response = Result_BadRequest.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public  Result something_validateProperty() {
        try {

            // Get and Validate Object
            Swagger_Entity_Validation_In help = baseFormFactory.formFromRequestWithValidation(Swagger_Entity_Validation_In.class);


            Swagger_Entity_Validation_Out validation = new Swagger_Entity_Validation_Out();


            switch (help.key) {

                case "mail":{
                    if (Model_Person.find.query().where().ieq("mail", help.value).findOne() == null) {

                        validation.valid = true;
                        return ok(Json.toJson(validation));
                    }

                    validation.valid = false;
                    validation.message = "email is used";

                    break;
                }

                case "nick_name" : {
                    if (Model_Person.find.query().where().ieq("nick_name", help.value).findOne() == null) {

                        validation.valid = true;
                        return ok(Json.toJson(validation));
                    }

                    validation.valid = false;
                    validation.message = "nick_name is used";

                    break;
                }

                case "vat_number":{

                    try {

                        logger.debug("person_validateProperty:: Link:: " + "https://www.isvat.eu/" + help.value.substring(0, 2) + "/" + help.value.substring(2));

                        WSResponse wsResponse = ws.url("https://www.isvat.eu/" + help.value.substring(0, 2) + "/" + help.value.substring(2))
                                .setRequestTimeout(Duration.ofSeconds(10))
                                .get()
                                .toCompletableFuture()
                                .get();

                        JsonNode result = wsResponse.asJson();

                        logger.debug("person_validateProperty: http request: {} ", wsResponse.getStatus());
                        logger.debug("person_validateProperty: vat_number: {} ", result);

                        if (result.get("valid").asBoolean()) {

                            validation.valid = true;
                            try {
                                validation.message = result.get("name").get("0").asText();
                            } catch (Exception e) {
                                // do nothing
                            }
                            return ok(Json.toJson(validation));
                        }

                    } catch (RuntimeException e) {

                        validation.message = "vat_number is not valid or could not be found";
                        validation.valid = false;
                        return  ok(Json.toJson(validation));

                    } catch (Exception e) {
                        logger.internalServerError(e);
                        validation.valid = false;
                        validation.message = "vat_number is not valid or could not be found";

                        return  ok(Json.toJson(validation));
                    }

                    break;
                }

                default:return badRequest("Key does not exist, use only {mail, nick_name or vat_number}");
            }

            return ok(Json.toJson(validation));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit Person property",
            tags = {"Person"},
            notes = "Request password or email change. API does not change password or email, only sends email for authorization of the change and holds values in different object." +
                    "JSON value 'property' contains only 'password' or 'email'",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Person_ChangeProperty",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result person_changeLoginProperty() {
        try {

            // Get and Validate Object
            Swagger_Person_ChangeProperty help = baseFormFactory.formFromRequestWithValidation(Swagger_Person_ChangeProperty.class);

            if (Model_ChangePropertyToken.find.query().where().eq("person.id", _BaseController.personId()).findOne() != null) return badRequest("You can request only one change at this time.");

            // Proměnné mailu
            String subject;
            String text;
            String link;

            switch (help.property) {

                case "password":{

                    if (help.password == null) return badRequest("You must fill in the password");

                    // Vytvoření tokenu pro podržení hesla
                    Model_ChangePropertyToken changePropertyToken = new Model_ChangePropertyToken();
                    changePropertyToken.person = person();
                    changePropertyToken.property = help.property;
                    changePropertyToken.value = help.password;
                    changePropertyToken.save();

                    // Úprava proměnných mailu
                    subject = "Password change - need authorization";
                    text = "Password change was requested for your account. Click on the link below to authorize the change.";
                    link = Server.httpAddress + "/person/authorize_change/" + changePropertyToken.id;

                    break;}

                case "email":{

                    if (help.email == null) return badRequest("You must fill in the email");

                    // Vytvoření tokenu pro podržení emailu
                    Model_ChangePropertyToken changePropertyToken = new Model_ChangePropertyToken();
                    changePropertyToken.person = person();
                    changePropertyToken.property = help.property;
                    changePropertyToken.value = help.email;
                    changePropertyToken.save();

                    // Úprava proměnných mailu
                    subject = "Email change - need authorization";
                    text = "Email change was requested for your account. Click on the link below to authorize the change. Verification email will be sent to your new email";
                    link = Server.httpAddress + "/person/authorize_change/" + changePropertyToken.id;

                    break;}

                default: return badRequest("No such property");
            }

            // Odeslání emailu
            try {

                new Email()
                        .text(text)
                        .divider()
                        .text("If you do not recognize any of this activity, we strongly recommend you to go to your account and change your password there, because it was probably stolen.")
                        .divider()
                        .link("Authorize change",link)
                        .send(person().email, subject);

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            return ok("Change was requested. You must authorize the change in next 4 hours via your email. Authorization email was sent.");

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "email Person approving password or email change",
            tags = {"Admin-Person"},
            notes = "",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Person_ChangeProperty",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result person_authorizePropertyChange(String token) {
        try {
            Model_ChangePropertyToken changePropertyToken = Model_ChangePropertyToken.getById(token);
            if (changePropertyToken == null) return redirect(Server.becki_mainUrl + "/" + Server.becki_propertyChangeFailed);

            if (((new Date()).getTime() - changePropertyToken.created.getTime()) > 14400000 ) {
                changePropertyToken.delete();
                return redirect(Server.becki_mainUrl + "/" + Server.becki_propertyChangeFailed);
            }

            Model_Person person = Model_Person.getById(changePropertyToken.person.id);
            if (person == null) return redirect(Server.becki_mainUrl + "/" +  Server.becki_propertyChangeFailed);

            switch (changePropertyToken.property) {

                case "password":{
                    // Úprava objektu
                    person.setPassword(changePropertyToken.value);
                    person.update();
                    break;
                }

                case "email":{

                    // Úprava objektu
                    person.email = changePropertyToken.value;
                    person.validated = false;
                    person.update();

                    // Vytvoření validačního tokenu
                    Model_ValidationToken validationToken = Model_ValidationToken.find.query().where().eq("email",person.email).findOne();
                    if (validationToken!=null) validationToken.delete();
                    validationToken = new Model_ValidationToken().setValidation(person.email);

                    String link = Server.httpAddress + "/person/mail_authentication/" + validationToken.token;

                    // Odeslání emailu
                    try {
                        new Email()
                                .text("Email verification is needed to complete your registration.")
                                .divider()
                                .link("Verify your email address",link)
                                .send(validationToken.email, "Email Verification");

                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                    break;
                }
            }

            // Odhlášení uživatele všude
            for ( Model_AuthorizationToken floatingPersonToken : Model_AuthorizationToken.find.query().where().eq("person.id",  personId()).findList()) {
                floatingPersonToken.delete();
            }

            changePropertyToken.delete();

            return redirect(Server.becki_mainUrl);

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "upload Person picture",
            tags = {"Person"},
            notes = "Uploads personal photo. Picture must be smaller than 800 KB and its dimensions must be between 50 and 400 pixels. If user already has a picture, it will be replaced by the new one. " +
                    "API requires base64 Content-Type, name of the property is 'file'.",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_BASE64_FILE",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    @BodyParser.Of(value = BodyParser.Json.class)
    public Result person_uploadPicture() {
        try {

            // Get and Validate Object
            Swagger_BASE64_FILE help = baseFormFactory.formFromRequestWithValidation(Swagger_BASE64_FILE.class);

            Model_Person person = person();
            if (person ==  null) {
                return badRequest("User not found"); // Just for compiler Error
            }

          // Odeberu cache - jen projistotu
            person.cache_picture_link = null;

            // Pokud tu byl nějaký soubor - smažu ho - prázdný soubor je příkaz ke smazání
            if (help.file == null || help.file.equals("")) {
                Model_Blob fileRecord = person.picture;
                person.picture = null;
                person.alternative_picture_link = "";
                person.update();
                fileRecord.refresh();
                fileRecord.delete();

                return ok();
            }

            //  data:image/png;base64,
            //  data:image/png;base64,
            String[] parts = help.file.split(",");
            String[] type = parts[0].split(":");
            String[] content_type = type[1].split(";");
            String dataType = content_type[0].split("/")[1];

            logger.debug("bootLoader_uploadFile:: Cont Type:" + content_type[0] + ":::");
            logger.debug("bootLoader_uploadFile:: Data Type:" + dataType + ":::");
            logger.debug("bootLoader_uploadFile:: Data: " + parts[1].substring(0, 10) + "......");

            // Odebrání předchozího obrázku
            if (person.picture != null) {
                logger.debug("person_uploadPicture:: Removing previous picture");
                Model_Blob fileRecord = person.picture;
                person.picture = null;
                person.alternative_picture_link = "";
                person.update();
                fileRecord.delete();
            }

            // Pokud link není, vygeneruje se nový, unikátní
            if (person.alternative_picture_link == null || person.alternative_picture_link.equals("")) {
                person.alternative_picture_link = person.get_Container().getName() + "/" + UUID.randomUUID().toString() + ".png";
                person.update();
            }

            // Pouze pro případy, kdy se uživatel registroval skrze sociální síť a Tyrion používá obrázek daného uživatele
            // Z konrkétní sociální sítě - pak chybí soubor, ale existuje cesta k souboru, kterou zaslí tyrion do Becki
            // Například:: https://avatars1.githubusercontent.com/u/16296782?v=3
            // PRoto je nutné na to pamatovat - jinak se pak taková cesta strká do Azure k přepsání předchozího obrázku
            if (person.alternative_picture_link.contains("http")) {
                person.alternative_picture_link = null;
                person.update();
            }


            String file_name =  UUID.randomUUID().toString() + ".jpg";
            String file_path =  person.get_Container().getName() + "/" +file_name;

            logger.debug("person_uploadPicture::  File Name " + file_name );
            logger.debug("person_uploadPicture::  File Path " + file_path );

            person.picture = Model_Blob.uploadAzure_File( parts[1], content_type[0], file_name, file_path);
            person.update();


            return ok("Picture successfully uploaded");
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete Person picture",
            tags = {"Person"},
            notes = "Removes picture of logged person",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result person_removePicture() {
        try {

            Model_Person person = person();

            if (!(person.picture == null)) {
                Model_Blob fileRecord = person.picture;
                person.picture = null;
                person.alternative_picture_link = null;
                person.update();
                fileRecord.delete();
            } else {
                return badRequest("There is no picture to remove.");
            }

            return ok("Picture successfully removed");
        } catch (Exception e) {
            return internalServerError(e);
        }
    }


//######################################################################################################################

}
