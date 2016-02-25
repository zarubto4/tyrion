package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;
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
import utilities.emails.EmailTool;
import utilities.loginEntities.Secured;
import utilities.response.GlobalResult;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Locale;

@Api(value = "PersonController - nezdokumentované",
     description = "Compilation operation (Role, Permission and permissions operations",
     authorizations = { @Authorization(value="logged_in", scopes = {} )}
)
public class PersonController extends Controller {

    @Inject MailerClient mailerClient;


    @BodyParser.Of(BodyParser.Json.class)
    public Result developerRegistration() {
        try{
            JsonNode json = request().body().asJson();

            if ( Person.find.where().eq("nick_name", json.get("nick_name").asText()).findUnique() != null) return GlobalResult.badRequest("nick name is used");
            if ( Person.find.where().eq("mail", json.get("mail").asText()).findUnique()  != null) return GlobalResult.badRequest("Email is registered");

            Person person = new Person();
            person.mail = json.get("mail").asText();
            person.mailValidated = true;
            person.nick_name = json.get("nick_name").asText();

            person.setSha(json.get("password").asText());
            person.save();

            return GlobalResult.okResult();
        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "mail",  "password");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("PersonController - edit_Person_Information ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result registred_Person() {
        try {
            JsonNode json = request().body().asJson();

            if (Person.find.where().eq("nick_name", json.get("nick_name").asText()).findUnique()  != null) return GlobalResult.badRequest("nick name is used");
            if (Person.find.where().eq("mail", json.get("mail").asText()).findUnique() != null)  return GlobalResult.badRequest("Email is registered");


            Person person = new Person();

            person.nick_name = json.get("nick_name").asText();
            person.mail = json.get("mail").asText();
            person.mailValidated = false;

            person.setSha(json.get("password").asText());
            person.save();

            ValidationToken validationToken = new ValidationToken().setValidation(person.mail);

            String link = Configuration.root().getString("serverLink.Production") + "/mailPersonAuthentication/" + "?mail=" + person.mail + "&authToken=" + validationToken.authToken;

            try {
                Email email = new EmailTool().sendEmailValidation(person.first_name + person.last_name, person.mail, link);
                mailerClient.send(email);

            }catch (Exception e){
                // TODO vhodně zalogovat tento problém
                System.out.println("Odesílání emailu se nezdařilo");
                e.printStackTrace();
            }

            return GlobalResult.okResult(Json.toJson(person));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("PersonController - edit_Person_Information ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }


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
    public  Result getPerson(String id){
        try{

            Person person =Person.find.byId(id);
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
    public  Result deletePerson(String id){
        try{

            Person person = Person.find.byId(id);
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


}
