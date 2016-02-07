package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.login.Person;
import models.login.ValidationToken;
import play.Configuration;
import play.Logger;
import play.api.libs.mailer.MailerClient;
import play.libs.Json;
import play.libs.mailer.Email;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.loginEntities.Secured;
import utilities.emails.EmailTool;
import utilities.response.GlobalResult;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class PersonCreateController extends Controller {

    @Inject MailerClient mailerClient;

    @BodyParser.Of(BodyParser.Json.class)
    public Result developerRegistration() {
        try{
            JsonNode json = request().body().asJson();

            Person person = new Person();
            person.mail = json.get("mail").asText();
            person.password = json.get("password").asText();
            person.emailValidated = true;

            if (Person.find.byId(json.get("mail").asText()) != null) return GlobalResult.badRequest("Email Exist");

            person.setSha(json.get("password").asText());
            person.save();

            return GlobalResult.okResult();
        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e, "mail - String",  "password - String");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("PersonCreateController - updatePersonInformation ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result standartRegistration() {
        try {
            JsonNode json = request().body().asJson();

            Person person = new Person();

            person.nickName = json.get("nickName").asText();
            person.mail = json.get("mail").asText();
            person.password = json.get("password").asText();
            person.emailValidated = false;

            if (Person.find.where().eq("mail",json.get("mail").asText()).findUnique() != null) return GlobalResult.badRequest("Email is used");
            if (Person.find.where().eq("nickName",json.get("nickName").asText()).findUnique() != null) return GlobalResult.badRequest("Nickname is used");


            person.setSha(json.get("password").asText());
            person.save();

            ValidationToken validationToken = new ValidationToken().setValidation(person.mail);

            String link = Configuration.root().getString("serverLink.Production") + "/emailPersonAuthentication/" + "?mail=" + person.mail + "&authToken=" + validationToken.authToken;

            try {
                Email email = new EmailTool().sendEmailValidation(person.firstName + person.lastName, person.mail, link);
                mailerClient.send(email);

            }catch (Exception e){
                System.out.println("Odesílání emailu se nezdařilo");
                e.printStackTrace();
            }

            return GlobalResult.okResult(Json.toJson(person));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("PersonCreateController - updatePersonInformation ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }


    public Result emailPersonAuthentitaction(String mail, String authToken) {
        try{

            Person person = Person.find.where().eq("mail", mail).findUnique();
            ValidationToken validationToken = ValidationToken.find.where().eq("authToken", authToken).findUnique();

            if(person == null || validationToken == null || !validationToken.personEmail.equals(mail)) return GlobalResult.redirect( Configuration.root().getString("Becki.accountAuthorizedFailed") );

            person.emailValidated = true;
            person.update();

           validationToken.delete();

            return GlobalResult.redirect( Configuration.root().getString("Becki.accountAuthorizedSuccessful") );
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("PersonCreateController - emailPersonAuthentitaction ERROR");
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
            Logger.error("PersonCreateController - getPerson ERROR");
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
            Logger.error("PersonCreateController - deletePerson ERROR");
            return GlobalResult.internalServerError();
        }
    }



    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public  Result updatePersonInformation(){
        try{
            JsonNode json = request().body().asJson();

            Person person = Person.find.byId(json.get("mail").asText());
            if (person == null) return GlobalResult.notFoundObject();

            person.nickName     =  json.get("nickName")     .asText();
            person.firstName    = json.get("firstName")     .asText();
            person.middleName   = json.get("middleName")    .asText();
            person.lastName = json.get("lastName")      .asText();
            person.dateOfBirth  = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH).parse(json.get("dateOfBirth").asText());
            person.firstTitle   = json.get("firstTitle")    .asText();
            person.lastTitle    = json.get("lastTitle")     .asText();
            person.update();

            return GlobalResult.okResult();

        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e, "nickName - String",  "firstName - String",  "middleName - String",  "lastName - String",  "dateOfBirth - String",  "firstTitle - String", "lastTitle - String");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("PersonCreateController - updatePersonInformation ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }


}
