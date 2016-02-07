package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuthService;
import models.login.LinkedAccount;
import models.login.Person;
import play.Configuration;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.*;
import utilities.loginEntities.Secured;
import utilities.loginEntities.Socials;
import utilities.response.CoreResponse;
import utilities.response.GlobalResult;

import javax.inject.Inject;
import java.text.SimpleDateFormat;


public class SecurityController extends Controller {

    public final static String AUTH_TOKEN_HEADER = "X-AUTH-TOKEN";
    public final static String AUTH_TOKEN = "authToken";

    public Result index() {
        return ok("Version 1.4 is alive!");
    }

    public static Person getPerson() {
        return (Person) Http.Context.current().args.get("person");
    }


    @BodyParser.Of(BodyParser.Json.class)
    public Result login() {
        try {
            JsonNode json = request().body().asJson();

            Person person = Person.findByEmailAddressAndPassword(json.get("email").asText(), json.get("password").asText());
            if (person == null) return GlobalResult.forbidden("Email or password are wrong");


            if (!person.emailValidated) return GlobalResult.forbidden("Account is not authorized");

            String authToken = person.createToken();

            ObjectNode result = Json.newObject();
            result.replace("person", Json.toJson(person));
            result.put("authToken", authToken);

            response().setCookie(AUTH_TOKEN, authToken);

            return GlobalResult.okResult(result);

        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e, "email - String", "password - String");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("SecurityController - login ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }


    public  Result getPersonByToken(){
        try{

            String token = request().getHeader("X-AUTH-TOKEN");

            Person person = Person.findByAuthToken(token);
            if(person == null) return GlobalResult.forbidden("Account is not authorized");

            ObjectNode result = Json.newObject();
            result.replace("person", Json.toJson(person));
            result.put("authToken", token);

            response().setCookie(AUTH_TOKEN, token);

            return GlobalResult.okResult(result);

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("PersonCreateController - getPerson ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @Security.Authenticated(Secured.class)
    public Result logout() {
        try {

            response().discardCookie(AUTH_TOKEN);
            getPerson().deleteAuthToken();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - logout ERROR");
            return GlobalResult.internalServerError();
        }
    }

    //#### Oaut pro příjem Requestů zvenčí ################################################################################*/
    // Metoda slouží pro příjem autentifikačních klíču ze sociálních sítí když se přihlásí uživatel.
    // Taktéž spojuje přihlášené účty pod jednu cvirtuální Person - aby v systému bylo jendotné rozpoznávání.
    // V nějaké fázy je nutné mít email - pokud ho nedostaneme od sociální služby - mělo by někde v kodu být upozornění pro fontEnd
    // Aby doplnil uživatel svůj email - hlavní identifikátor!
    public Result GET_github_oauth(String code, String state) {
        try {

            LinkedAccount linkedAccount = LinkedAccount.find.where().eq("providerKey", state).findUnique();
            if (linkedAccount == null) return redirect(Configuration.root().getString("Becki.redirectFail"));
            linkedAccount.tokenVerified = true;

            OAuthService service = Socials.GitHub(state);

            Verifier verifier = new Verifier(code);
            Token accessToken = service.getAccessToken(null, verifier);


            OAuthRequest request = new OAuthRequest(Verb.GET, Configuration.root().getString(linkedAccount.typeOfConnection + ".url"), service);
            service.signRequest(accessToken, request);

            Response response = request.send();
            if (!response.isSuccessful()) {
                Logger.error("Incoming state: " + state + " not found in database");
                redirect(Configuration.root().getString("Becki.redirectFail"));
            }

            System.out.println("Body = " + response.getBody());

            JsonNode jsonNode = Json.parse(response.getBody());


                // Zkontroluji zda už takové id nemám náhodou v databázi a pokud ano, zamezuji duplicitě
                LinkedAccount usedAccount = LinkedAccount.find.where().eq("providerUserId", jsonNode.get("id").asText()).findUnique();

                // Ovařím zda už v databázi není - pokud ano provedu následující metodu, ve které smažu poslední LinkedAccount
                // a používám už ten předchozí, jen prohodím a aktualizuji token! Poté přesměruji
                if (usedAccount != null) {

                    usedAccount.authToken = linkedAccount.authToken;
                    linkedAccount.delete();
                    usedAccount.update();
                    return redirect(Configuration.root().getString("Becki.redirectOk"));

                }

                // Pokud se uživatel přihlásí poprvé
                else {

                    // Přiřadím do záznamu id uživatele ze sociální sítě
                    linkedAccount.providerUserId = jsonNode.get("id").asText();
                    linkedAccount.update();



                        Person person = new Person();

                        if (jsonNode.has("email")) {
                            person.mail = jsonNode.get("email").asText();
                        }

                        if (jsonNode.has("login")) {
                            person.nickName = jsonNode.get("login").asText();
                        }

                        if (jsonNode.has("name")) {
                            try {
                                System.out.println("name: " + jsonNode.get("login").asText());
                                String[] parts = jsonNode.get("name").asText().split("\\s+");
                                person.firstName = parts[0];
                                person.lastName = parts[1];
                            }catch (ArrayIndexOutOfBoundsException e){
                                System.out.println("Uživatel nemá vyplněné jméno a příjmení s mezerou .. enbo jiná TODO aktivita");
                            }
                        }


                        person.setToken(linkedAccount.authToken); // update je v metodě už zahrnut!
                        person.save();

                        linkedAccount.person = person;
                        linkedAccount.update();

                        return redirect(Configuration.root().getString("Becki.redirectOk"));


                }

        } catch (Exception e) {

            Logger.error("Error ");
            Logger.error("Error - Příchozí JSON v metodě GEToauth_callback neobsahuje identifikačnmí ID!");
            Logger.error("Error \n");
            Logger.error("Error", e);
            Logger.error("SecurityController - GEToauth_callback ERROR");
            return GlobalResult.internalServerError();
        }


    }

    @Inject WSClient ws;

    public Result GET_facebook_oauth(String code, String state) {
        try {

            LinkedAccount linkedAccount = LinkedAccount.find.where().eq("providerKey", state).findUnique();
            if (linkedAccount == null) return redirect(Configuration.root().getString("Becki.redirectFail"));
            linkedAccount.tokenVerified = true;

            OAuthService  service = Socials.Facebook(state);

            Verifier verifier = new Verifier(code);
            Token accessToken = service.getAccessToken(null, verifier);

            OAuthRequest request = new OAuthRequest(Verb.GET, Configuration.root().getString(linkedAccount.typeOfConnection + ".url"), service);
            service.signRequest(accessToken, request);

            Response response = request.send();
            if (!response.isSuccessful()) {
                Logger.error("Incoming state: " + state + " not found in database");
                redirect(Configuration.root().getString("Becki.redirectFail"));
            }

            System.out.println("Body = " + response.getBody());

            JsonNode jsonNode = Json.parse(response.getBody());

           // System.out.println("Acces token je: " + accessToken.getToken());

            WSRequest wsrequest = ws.url("https://graph.facebook.com/v2.5/"+ jsonNode.get("id").asText());
            WSRequest complexRequest = wsrequest.setQueryParameter("access_token", accessToken.getToken())
                                                .setQueryParameter("fields", "id,name,first_name,last_name,email,birthday,languages");

            F.Promise<JsonNode> jsonPromise = wsrequest.get().map(rsp -> { return rsp.asJson();});
            JsonNode jsonRequest = jsonPromise.get(10000);

            System.out.println("Příchozí JSON: " + jsonRequest.toString());

            // Zkontroluji zda už takové id nemám náhodou v databázi a pokud ano, zamezuji duplicitě
           LinkedAccount usedAccount = LinkedAccount.find.where().eq("providerUserId", jsonRequest.get("id").asText()).findUnique();

           // Ověřím zda už v databázi není - pokud ano provedu následující metodu, ve které smažu poslední LinkedAccount
           // a používám už ten předchozí, jen prohodím a aktualizuji token! Poté přesměruji
           if (usedAccount != null) {

                usedAccount.authToken = linkedAccount.authToken;
                linkedAccount.delete();
                usedAccount.update();
                usedAccount.person.setToken(usedAccount.authToken);
                usedAccount.person.update();
                return redirect(Configuration.root().getString("Becki.redirectOk"));

           }

           // Pokud se uživatel přihlásí poprvé
           else {

                // Přiřadím do záznamu id uživatele ze sociální sítě
                linkedAccount.providerUserId = jsonRequest.get("id").asText();
                linkedAccount.update();

                // A vytvářím osobu - Je nutné se podívat, zda náhodou už neexistuje daná osoba v systému
                // - pak bych tento LinkedAccount napojil na ní.


                // V ostatních případech - kdy email nemám a tedy nemám šanci najít osobu pro marge

                        Person person = new Person();
                        if (jsonRequest.has("email")) {
                            person.mail = jsonRequest.get("email").asText();
                        }

                        if (jsonRequest.has("first_name")) {
                            person.firstName = jsonRequest.get("first_name").asText();
                        }
                        if (jsonRequest.has("last_name")) {
                            person.lastName = jsonRequest.get("last_name").asText();
                        }
                        if (jsonRequest.has("birthday")) {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY");
                                person.dateOfBirth = sdf.parse(jsonRequest.get("birthday").asText());
                            }catch (Exception e){
                                Logger.error("Error FACEBOOK");
                                Logger.error("Error - Příchozí JSON Obsahoval narozeniny - ale něco se pokazilo! " + jsonRequest.toString());
                                Logger.error("Error '");
                            }
                        }

                        person.setToken(linkedAccount.authToken);
                        person.save();

                        linkedAccount.person = person;
                        linkedAccount.update();

                        return redirect(Configuration.root().getString("Becki.redirectOk"));


           }



        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("SecurityController - GEToauth_callback ERROR");
            return GlobalResult.internalServerError();
        }
    }


//###### Socilání sítě - a generátory přístupů ########################################################################*/

    public Result GitHub(){
        try {
            LinkedAccount linkedAccount = LinkedAccount.setProviderKey("GitHub");

            OAuthService service = Socials.GitHub(linkedAccount.providerKey);

            ObjectNode result = Json.newObject();
            result.put("type", "GitHub");
            result.put("url", service.getAuthorizationUrl(null));
            result.put("authToken", linkedAccount.authToken);



            return GlobalResult.okResult(result);

        }catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("SecurityController - GitHub ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public Result Facebook(){
        try {
            LinkedAccount linkedAccount = LinkedAccount.setProviderKey("Facebook");

            OAuthService service = Socials.Facebook(linkedAccount.providerKey);


            ObjectNode result = Json.newObject();
            result.put("type", "Facebook");
            result.put("url", service.getAuthorizationUrl(null));
            result.put("authToken", linkedAccount.authToken);



            return GlobalResult.okResult(result);
        }catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("SecurityController - facebook ERROR");

            return GlobalResult.internalServerError();
        }
    }

    public Result Twitter(){
        try {
            LinkedAccount linkedAccount = LinkedAccount.setProviderKey("Twitter");

            OAuthService service = Socials.Twitter(linkedAccount.providerKey);


            Token requestToken = service.getRequestToken();

            ObjectNode result = Json.newObject();
            result.put("type", "Twitter");
            result.put("url", service.getAuthorizationUrl(requestToken) );
            result.put("authToken", linkedAccount.authToken);



            return GlobalResult.okResult(result);

        }catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("SecurityController - Twitter ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public Result Vkontakte(){
        try {
            LinkedAccount linkedAccount = LinkedAccount.setProviderKey("Vkontakte");

            OAuthService service = Socials.Vkontakte(linkedAccount.providerKey);


            ObjectNode result = Json.newObject();
            result.put("type", "Vkontakte");
            result.put("url", service.getAuthorizationUrl(null));
            result.put("authToken", linkedAccount.authToken);



            return GlobalResult.okResult(result);

        }catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("SecurityController - facebook ERROR");
            return GlobalResult.internalServerError();
        }
    }

//###### Option########################################################################*/

    public Result option(){
        return GlobalResult.okResult();
    }

    public Result optionLink(String url){
        CoreResponse.cors();
        response().setHeader("Access-Control-Link", url);
        return ok();
    }
}