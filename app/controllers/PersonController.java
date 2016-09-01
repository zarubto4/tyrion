package controllers;

import io.swagger.annotations.*;
import models.person.*;
import play.api.libs.mailer.MailerClient;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.Server;
import utilities.emails.EmailTool;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Swagger_Entity_Validation_Out;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck") // Překrývá nezdokumentované API do jednotné serverové kategorie ve Swaggeru.
public class PersonController extends Controller {

    @Inject MailerClient mailerClient;
    @Inject ProgramingPackageController programingPackageController;
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

//######################################################################################################################

    @ApiOperation(value = "register new Person",
            tags = {"Person"},
            notes = "create new Person with unique email and nick_name, for standard registration leave invitationToken empty, it's used only if someone is invited via email",
            produces = "application/json",
            protocols = "https",
            code = 201
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
            @ApiResponse(code = 201, message = "Successful created",      response = Result_ok.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result registred_Person() {
        try {

           final Form<Swagger_Person_New> form = Form.form(Swagger_Person_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Person_New help = form.get();

            if (Person.find.where().eq("nick_name", help.nick_name).findUnique() != null)
                return GlobalResult.result_BadRequest("nick name is used");
            if (Person.find.where().eq("mail", help.mail).findUnique() != null)
                return GlobalResult.result_BadRequest("Email is registered");


            Person person = new Person();

            person.nick_name =  help.nick_name;
            person.mail = help.mail;
            person.mailValidated = false;

            person.setSha(help.password);
            person.save();

            Invitation invitation = Invitation.find.where().eq("mail", person.mail).findUnique();

            if(invitation == null) {

                ValidationToken validationToken = ValidationToken.find.where().eq("personEmail",help.mail).findUnique();
                if(validationToken!=null) validationToken.delete();
                validationToken = new ValidationToken().setValidation(person.mail);

                String link = Server.tyrion_serverAddress + "/mail_person_authentication" + "/" + person.mail + "/" + validationToken.authToken;

                try {
                            new EmailTool()
                            .addEmptyLineSpace()
                            .startParagraph("13")
                            .addText("Email verification is needed to complete your registration.")
                            .endParagraph()
                            .addEmptyLineSpace()
                            .addLine()
                            .addEmptyLineSpace()
                            .addLink(link,"Click here to verify","18")
                            .addEmptyLineSpace()
                            .sendEmail(help.mail, "Email Verification");

                } catch (Exception e) {
                    logger.error("Sending mail -> critical error", e);
                    e.printStackTrace();
                }
            }else{
                person.mailValidated = true;
                person.update();

                try {
                    programingPackageController.addParticipantToProject(invitation.id, true);
                }catch(Exception e){
                    return Loggy.result_internalServerError(e, request());
                }
            }

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Email verification of registration", hidden = true)
    public Result email_Person_authentitaction(String mail, String authToken) {
        try{

            Person person = Person.find.where().eq("mail", mail).findUnique();
            ValidationToken validationToken = ValidationToken.find.where().eq("authToken", authToken).findUnique();

            if(person == null || validationToken == null || !validationToken.personEmail.equals(mail)) return GlobalResult.redirect( Server.becki_accountAuthorizedFailed  );

            person.mailValidated = true;
            person.update();

           validationToken.delete();

            return GlobalResult.redirect( Server.becki_accountAuthorizedSuccessful );
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }



    @ApiOperation(value = "send password recovery email",
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
            @ApiResponse(code = 200, message = "OK",                      response = Result_ok.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result sendPasswordRecoveryEmail(){
        try{

            final Form<Swagger_Person_Password_RecoveryEmail> form = Form.form(Swagger_Person_Password_RecoveryEmail.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Person_Password_RecoveryEmail help = form.get();

            String link;

            Person person = Person.find.where().eq("mail", help.mail).findUnique();
            if(person == null) return GlobalResult.result_ok();

            PasswordRecoveryToken previousToken = PasswordRecoveryToken.find.where().eq("person_id",person.id).findUnique();

            if(!(previousToken == null)) if(((new java.util.Date()).getTime() - previousToken.time_of_creation.getTime()) > 900000)
            {
                previousToken.delete();
                previousToken = null;
            }

            if(previousToken == null){

                PasswordRecoveryToken passwordRecoveryToken = new PasswordRecoveryToken();
                passwordRecoveryToken.setPasswordRecoveryToken();
                passwordRecoveryToken.person = person;
                passwordRecoveryToken.time_of_creation = new Date();
                passwordRecoveryToken.save();

                link = Server.becki_passwordReset + "/" + passwordRecoveryToken.password_recovery_token;
            }else {
                link = Server.becki_passwordReset + "/" + previousToken.password_recovery_token;
            }
            try {
                        new EmailTool()
                        .addEmptyLineSpace()
                        .startParagraph("13")
                        .addText("Password reset was requested for this email.")
                        .endParagraph()
                        .addEmptyLineSpace()
                        .addLine()
                        .addEmptyLineSpace()
                        .addLink(link,"Click here to reset your password","18")
                        .addEmptyLineSpace()
                        .sendEmail(help.mail,"Password Reset" );



            } catch (Exception e) {
                logger.error ("Sending mail -> critical error", e);
                e.printStackTrace();
            }
            return GlobalResult.result_ok();
        }catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "change person password",
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
            @ApiResponse(code = 200, message = "OK",                      response = Result_ok.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result personPasswordRecovery() {
        try{

            final Form<Swagger_Person_Password_New> form = Form.form(Swagger_Person_Password_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Person_Password_New help = form.get();

            Person person = Person.find.where().eq("mail", help.mail).findUnique();

            PasswordRecoveryToken passwordRecoveryToken = PasswordRecoveryToken.find.where().eq("password_recovery_token", help.password_recovery_token).findUnique();
            if(passwordRecoveryToken == null) return GlobalResult.result_BadRequest("Password change was unsuccessful");

            if(person == null || !passwordRecoveryToken.person.id.equals(person.id)) {
                passwordRecoveryToken.delete();
                return GlobalResult.result_BadRequest("Password change was unsuccessful");
            }

            if(((new java.util.Date()).getTime() - passwordRecoveryToken.time_of_creation.getTime()) > 86400000 ){
                passwordRecoveryToken.delete();
                return GlobalResult.result_BadRequest("You must recover your password in 24 hours.");
            }

            for ( FloatingPersonToken floatingPersonToken : person.floatingPersonTokens  ) {
                floatingPersonToken.delete();
            }

            person.shaPassword = null;
            person.update();

            person.refresh();
            person.setSha(help.password);
            person.update();

            passwordRecoveryToken.delete();

            try {
                        new EmailTool()
                        .addEmptyLineSpace()
                        .startParagraph("13")
                        .addText("Password was changed for your account.")
                        .endParagraph()
                        .addEmptyLineSpace()
                        .sendEmail(help.mail, "Password Reset");

            } catch (Exception e) {
                logger.error ("Sending mail -> critical error", e);
                e.printStackTrace();
            }

            return GlobalResult.result_ok("Password was changed successfully");
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Person.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public  Result get_Person(@ApiParam(value = "person_id String query", required = true)  String person_id){
        try{

            Person person = Person.find.byId(person_id);
            if(person == null )  return GlobalResult.notFoundObject("Person person_id not found");
            return GlobalResult.result_ok(Json.toJson(person));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all Person",
            tags = {"Person"},
            notes = "get all Persons",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Person.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public  Result get_Person_all(){
        try{

            List<Person> persons = Person.find.all();
            return GlobalResult.result_ok(Json.toJson(persons));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Person",
            hidden = true,
            tags = {"Person"},
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public  Result deletePerson(@ApiParam(value = "person_id String query", required = true) String person_id){
        try{

            Person person = Person.find.byId(person_id);
            if(person == null ) return GlobalResult.notFoundObject("Person person_id not found");


            if (!person.delete_permission())  return GlobalResult.forbidden_Permission();
            person.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove all connection tokens",
            hidden = true,
            tags = {"Person"},
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result delete_all_tokens(@ApiParam(value = "person_id String query", required = true) String person_id){
        try{

            Person person = Person.find.byId(person_id);
            if(person == null ) return GlobalResult.notFoundObject("Person person_id not found");

            if (!person.edit_permission())  return GlobalResult.forbidden_Permission();

            for(FloatingPersonToken token : person.floatingPersonTokens) token.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "activate Person",
            hidden = true,
            tags = {"Person"},
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result activatePerson(@ApiParam(value = "person_id String query", required = true) String person_id){
        try{

            Person person = Person.find.byId(person_id);
            if(person == null ) return GlobalResult.notFoundObject("Person person_id not found");

            if (!person.activation_permission())  return GlobalResult.forbidden_Permission();

            person.freeze_account = false;
            person.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "deactivate Person",
            hidden = true,
            tags = {"Person"},
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result deactivatePerson(@ApiParam(value = "person_id String query", required = true) String person_id){
        try{

            Person person = Person.find.byId(person_id);
            if(person == null ) return GlobalResult.notFoundObject("Person person_id not found");

            if (!person.activation_permission())  return GlobalResult.forbidden_Permission();

            person.freeze_account = true;

            for(FloatingPersonToken token : person.floatingPersonTokens) token.delete();

            person.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "valid Person email - access to Becki",
            hidden = true,
            tags = {"Person"},
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result valid_email_Person(@ApiParam(value = "person_id String query", required = true) String person_id){
        try{

            Person person = Person.find.byId(person_id);
            if(person == null ) return GlobalResult.notFoundObject("Person person_id not found");

            if (!person.activation_permission())  return GlobalResult.forbidden_Permission();

            person.mailValidated = true;
            person.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


    @ApiOperation(value = "edit Person basic information",
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
            @ApiResponse(code = 200, message = "Successful updated",      response = Person.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_API.class)
    public  Result edit_Person_Information(String person_id){
        try{

            final Form<Swagger_Person_Update> form = Form.form(Swagger_Person_Update.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Person_Update help = form.get();

            Person person = Person.find.byId(person_id);
            if (person == null) return GlobalResult.notFoundObject("Person person_id not found");
            if (!person.edit_permission())  return GlobalResult.forbidden_Permission();

            person.nick_name    = help.nick_name;
            person.full_name    = help.full_name;

            person.update();

            return GlobalResult.result_ok(Json.toJson(person));

         } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get logged connections",
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
            @ApiResponse(code = 200, message = "Its possible used that",  response = FloatingPersonToken.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Not Found object",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public  Result get_Person_Connections(){
        try{

           return GlobalResult.result_ok(Json.toJson( SecurityController.getPerson().floatingPersonTokens ));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
            @ApiResponse(code = 200, message = "Its possible used that",  response = Result_ok.class),
            @ApiResponse(code = 400, message = "Not Found object",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public  Result remove_Person_Connection(String connection_id){
        try{

            FloatingPersonToken token = FloatingPersonToken.find.byId(connection_id);
            if(token == null ) return GlobalResult.notFoundObject("FloatingPersonToken connection_id not found");

            if (!token.delete_permission())  return GlobalResult.forbidden_Permission();

            token.delete();

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "validate some Entity",
            tags = {"Person"},
            notes = "for cyclical validation during registration, key contains 'mail' or 'nick_name'",
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
            @ApiResponse(code = 200, message = "Result if its possible to used that",   response = Swagger_Entity_Validation_Out.class),
            @ApiResponse(code = 400, message = "Something is wrong",                    response = Result_BadRequest.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result validate_Entity(){
        try{

            final Form<Swagger_Entity_Validation_In> form = Form.form(Swagger_Entity_Validation_In.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Entity_Validation_In help = form.get();

            Swagger_Entity_Validation_Out validation = new Swagger_Entity_Validation_Out();

            switch (help.key){
                case "mail":{
                    if(Person.find.where().ieq("mail", help.value).findUnique() == null ){

                        validation.valid = true;
                        return GlobalResult.result_ok(Json.toJson(validation));
                    }

                    validation.valid = false;
                    validation.message = "email is used";

                    break;
                }

                case "nick_name":{
                    if(Person.find.where().ieq("nick_name", help.value).findUnique() == null ){

                        validation.valid = true;
                        return GlobalResult.result_ok(Json.toJson(validation));
                    }

                    validation.valid = false;
                    validation.message = "nick_name is used";

                    break;
                }

                default:return GlobalResult.badRequest("Key does not exist");
            }

            return GlobalResult.result_ok(Json.toJson(validation));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "change person login info",
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
            @ApiResponse(code = 200, message = "OK Result",               response = Result_ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result changePersonLoginProperty(){

        // Získání JSON
        final Form<Swagger_Person_ChangeProperty> form = Form.form(Swagger_Person_ChangeProperty.class).bindFromRequest();
        if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
        Swagger_Person_ChangeProperty help = form.get();

        // Proměnné mailu
        String subject;
        String text;
        String link;

        try {

            switch (help.property){

                case "password":{

                    if (help.password == null) return GlobalResult.badRequest("You must fill in the password");

                    // Vytvoření tokenu pro podržení hesla
                    ChangePropertyToken changePropertyToken = new ChangePropertyToken();
                    changePropertyToken.person = SecurityController.getPerson();
                    changePropertyToken.property = help.property;
                    changePropertyToken.time_of_creation = new Date();
                    changePropertyToken.value = help.password;
                    changePropertyToken.setChangePropertyToken();
                    changePropertyToken.save();

                    // Úprava proměnných mailu
                    subject = "Password change - need authorization";
                    text = "Password change was requested for your account. Click on the link below to authorize the change.";
                    link = Server.tyrion_serverAddress + "/coreClient/authorize_change/" + changePropertyToken.change_property_token;

                    break;}

                case "email":{

                    if (help.email == null) return GlobalResult.badRequest("You must fill in the email");

                    // Vytvoření tokenu pro podržení emailu
                    ChangePropertyToken changePropertyToken = new ChangePropertyToken();
                    changePropertyToken.person = SecurityController.getPerson();
                    changePropertyToken.property = help.property;
                    changePropertyToken.time_of_creation = new Date();
                    changePropertyToken.value = help.email;
                    changePropertyToken.setChangePropertyToken();
                    changePropertyToken.save();

                    // Úprava proměnných mailu
                    subject = "Email change - need authorization";
                    text = "Email change was requested for your account. Click on the link below to authorize the change. Verification email will be sent to your new email";
                    link = Server.tyrion_serverAddress + "/coreClient/authorize_change/" + changePropertyToken.change_property_token;

                    break;}

                default: return GlobalResult.badRequest("No such property");
            }

            // Odeslání emailu
            try {

                new EmailTool()
                        .addEmptyLineSpace()
                        .startParagraph("13")
                        .addText(text)
                        .endParagraph()
                        .startParagraph("13")
                        .addText("If you do not recognize any of this activity, we strongly recommend you to go to your account and change your password there, because it was probably stolen")
                        .endParagraph()
                        .addEmptyLineSpace()
                        .addLine()
                        .addEmptyLineSpace()
                        .addLink(link, "Authorize", "18")
                        .addEmptyLineSpace()
                        .sendEmail(SecurityController.getPerson().mail, subject);

            } catch (Exception e) {
                logger.error ("Sending mail -> critical error", e);
                e.printStackTrace();
            }

            return GlobalResult.result_ok("Change was requested. You must authorize the change in next 4 hours via your email. Authorization email was sent.");

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Authorization of password or email change", hidden = true)
    public Result authorizePropertyChange(String token){
        try{
            ChangePropertyToken changePropertyToken = ChangePropertyToken.find.where().eq("change_property_token",token).findUnique();
            if(changePropertyToken == null) return redirect(Server.becki_propertyChangeFailed);

            if(((new java.util.Date()).getTime() - changePropertyToken.time_of_creation.getTime()) > 14400000 ){
                changePropertyToken.delete();
                return redirect(Server.becki_propertyChangeFailed);
            }

            Person person = Person.find.byId(changePropertyToken.person.id);
            if(person == null) return redirect(Server.becki_propertyChangeFailed);

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
                    ValidationToken validationToken = ValidationToken.find.where().eq("personEmail",person.mail).findUnique();
                    if(validationToken!=null) validationToken.delete();
                    validationToken = new ValidationToken().setValidation(person.mail);

                    String link = Server.tyrion_serverAddress + "/mail_person_authentication" + "/" + person.mail + "/" + validationToken.authToken;

                    // Odeslání emailu
                    try {
                        new EmailTool()
                                .addEmptyLineSpace()
                                .startParagraph("13")
                                .addText("Email verification is needed to complete your email change.")
                                .endParagraph()
                                .addEmptyLineSpace()
                                .addLine()
                                .addEmptyLineSpace()
                                .addLink(link,"Click here to verify","18")
                                .addEmptyLineSpace()
                                .sendEmail(person.mail, "Email Verification");

                    } catch (Exception e) {
                        logger.error("Sending mail -> critical error", e);
                        e.printStackTrace();
                    }
                    break;
                }
            }

            // Odhlášení uživatele všude
            for ( FloatingPersonToken floatingPersonToken : person.floatingPersonTokens  ) {
                floatingPersonToken.delete();
            }

            changePropertyToken.delete();

            return redirect(Server.becki_mainUrl);

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }
}
