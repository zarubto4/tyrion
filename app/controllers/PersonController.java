package controllers;

import io.swagger.annotations.*;
import models.person.FloatingPersonToken;
import models.person.Person;
import models.person.ValidationToken;
import play.api.libs.mailer.MailerClient;
import play.data.Form;
import play.libs.Json;
import play.libs.mailer.Email;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.Server;
import utilities.emails.EmailTool;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.Swagger_Person_New;
import utilities.swagger.documentationClass.Swagger_Person_Update;

import javax.inject.Inject;
import javax.websocket.server.PathParam;
import java.text.SimpleDateFormat;
import java.util.Locale;

@Api(value = "Not Documented API - InProgress or Stuck") // Překrývá nezdokumentované API do jednotné serverové kategorie ve Swaggeru.
public class PersonController extends Controller {

    @Inject MailerClient mailerClient;
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

//######################################################################################################################

    @ApiOperation(value = "register new Person",
            tags = {"Person"},
            notes = "create new Person with unique email and nick_name",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "my-extension", properties = {
                            @ExtensionProperty(name = "test1", value = "value1"),
                            @ExtensionProperty(name = "test2", value = "value2")
                    })
            }
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
            @ApiResponse(code = 201, message = "Successful created",      response = Person.class),
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

            ValidationToken validationToken = new ValidationToken().setValidation(person.mail);

            String link = Server.tyrion_serverAddress + "/mail_person_authentication" + "?mail=" + person.mail + "&token=" + validationToken.authToken;

            try {
                Email email = new EmailTool().sendEmailValidation(help.nick_name , person.mail, link);
                mailerClient.send(email);

            } catch (Exception e) {
                logger.error ("Sending mail -> critical error", e);
                e.printStackTrace();
            }

            return GlobalResult.created(Json.toJson(person));
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

    @ApiOperation(value = "get Person",
            tags = {"Person"},
            notes = "get Person by id",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
      @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Person.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public  Result getPerson(@ApiParam(value = "person_id String query", required = true) @PathParam("person_id")  String person_id){
        try{

            Person person = Person.find.byId(person_id);
            if(person == null )  return GlobalResult.notFoundObject("Person person_id not found");
            return GlobalResult.result_ok(Json.toJson(person));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Person",
            tags = {"Person"},
            notes = "delete Person by id",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public  Result deletePerson(@ApiParam(value = "person_id String query", required = true) @PathParam("person_id") String person_id){
        try{

            Person person = Person.find.byId(person_id);
            if(person == null ) return GlobalResult.notFoundObject("Person person_id not found");
            person.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "register new Person",
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
                            dataType = "utilities.swagger.documentationClass.Swagger_Person_Update",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful updated",      response = Person.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured.class)
    public  Result edit_Person_Information(String person_id){
        try{

            final Form<Swagger_Person_Update> form = Form.form(Swagger_Person_Update.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Person_Update help = form.get();

            Person person = Person.find.byId(person_id);
            if (person == null) return GlobalResult.notFoundObject("Person person_id not found");

            person.nick_name    = help.nick_name;
            person.full_name   = help.full_name;
            person.date_of_birth = help.date_of_birth;

            person.last_title   = help.last_title;

            person.update();

            return GlobalResult.result_ok();

         } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get logged connection",
            tags = {"Person"},
            notes = "get all connections, where user is logged",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Its possible used that",  response = FloatingPersonToken.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Not Found object",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
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
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Its possible used that",  response = Result_ok.class),
            @ApiResponse(code = 400, message = "Not Found object", response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result remove_Person_Connection(String connection_id){
        try{

            FloatingPersonToken token = FloatingPersonToken.find.byId(connection_id);
            if(token == null ) return GlobalResult.notFoundObject("Person person_id not found");
            token.delete();

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "valid email during registration",
            tags = {"Person"},
            notes = "for cyclical validation during registration",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Its possible used that",  response = Result_ok.class),
            @ApiResponse(code = 400, message = "Nick name is used", response = Result_BadRequest.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result valid_Person_mail(@ApiParam(value = "mail value for server side unique control", required = true) @PathParam("person_id") String mail){
        try{

            if(Person.find.where().ieq("mail", mail).findUnique() == null ) return GlobalResult.result_ok();
            else return GlobalResult.result_BadRequest("Its used");

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "valid nick_name during registration",
            tags = {"Person"},
            notes = "for cyclical validation during registration",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Its possible used that",  response = Result_ok.class),
            @ApiResponse(code = 400, message = "Nick name is used", response = Result_BadRequest.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result valid_Person_NickName(@ApiParam(value = "nick_name value for server side - it must be unique", required = true) @PathParam("nick_name")  String nick_name){
        try{

            if(Person.find.where().ieq("nick_name", nick_name).findUnique() == null ) return GlobalResult.result_ok();
            else return GlobalResult.result_BadRequest("Its used");

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

}
