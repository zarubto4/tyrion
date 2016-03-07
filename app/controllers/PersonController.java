package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.*;
import models.persons.Person;
import models.persons.ValidationToken;
import play.Configuration;
import play.Logger;
import play.api.libs.mailer.MailerClient;
import play.libs.Json;
import play.libs.mailer.Email;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.Server;
import utilities.emails.EmailTool;
import utilities.loginEntities.Secured;
import utilities.response.GlobalResult;
import utilities.response.response_objects.JsonValueMissing;
import utilities.response.response_objects.Result_PermissionRequired;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.swagger.documentationClass.Swagger_Person_New;

import javax.inject.Inject;
import javax.websocket.server.PathParam;
import java.text.SimpleDateFormat;
import java.util.Locale;

@Api(value = "Not Documented API - InProgress or Stuck")
public class PersonController extends Controller {
    @Inject MailerClient mailerClient;


    @ApiOperation(value = "register new Person",
            tags = {"Person"},
            notes = "create new Person with unique email and nick_name",
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
            @ApiResponse(code = 201, message = "Successful created",      response = Person.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result registred_Person() {
        try {
            Swagger_Person_New help = Json.fromJson( request().body().asJson() ,Swagger_Person_New.class);

            if (Person.find.where().eq("nick_name", help.nick_name).findUnique() != null)
                return GlobalResult.badRequest("nick name is used");
            if (Person.find.where().eq("mail", help.mail).findUnique() != null)
                return GlobalResult.badRequest("Email is registered");


            Person person = new Person();

            person.nick_name =  help.nick_name;
            person.mail = help.mail;
            person.mailValidated = false;

            person.setSha(help.password);
            person.save();

            ValidationToken validationToken = new ValidationToken().setValidation(person.mail);

            String link = Server.serverAddress + "/mail_person_authentication" + "?mail=" + person.mail + "&token=" + validationToken.authToken;

            try {
                Email email = new EmailTool().sendEmailValidation(help.nick_name , person.mail, link);
                mailerClient.send(email);

            } catch (Exception e) {
                // TODO vhodně zalogovat tento problém
                System.out.println("Odesílání emailu se nezdařilo");
                e.printStackTrace();
            }

            return GlobalResult.okResult(Json.toJson(person));

        }catch (NullPointerException e){
            return GlobalResult.nullPointerResult(e, "nick_name","mail", "password" );

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("PersonController - edit_Person_Information ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "Email verification of registration", hidden = true)
    public Result email_Person_authentitaction(String mail, String authToken) {
        try{

            Person person = Person.find.where().eq("mail", mail).findUnique();
            ValidationToken validationToken = ValidationToken.find.where().eq("authToken", authToken).findUnique();

            if(person == null || validationToken == null || !validationToken.personEmail.equals(mail)) return GlobalResult.redirect( Configuration.root().getString("Becki.accountAuthorizedFailed") );

            person.mailValidated = true;
            person.update();

           validationToken.delete();

            return GlobalResult.redirect( Configuration.root().getString("Becki.accountAuthorizedSuccessful") );
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("PersonController - email_Person_authentitaction ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @Security.Authenticated(Secured.class)
    public  Result getPerson(@ApiParam(value = "person_id String query", required = true) @PathParam("person_id")  String person_id){
        try{

            Person person =Person.find.byId(person_id);
            if(person == null )  return GlobalResult.notFoundObject();
            return GlobalResult.okResult(Json.toJson(person));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("PersonController - getPerson ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public  Result deletePerson(@ApiParam(value = "person_id String query", required = true) @PathParam("person_id") String person_id){
        try{

            Person person = Person.find.byId(person_id);
            if(person == null ) return GlobalResult.notFoundObject();
            person.delete();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("PersonController - deletePerson ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public  Result edit_Person_Information(){
        try{
            JsonNode json = request().body().asJson();

            Person person = Person.find.byId(json.get("mail").asText());
            if (person == null) return GlobalResult.notFoundObject();

            person.nick_name    = json.get("nick_name")     .asText();
            person.first_name   = json.get("first_name")     .asText();
            person.middle_name  = json.get("middle_name")    .asText();
            person.last_name    = json.get("last_name")      .asText();
            person.date_of_birth = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH).parse(json.get("date_of_birth").asText());
            person.first_title  = json.get("first_title")    .asText();
            person.last_title   = json.get("last_title")     .asText();
            person.update();

            return GlobalResult.okResult();

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "nick_name - String",  "first_name - String",  "middle_name - String",  "last_name - String",  "date_of_birth - String",  "first_title - String", "last_title - String");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("PersonController - edit_Person_Information ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }




    public  Result valid_Person_mail(@ApiParam(value = "mail value for server side unique control", required = true) @PathParam("person_id") String mail){
        try{

            if(Person.find.where().ieq("mail", mail).findUnique() == null ) return GlobalResult.okResult();
            else return GlobalResult.badRequest("Its used");

        }catch (Exception e){
            return GlobalResult.internalServerError();
        }
    }

    public  Result valid_Person_NickName(@ApiParam(value = "nick_name value for server side unique control", required = true) @PathParam("person_id")  String nick_name){
        try{

            if(Person.find.where().ieq("nick_name", nick_name).findUnique() == null ) return GlobalResult.okResult();
            else return GlobalResult.badRequest("Its used");

        }catch (Exception e){
            return GlobalResult.internalServerError();
        }
    }

}
