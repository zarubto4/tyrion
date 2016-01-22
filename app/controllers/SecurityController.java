package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Person;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utilities.response.GlobalResult;
import utilities.response.CoreResponse;
import utilities.Secured;


public class SecurityController extends Controller {

    public final static String AUTH_TOKEN_HEADER = "X-AUTH-TOKEN";
    public static final String AUTH_TOKEN = "authToken";


    public static Person getPerson() {
        return (Person) Http.Context.current().args.get("person");
    }


    public Result login() {
       try{

        JsonNode json = request().body().asJson();
        if (json == null) throw new Exception("Null Json");


        Person person = Person.findByEmailAddressAndPassword(json.get("email").asText(), json.get("password").asText());
        if (person == null) throw new Exception("Email or password are wrong");

        String authToken = person.createToken();

        ObjectNode result = Json.newObject();
        result.replace("person", Json.toJson(person));
        result.put("authToken", authToken );


        response().setCookie(AUTH_TOKEN, authToken);

        return GlobalResult.okResult(result);

        }catch(Exception e){
           return GlobalResult.badRequest(e);
       }
    }

   @Security.Authenticated(Secured.class)
    public  Result logout() {
        try {
            response().discardCookie(AUTH_TOKEN);
            getPerson().deleteAuthToken();

            return GlobalResult.okResult();

        }catch(Exception e){return GlobalResult.badRequest(e);}
    }


    public Result option(){
        return GlobalResult.okResult();
    }

    public Result optionLink(String url){

        CoreResponse.cors();
        response().setHeader("Access-Control-Link", url);
        System.out.println("URL: " + url + " confirm with POST");
        return ok();
    }


}