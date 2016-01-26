package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.login.Person;
import models.login.ValidationToken;
import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.Secured;
import utilities.response.GlobalResult;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class PersonCreateController extends Controller {

    @BodyParser.Of(BodyParser.Json.class)
    public Result developerRegistration() {
        try{
            JsonNode json = request().body().asJson();

            Person person = new Person();
            person.mail = json.get("mail").asText();
            person.password = json.get("password").asText();
            person.validation(true);

            if (Person.find.byId(json.get("mail").asText()) != null) return GlobalResult.badRequest("Email Exist");



            person.setSha(json.get("password").asText());
            person.save();

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

    @BodyParser.Of(BodyParser.Json.class)
    public Result standartRegistration() {
        try{
            JsonNode json = request().body().asJson();

            Person person = new Person();

            person.nickName = json.get("nickName").asText();
            person.mail     = json.get("mail").asText();
            person.password = json.get("password").asText();
            person.validation(false);

            if (Person.find.byId(json.get("mail").asText()) != null) return GlobalResult.badRequest("Email Exist");

            person.setSha(json.get("password").asText());
            person.save();

            EmailController.sendEmailValidation(person);
            return GlobalResult.okResult();

        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e,"nickName - String", "password - String",  "mail - String");
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

            person.validation(true);
            person.update();

            validationToken.delete();

            return GlobalResult.redirect( Configuration.root().getString("Becki.accountAuthorizedSuccessful ") );
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("PersonCreateController - updatePersonInformation ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @Security.Authenticated(Secured.class)
    public  Result getPerson(String id){
        try{

            Person person =Person.find.byId(id);
            if(person == null )  return GlobalResult.notFound();


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
            if(person == null ) return GlobalResult.notFound();

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
            if (person == null) return GlobalResult.notFound();

            person.nickName     =  json.get("nickName")     .asText();
            person.firstName    = json.get("firstName")     .asText();
            person.middleName   = json.get("middleName")    .asText();
            person.lastNAme     = json.get("lastName")      .asText();
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
