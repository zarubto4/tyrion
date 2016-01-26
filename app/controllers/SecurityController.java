package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuthService;
import models.login.LinkedAccount;
import models.login.Person;
import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.mvc.*;
import utilities.Secured;
import utilities.loginEntities.Socials;
import utilities.response.CoreResponse;
import utilities.response.GlobalResult;


public class SecurityController extends Controller {

    public final static String AUTH_TOKEN_HEADER = "X-AUTH-TOKEN";
    public final static String AUTH_TOKEN = "authToken";

    public Result index(){
        return ok("Version 1.4 is alive!");
    }

    public static Person getPerson() {
        return (Person) Http.Context.current().args.get("person");
    }


    @BodyParser.Of(BodyParser.Json.class)
    public Result login() {
       try{
        JsonNode json = request().body().asJson();

        Person person = Person.findByEmailAddressAndPassword(json.get("email").asText(), json.get("password").asText());
        if (person == null) return GlobalResult.forbidden("Email or password are wrong");

        String authToken = person.createToken();

        ObjectNode result = Json.newObject();
        result.replace("person", Json.toJson(person));
        result.put("authToken", authToken );

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

   @Security.Authenticated(Secured.class)
    public  Result logout() {
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
    public Result  GEToauth_callback(String code, String state){
        try {

            LinkedAccount linkedAccount = LinkedAccount.find.where().eq("providerKey", state).findUnique();
            if(linkedAccount == null) return redirect(Configuration.root().getString("Becki.redirectFail"));
            linkedAccount.tokenVerified = true;

            OAuthService service;

            switch (linkedAccount.typeOfConnection){
                case "Facebook" : service  = Socials.Facebook(state);   break;
                case "Github"   : service  = Socials.GitHub(state);     break;
                case "Twitter"  : service  = Socials.Twitter(state);    break;
                case "Vkontakte": service  = Socials.Vkontakte(state);  break;
                default: {
                    Logger.error("Error - SWITCH ERROR");
                    Logger.error("linkedAccount.typeOfConnection: " + linkedAccount.typeOfConnection + " not found");
                    return GlobalResult.internalServerError();
                }
            }

            Verifier verifier = new Verifier(code);
            Token accessToken = service.getAccessToken(null, verifier);


            OAuthRequest request = new OAuthRequest(Verb.GET, Configuration.root().getString(linkedAccount.typeOfConnection + ".url") , service);
            service.signRequest(accessToken, request);

            Response response = request.send();
            if(!response.isSuccessful()){
                Logger.error("Incoming state: " + state + " not found in database");
                redirect(Configuration.root().getString("Becki.redirectFail"));
            }


            JsonNode jsonNode = Json.parse(response.getBody());

            // Zkontroluji zda příchozí Json obsahuje id uživatele ze sociální sítě
            if(jsonNode.has("id")) {

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

                    // A vytvářím osobu - Je nutné se podívat, zda náhodou už neexistuje daná osoba v systému
                    // - pak bych tento LinkedAccount napojil na ní.

                    // Existuje email podle které bych mohl identifikovat osobu - a Person s tímto emailem existuje.
                    if( jsonNode.has("email") && Person.find.where().eq("mail", jsonNode.get("mail").asText()).findUnique() != null ) {
                       Person person = Person.find.where().eq("mail", jsonNode.get("mail").asText()).findUnique();

                        person.linkedAccounts.add(linkedAccount);
                        person.setToken(linkedAccount.authToken); // update je v metodě už zahrnut!
                        person.update();
                        return redirect(Configuration.root().getString("Becki.redirectOk"));
                    }
                    // V ostatních případech - kdy email nemám a tedy nemám šanci najít osobu pro marge
                    else{

                        Person person = new Person();
                        if( jsonNode.has("email") ){
                            person.mail = jsonNode.get("email").asText();
                        }
                        person.setToken(linkedAccount.authToken); // update je v metodě už zahrnut!
                        person.save();

                        linkedAccount.person = person;
                        linkedAccount.update();

                        return redirect(Configuration.root().getString("Becki.redirectOk"));
                    }

                }
            }

            Logger.error("Error ");
            Logger.error("Error - Příchozí JSON v metodě GEToauth_callback neobsahuje identifikačnmí ID!");
            Logger.error("Error \n");

            return redirect(Configuration.root().getString("Becki.redirectFail"));

        }catch (Exception e) {
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