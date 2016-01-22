package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Person;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.response.GlobalResult;
import utilities.Secured;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class PersonCreateController extends Controller {

    public Result createNewPerson() {

        try{
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            Person person = new Person();
            person.mail = json.get("mail").asText();
            person.password = json.get("password").asText();


            if (Person.find.byId(json.get("mail").asText()) != null) throw new Exception("User with this email exist yet!");

            person.setSha();
            person.save();

            return GlobalResult.okResult();
        }catch(Exception e){return GlobalResult.badRequest(e);}
    }


    public  Result getPerson(String mail){
        try{

            Person p =Person.find.byId(mail);
            if(p == null ) throw new Exception("User not exist");


            return GlobalResult.okResult(Json.toJson(p));

        }catch(Exception e){return GlobalResult.badRequest(e);}
    }


    public  Result deletePerson(String mail){
        try{

            Person p = Person.find.byId(mail);
            if(p == null ) throw new Exception("User not exist");

            p.delete();

            return GlobalResult.okResult();

        }catch(Exception e){ return GlobalResult.badRequest(e);}
    }



    @Security.Authenticated(Secured.class)
    public  Result updatePersonInformation(){
        try{
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            Person person = Person.find.byId(json.get("email").asText());
            if (person == null) throw new Exception("User for update doesn't exist in database");

            person.firstName    = json.get("firstName")     .asText();
            person.middleName   = json.get("middleName")    .asText();
            person.lastNAme     = json.get("lastNAme")      .asText();
            person.dateOfBirth  = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH).parse(json.get("dateOfBirth").asText());
            person.firstTitle   = json.get("firstTitle")    .asText();
            person.lastTitle    = json.get("lastTitle")     .asText();
            person.save();

            return GlobalResult.okResult("updating was properly performed");

        }catch(Exception e){return GlobalResult.badRequest(e);}
    }


}
