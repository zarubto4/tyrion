package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.*;
import models.*;
import play.api.Play;
import play.data.Form;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import play.mvc.Security;
import utilities.Server;
import utilities.emails.Email;
import utilities.enums.Enum_Notification_action;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_API;
import utilities.notifications.NotificationActionHandler;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Swagger_Entity_Validation_Out;

import java.util.Date;
import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck") // Překrývá nezdokumentované API do jednotné serverové kategorie ve Swaggeru.
public class Controller_Person extends Controller {
    
// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Person.class);

//######################################################################################################################

    @ApiOperation(value = "create Person",
            tags = {"Person"},
            notes = "create new Person with unique email and nick_name",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Person_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully created",    response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result person_create() {
        try {

            final Form<Swagger_Person_New> form = Form.form(Swagger_Person_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Person_New help = form.get();

            if (Model_Person.find.where().eq("nick_name", help.nick_name).findUnique() != null)
                return GlobalResult.result_badRequest("nick name is used");
            if (Model_Person.find.where().eq("mail", help.mail).findUnique() != null)
                return GlobalResult.result_badRequest("Email is registered");


            Model_Person person = new Model_Person();

            person.nick_name =  help.nick_name;
            person.full_name = help.full_name;
            person.mail = help.mail;
            person.mailValidated = false;

            person.setSha(help.password);
            person.save();

            Model_Invitation invitation = Model_Invitation.find.where().eq("mail", person.mail).findUnique();

            if(invitation == null) {

                Model_ValidationToken validationToken = Model_ValidationToken.find.where().eq("personEmail",help.mail).findUnique();
                if(validationToken!=null) validationToken.delete();
                validationToken = new Model_ValidationToken().setValidation(person.mail);

                String link = Server.tyrion_serverAddress + "/person/mail_authentication/" + validationToken.authToken;

                try {

                    new Email()
                            .text("Email verification is needed to complete your registration.")
                            .divider()
                            .link("Verify your email address",link)
                            .send(validationToken.personEmail, "Email Verification");

                } catch (Exception e) {
                    terminal_logger.internalServerError("person_create:", e);
                }

            }else{
                person.mailValidated = true;
                person.update();

                try {

                    NotificationActionHandler.perform(Enum_Notification_action.accept_project_invitation, invitation.id);

                } catch (IllegalArgumentException e) {

                    person.notification_error(e.getMessage());

                } catch (Exception e) {

                    terminal_logger.internalServerError("person_create:", e);
                }
            }

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "authenticate Email from registration",
            tags = {"Admin-Person"},
            notes = "sends authentication email, if user did not get the first one from the registration",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Person_Authentication",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 404, message = "Not found object",        response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result person_emailAuthentication(String authToken) {
        try{

            Model_ValidationToken validationToken = Model_ValidationToken.find.where().eq("authToken", authToken).findUnique();

            if (validationToken == null) return GlobalResult.redirect(Server.becki_mainUrl + "/" + Server.becki_redirectOk  );

            Model_Person person = Model_Person.find.where().eq("mail", validationToken.personEmail).findUnique();
            if(person == null) return GlobalResult.redirect( Server.becki_mainUrl + "/" + Server.becki_redirectFail  );

            person.mailValidated = true;
            person.update();

           validationToken.delete();

            return GlobalResult.redirect( Server.becki_mainUrl + "/" + Server.becki_accountAuthorizedSuccessful );
        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "send Email authentication",
            tags = {"Person"},
            notes = "sends authentication email, if user did not get the first one from the registration",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Person_Authentication",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 404, message = "Not found object",        response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result person_authenticationSendEmail() {
        try{

            final Form<Swagger_Person_Authentication> form = Form.form(Swagger_Person_Authentication.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Person_Authentication help = form.get();

            Model_Person person = Model_Person.find.where().eq("mail", help.mail).findUnique();
            if (person == null) return GlobalResult.result_notFound("No such user is registered");
            if (person.mailValidated) return GlobalResult.result_badRequest("This user is validated");

            Model_ValidationToken validationToken = Model_ValidationToken.get_byId(help.mail);
            if (validationToken == null) return GlobalResult.result_notFound("Validation token not found");

            String link = Server.tyrion_serverAddress + "/person/mail_authentication/" + validationToken.authToken;

            try {

                new Email()
                        .text("Email verification is needed to complete your registration.")
                        .divider()
                        .link("Verify your email address",link)
                        .send(validationToken.personEmail, "Email Verification");

            } catch (Exception e) {
                terminal_logger.internalServerError("person_authenticationSendEmail:", e);
            }

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "send Email password recovery email",
            tags = {"Access"},
            notes = "sends email with link for changing forgotten password",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Person_Password_RecoveryEmail",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result person_passwordRecoverySendEmail(){
        try{

            final Form<Swagger_Person_Password_RecoveryEmail> form = Form.form(Swagger_Person_Password_RecoveryEmail.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Person_Password_RecoveryEmail help = form.get();

            String link;

            Model_Person person = Model_Person.find.where().eq("mail", help.mail).findUnique();
            if(person == null) return GlobalResult.result_ok();

            Model_PasswordRecoveryToken previousToken = Model_PasswordRecoveryToken.find.where().eq("person_id",person.id).findUnique();

            if(!(previousToken == null)) if(((new Date()).getTime() - previousToken.time_of_creation.getTime()) > 900000)
            {
                previousToken.delete();
                previousToken = null;
            }

            if(previousToken == null){

                Model_PasswordRecoveryToken passwordRecoveryToken = new Model_PasswordRecoveryToken();
                passwordRecoveryToken.setPasswordRecoveryToken();
                passwordRecoveryToken.person = person;
                passwordRecoveryToken.time_of_creation = new Date();
                passwordRecoveryToken.save();

                link =Server.becki_mainUrl + "/" +  Server.becki_passwordReset + "/" + passwordRecoveryToken.password_recovery_token;
            }else {
                link =Server.becki_mainUrl + "/" +  Server.becki_passwordReset + "/" + previousToken.password_recovery_token;
            }
            try {

                new Email()
                        .text("Password reset was requested for this email.")
                        .divider()
                        .link("Reset your password",link)
                        .send(help.mail,"Password Reset");

            } catch (Exception e) {
                terminal_logger.internalServerError("person_passwordRecoverySendEmail:", e);
            }
            return GlobalResult.result_ok();
        }catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "restart Person password",
            tags = {"Access"},
            notes = "changes password if password_recovery_token is not older than 24 hours, deletes all FloatingPersonTokens",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Person_Password_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result person_passwordRecovery() {
        try{

            final Form<Swagger_Person_Password_New> form = Form.form(Swagger_Person_Password_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Person_Password_New help = form.get();

            Model_Person person = Model_Person.find.where().eq("mail", help.mail).findUnique();

            Model_PasswordRecoveryToken passwordRecoveryToken = Model_PasswordRecoveryToken.find.where().eq("password_recovery_token", help.password_recovery_token).findUnique();
            if(passwordRecoveryToken == null) return GlobalResult.result_badRequest("Password change was unsuccessful");

            if(person == null || !passwordRecoveryToken.person.id.equals(person.id)) {
                passwordRecoveryToken.delete();
                return GlobalResult.result_badRequest("Password change was unsuccessful");
            }

            if(((new java.util.Date()).getTime() - passwordRecoveryToken.time_of_creation.getTime()) > 86400000 ){
                passwordRecoveryToken.delete();
                return GlobalResult.result_badRequest("You must recover your password in 24 hours.");
            }

            for ( Model_FloatingPersonToken floatingPersonToken : person.floatingPersonTokens  ) {
                floatingPersonToken.delete();
            }

            person.shaPassword = null;
            person.update();

            person.refresh();
            person.setSha(help.password);
            person.update();

            passwordRecoveryToken.delete();

            try {

                new Email()
                        .text("Password was changed for your account.")
                        .send(help.mail,"Password Reset");

            } catch (Exception e) {
                terminal_logger.internalServerError("person_passwordRecovery:", e);
            }

            return GlobalResult.result_ok("Password was changed successfully");
        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Person",
            tags = {"Person"},
            notes = "get Person by id",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
      @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",               response = Model_Person.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Secured_API.class)
    public  Result person_get(@ApiParam(value = "person_id String query", required = true)  String person_id){
        try{

            Model_Person person = Model_Person.get_byId(person_id);
            if(person == null )  return GlobalResult.result_notFound("Person person_id not found");
            return GlobalResult.result_ok(Json.toJson(person));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Person",
            tags = {"Admin-Person"},
            notes = "delete Person by id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Person.delete_permission", value = "true"),
                    })
            }


    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Secured_API.class)
    public  Result person_delete(@ApiParam(value = "person_id String query", required = true) String person_id){
        try{

            Model_Person person = Model_Person.get_byId(person_id);
            if(person == null ) return GlobalResult.result_notFound("Person person_id not found");


            if (!person.delete_permission())  return GlobalResult.result_forbidden();
            person.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove  Connection_token All",
            tags = {"Admin-Person"},
            notes = "remove all connection tokens",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Person.activation_permission", value = "true"),
                    })
            }


    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Secured_API.class)
    public Result person_removeAllConnections(@ApiParam(value = "person_id String query", required = true) String person_id){
        try{

            Model_Person person = Model_Person.get_byId(person_id);
            if(person == null ) return GlobalResult.result_notFound("Person person_id not found");

            if (!person.edit_permission())  return GlobalResult.result_forbidden();

            for(Model_FloatingPersonToken token : person.floatingPersonTokens) token.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "activate Person",
            tags = {"Admin-Person"},
            notes = "activate Person by id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Person.activation_permission", value = "true"),
                    })
            }


    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Secured_API.class)
    public Result person_activate(@ApiParam(value = "person_id String query", required = true) String person_id){
        try{

            Model_Person person = Model_Person.get_byId(person_id);
            if(person == null ) return GlobalResult.result_notFound("Person person_id not found");

            if (!person.activation_permission())  return GlobalResult.result_forbidden();

            if(!person.freeze_account) return GlobalResult.result_badRequest("Person is already active.");

            person.freeze_account = false;
            person.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "deactivate Person",
            tags = {"Admin-Person"},
            notes = "deactivate Person by id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Person.activation_permission", value = "true"),
                    })
            }


    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Secured_API.class)
    public Result person_deactivate(@ApiParam(value = "person_id String query", required = true) String person_id){
        try{

            Model_Person person = Model_Person.get_byId(person_id);
            if(person == null ) return GlobalResult.result_notFound("Person person_id not found");

            if (!person.activation_permission())  return GlobalResult.result_forbidden();

            if(person.freeze_account) return GlobalResult.result_badRequest("Person is already deactivated.");

            person.freeze_account = true;

            for(Model_FloatingPersonToken token : person.floatingPersonTokens) token.delete();

            person.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "valid Person email",
            tags = {"Admin-Person"},
            notes = "valid Person email by id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Person.activation_permission", value = "true"),
                    })
            }


    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Secured_API.class)
    public Result person_validEmail(@ApiParam(value = "person_id String query", required = true) String person_id){
        try{

            Model_Person person = Model_Person.get_byId(person_id);
            if(person == null ) return GlobalResult.result_notFound("Person person_id not found");

            if (!person.activation_permission())  return GlobalResult.result_forbidden();

            person.mailValidated = true;
            person.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Person",
            tags = {"Person"},
            notes = "Edit person basic information",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Person.edit_permission", value = "true"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Person_Update",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated",    response = Model_Person.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_API.class)
    public  Result person_update(@ApiParam(value = "person_id String query", required = true) String person_id){
        try{

            final Form<Swagger_Person_Update> form = Form.form(Swagger_Person_Update.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Person_Update help = form.get();

            Model_Person person = Model_Person.get_byId(person_id);
            if (person == null) return GlobalResult.result_notFound("Person not found");
            if (!person.edit_permission())  return GlobalResult.result_forbidden();

            person.nick_name    = help.nick_name;
            person.full_name    = help.full_name != null ? help.full_name : null;

            if (help.country != null && (!help.country.equals("")))
                person.country = help.country;

            if (help.gender != null && !help.gender.equals("") && (help.gender.equals("male") || help.gender.equals("female")))
                person.gender = help.gender;

            person.update();

            return GlobalResult.result_ok(Json.toJson(person));

         } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Person logged connections",
            tags = {"Person"},
            notes = "get all connections, where user is logged",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "FloatingPersonToken.read_permission", value = "Only user can get own connections - its not possible get that from another account!"),
                    })
            }

    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK result",               response = Model_FloatingPersonToken.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not Found object",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Secured_API.class)
    public  Result person_getAllConnections(){
        try{

           return GlobalResult.result_ok(Json.toJson( Controller_Security.get_person().floatingPersonTokens ));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "terminate logging",
            tags = {"Person"},
            notes = "You know where the user is logged in. And you can log out this connection. (Terminate token)",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "FloatingPersonToken.delete_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK result",               response = Result_Ok.class),
            @ApiResponse(code = 404, message = "Not Found object",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Secured_API.class)
    public  Result remove_Person_Connection(@ApiParam(value = "connection_id String query", required = true) String connection_id){
        try{

            Model_FloatingPersonToken token = Model_FloatingPersonToken.get_byId(connection_id);
            if(token == null ) return GlobalResult.result_notFound("FloatingPersonToken connection_id not found");

            if (!token.delete_permission())  return GlobalResult.result_forbidden();

            token.delete();

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "validation Entity",
            tags = {"Person"},
            notes = "for cyclical validation during registration, key contains 'mail' or 'nick_name'. Or can be used for 'vat_number' as a key.",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.documentationClass.Swagger_Entity_Validation_In",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Result if it is possible to use",       response = Swagger_Entity_Validation_Out.class),
            @ApiResponse(code = 400, message = "Something is wrong",                    response = Result_BadRequest.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public  Result something_validateProperty(){
        try{
            
            final Form<Swagger_Entity_Validation_In> form = Form.form(Swagger_Entity_Validation_In.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Entity_Validation_In help = form.get();

            Swagger_Entity_Validation_Out validation = new Swagger_Entity_Validation_Out();


            switch (help.key){

                case "mail":{
                    if(Model_Person.find.where().ieq("mail", help.value).findUnique() == null ){

                        validation.valid = true;
                        return GlobalResult.result_ok(Json.toJson(validation));
                    }

                    validation.valid = false;
                    validation.message = "email is used";

                    break;
                }

                case "nick_name" : {
                    if(Model_Person.find.where().ieq("nick_name", help.value).findUnique() == null ){

                        validation.valid = true;
                        return GlobalResult.result_ok(Json.toJson(validation));
                    }

                    validation.valid = false;
                    validation.message = "nick_name is used";

                    break;
                }

                case "vat_number":{

                    try {

                        terminal_logger.debug("person_validateProperty:: Link:: " + "https://www.isvat.eu/" + help.value.substring(0, 2) + "/" + help.value.substring(2));

                        WSClient ws = Play.current().injector().instanceOf(WSClient.class);

                        F.Promise<WSResponse> responsePromise = ws
                                .url("https://www.isvat.eu/" + help.value.substring(0, 2) + "/" + help.value.substring(2))
                                .setRequestTimeout(10000)
                                .get();

                        WSResponse wsResponse = responsePromise.get(10000);

                        JsonNode result = wsResponse.asJson();

                        terminal_logger.debug("person_validateProperty: http request: {} ", wsResponse.getStatus());
                        terminal_logger.debug("person_validateProperty: vat_number: {} " , result);

                        if (result.get("valid").asBoolean()) {

                            validation.valid = true;
                            try {
                                validation.message = result.get("name").get("0").asText();
                            }catch (Exception e){
                                // do nothing
                            }
                            return GlobalResult.result_ok(Json.toJson(validation));
                        }
                    }catch (Exception e){
                        terminal_logger.internalServerError("person_validateProperty:", e);
                        // Server_Logger.internalServerError("person_validateProperty()::", e);
                        validation.valid = false;
                        validation.message = "vat_number is not valid or could not be found";
                    }

                    break;
                }

                default:return GlobalResult.result_badRequest("Key does not exist, use only {mail, nick_name or vat_number}");
            }

            return GlobalResult.result_ok(Json.toJson(validation));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Person property",
            tags = {"Person"},
            notes = "Request password or email change. API does not change password or email, only sends email for authorization of the change and holds values in different object." +
                    "JSON value 'property' contains only 'password' or 'email'",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.documentationClass.Swagger_Person_ChangeProperty",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Secured_API.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result person_changeLoginProperty(){

        try {

            // Získání JSON
            final Form<Swagger_Person_ChangeProperty> form = Form.form(Swagger_Person_ChangeProperty.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Person_ChangeProperty help = form.get();

            if(Model_ChangePropertyToken.find.where().eq("person.id", Controller_Security.get_person().id).findUnique() != null)
                return GlobalResult.result_badRequest("You can request only one change at this time.");

            // Proměnné mailu
            String subject;
            String text;
            String link;



            switch (help.property){

                case "password":{

                    if (help.password == null) return GlobalResult.result_badRequest("You must fill in the password");

                    // Vytvoření tokenu pro podržení hesla
                    Model_ChangePropertyToken changePropertyToken = new Model_ChangePropertyToken();
                    changePropertyToken.person = Controller_Security.get_person();
                    changePropertyToken.property = help.property;
                    changePropertyToken.time_of_creation = new Date();
                    changePropertyToken.value = help.password;
                    changePropertyToken.save();

                    // Úprava proměnných mailu
                    subject = "Password change - need authorization";
                    text = "Password change was requested for your account. Click on the link below to authorize the change.";
                    link = Server.tyrion_serverAddress + "/person/authorize_change/" + changePropertyToken.change_property_token;

                    break;}

                case "email":{

                    if (help.email == null) return GlobalResult.result_badRequest("You must fill in the email");

                    // Vytvoření tokenu pro podržení emailu
                    Model_ChangePropertyToken changePropertyToken = new Model_ChangePropertyToken();
                    changePropertyToken.person = Controller_Security.get_person();
                    changePropertyToken.property = help.property;
                    changePropertyToken.time_of_creation = new Date();
                    changePropertyToken.value = help.email;
                    changePropertyToken.save();

                    // Úprava proměnných mailu
                    subject = "Email change - need authorization";
                    text = "Email change was requested for your account. Click on the link below to authorize the change. Verification email will be sent to your new email";
                    link = Server.tyrion_serverAddress + "/person/authorize_change/" + changePropertyToken.change_property_token;

                    break;}

                default: return GlobalResult.result_badRequest("No such property");
            }

            // Odeslání emailu
            try {

                new Email()
                        .text(text)
                        .divider()
                        .text("If you do not recognize any of this activity, we strongly recommend you to go to your account and change your password there, because it was probably stolen.")
                        .divider()
                        .link("Authorize change",link)
                        .send(Controller_Security.get_person().mail, subject);

            } catch (Exception e) {
                terminal_logger.internalServerError("person_changeLoginProperty:", e);
            }

            return GlobalResult.result_ok("Change was requested. You must authorize the change in next 4 hours via your email. Authorization email was sent.");

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "email Person approving password or email change",
            tags = {"Admin-Person"},
            notes = "",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.documentationClass.Swagger_Person_ChangeProperty",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result person_authorizePropertyChange(String token){
        try{
            Model_ChangePropertyToken changePropertyToken = Model_ChangePropertyToken.get_byId(token);
            if(changePropertyToken == null) return redirect(Server.becki_mainUrl + "/" + Server.becki_propertyChangeFailed);

            if(((new Date()).getTime() - changePropertyToken.time_of_creation.getTime()) > 14400000 ){
                changePropertyToken.delete();
                return redirect(Server.becki_mainUrl + "/" + Server.becki_propertyChangeFailed);
            }

            Model_Person person = Model_Person.get_byId(changePropertyToken.person.id);
            if(person == null) return redirect(Server.becki_mainUrl + "/" +  Server.becki_propertyChangeFailed);

            switch (changePropertyToken.property){

                case "password":{
                    // Úprava objektu
                    person.setSha(changePropertyToken.value);
                    person.update();
                    break;
                }

                case "email":{

                    // Úprava objektu
                    person.mail = changePropertyToken.value;
                    person.mailValidated = false;
                    person.update();

                    // Vytvoření validačního tokenu
                    Model_ValidationToken validationToken = Model_ValidationToken.find.where().eq("personEmail",person.mail).findUnique();
                    if(validationToken!=null) validationToken.delete();
                    validationToken = new Model_ValidationToken().setValidation(person.mail);

                    String link = Server.tyrion_serverAddress + "/person/mail_authentication/" + validationToken.authToken;

                    // Odeslání emailu
                    try {
                        new Email()
                                .text("Email verification is needed to complete your registration.")
                                .divider()
                                .link("Verify your email address",link)
                                .send(validationToken.personEmail, "Email Verification");

                    } catch (Exception e) {
                        terminal_logger.internalServerError("person_authorizePropertyChange:", e);
                    }
                    break;
                }
            }

            // Odhlášení uživatele všude
            for ( Model_FloatingPersonToken floatingPersonToken : person.floatingPersonTokens  ) {
                floatingPersonToken.delete();
            }

            changePropertyToken.delete();

            return redirect(Server.becki_mainUrl);

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "upload Person picture",
            tags = {"Person"},
            notes = "Uploads personal photo. Picture must be smaller than 800 KB and its dimensions must be between 50 and 400 pixels. If user already has a picture, it will be replaced by the new one. " +
                    "API requires base64 Content-Type, name of the property is 'file'.",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.documentationClass.Swagger_BASE64_FILE",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Secured_API.class)
    @BodyParser.Of(value = BodyParser.Json.class, maxLength = 1024 * 1024)
    public Result person_uploadPicture(){
        try {

            // Získání JSON
            final Form<Swagger_BASE64_FILE> form = Form.form(Swagger_BASE64_FILE.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_BASE64_FILE help = form.get();

            Model_Person person = Controller_Security.get_person();
            if(person ==  null) {
                return GlobalResult.result_badRequest("User not found"); // Just for compiler Error
            }

            // Kontzrola oprávnění
            if(!person.edit_permission()) return GlobalResult.result_forbidden();

          // Odeberu cache - jen projistotu
            person.cache_picture_link = null;

            // Pokud tu byl nějaký soubor - smažu ho - prázdný soubor je příkaz ke smazání
            if(help.file == null || help.file.equals("")){
                Model_FileRecord fileRecord = person.picture;
                person.picture = null;
                person.alternative_picture_link = "";
                person.update();
                fileRecord.refresh();
                fileRecord.delete();

                return GlobalResult.result_ok();
            }

            //  data:image/png;base64,
            //  data:image/png;base64,
            String[] parts = help.file.split(",");
            String[] type = parts[0].split(":");
            String[] content_type = type[1].split(";");
            String dataType = content_type[0].split("/")[1];

            terminal_logger.debug("bootLoader_uploadFile:: Cont Type:" + content_type[0] + ":::");
            terminal_logger.debug("bootLoader_uploadFile:: Data Type:" + dataType + ":::");
            terminal_logger.debug("bootLoader_uploadFile:: Data: " + parts[1].substring(0, 10) + "......");

            // Odebrání předchozího obrázku
            if(person.picture != null){
                terminal_logger.debug("person_uploadPicture:: Removing previous picture");
                Model_FileRecord fileRecord = person.picture;
                person.picture = null;
                person.alternative_picture_link = "";
                person.update();
                fileRecord.delete();
            }

            // Pokud link není, vygeneruje se nový, unikátní
            if(person.alternative_picture_link == null || person.alternative_picture_link.equals("")){
                person.alternative_picture_link = person.get_Container().getName() + "/" + UUID.randomUUID().toString() + ".png";
                person.update();
            }

            // Pouze pro případy, kdy se uživatel registroval skrze sociální síť a Tyrion používá obrázek daného uživatele
            // Z konrkétní sociální sítě - pak chybí soubor, ale existuje cesta k souboru, kterou zaslí tyrion do Becki
            // Například:: https://avatars1.githubusercontent.com/u/16296782?v=3
            // PRoto je nutné na to pamatovat - jinak se pak taková cesta strká do Azure k přepsání předchozího obrázku
            if(person.alternative_picture_link.contains("http")){
                person.alternative_picture_link = null;
                person.update();
            }


            String file_name =  UUID.randomUUID().toString() + ".jpg";
            String file_path =  person.get_Container().getName() + "/" +file_name;

            terminal_logger.debug("person_uploadPicture::  File Name " + file_name );
            terminal_logger.debug("person_uploadPicture::  File Path " + file_path );

            person.picture = Model_FileRecord.uploadAzure_File( parts[1], content_type[0], file_name, file_path);
            person.update();


            return GlobalResult.result_ok("Picture successfully uploaded");
        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Person picture",
            tags = {"Person"},
            notes = "Removes picture of logged person",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Secured_API.class)
    public Result person_removePicture(){
        try {

            Model_Person person = Controller_Security.get_person();

            if(!(person.picture == null)) {
                Model_FileRecord fileRecord = person.picture;
                person.picture = null;
                person.alternative_picture_link = null;
                person.update();
                fileRecord.delete();
            }else{
                return GlobalResult.result_badRequest("There is no picture to remove.");
            }

            return GlobalResult.result_ok("Picture successfully removed");
        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }


//######################################################################################################################

}
